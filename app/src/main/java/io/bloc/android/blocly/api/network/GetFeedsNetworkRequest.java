package io.bloc.android.blocly.api.network;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by namlu on 27-Aug-16.
 */
public class GetFeedsNetworkRequest extends NetworkRequest <List<GetFeedsNetworkRequest.FeedResponse>>{

    // Error code thrown by Document parsing
    public static final int ERROR_PARSING = 3;

    // String references for each tag and attribute we require
    private static final String XML_TAG_TITLE = "title";
    private static final String XML_TAG_DESCRIPTION = "description";
    private static final String XML_TAG_LINK = "link";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_PUB_DATE = "pubDate";
    private static final String XML_TAG_GUID = "guid";
    private static final String XML_TAG_ENCLOSURE = "enclosure";

    // 56.5: The <content:encoded> and <media:content> tags are used similar to <description>
    // and <enclosure> respectively
    private static final String XML_TAG_CONTENT_ENCODED = "content:encoded";
    private static final String XML_TAG_MEDIA_CONTENT = "media:content";

    private static final String XML_ATTRIBUTE_URL = "url";
    private static final String XML_ATTRIBUTE_TYPE = "type";

    // Recover multiple RSS feeds and store each feed's address
    String [] feedUrls;

    public GetFeedsNetworkRequest(String... feedUrls){
        this.feedUrls = feedUrls;
    }

    // Method requests a BufferedReader for each URL found in feedUrls
    // Using a custom List<FeedResponse> decouples networking elements of application from model
    @Override
    public List<FeedResponse> performRequest() {

        // Variable which performRequest will ultimately return
        List<FeedResponse> responseFeeds = new ArrayList<FeedResponse>(feedUrls.length);

        for (String feedUrlString : feedUrls) {
            // openStream(String urlString), a method of NetworkRequest which takes a URL and returns an InputStream
            InputStream inputStream = openStream(feedUrlString);
            if (inputStream == null) {
                return null;
            }
            try {
                // DocumentBuilder parses an InputStream directly into a new Document
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                // Document class organizes raw XML String info into a manageable form, converting
                // each tag and attribute into a Node
                Document xmlDocument = documentBuilder.parse(inputStream);

                // Recover the title, link and description of the feed
                // .optFirstTagFromDocument(Document document, String tagName)
                String channelTitle = optFirstTagFromDocument(xmlDocument, XML_TAG_TITLE);
                String channelDescription = optFirstTagFromDocument(xmlDocument, XML_TAG_DESCRIPTION);
                String channelURL = optFirstTagFromDocument(xmlDocument, XML_TAG_LINK);

                // Each Node in list represents a single XML item (<item>...</item>) from feed
                NodeList allItemsNodes = xmlDocument.getElementsByTagName(XML_TAG_ITEM);
                List<ItemResponse> responseItems = new ArrayList<ItemResponse>(allItemsNodes.getLength());

                // Prepare a temporary variable for each field we hope to recover
                for (int itemIndex = 0; itemIndex < allItemsNodes.getLength(); itemIndex++) {
                    String itemURL = null;
                    String itemTitle = null;

                    // 56.3a: Stores the first image recovered from the HTML body, if any
                    String itemImageURL = null;

                    // 56.6:
                    String itemContentEncodedText = null;
                    String itemMediaURL = null;
                    String itemMediaMIMEType = null;

                    String itemDescription = null;
                    String itemGUID = null;
                    String itemPubDate = null;
                    String itemEnclosureURL = null;
                    String itemEnclosureMIMEType = null;

                    // Parse each item's tag
                    // Recover the Node representing the individual RSS item before
                    // extracting a list of its child Nodes
                    Node itemNode = allItemsNodes.item(itemIndex);
                    // .getChildNodes() returns a NodeList item
                    NodeList tagNodes = itemNode.getChildNodes();

                    for (int tagIndex = 0; tagIndex < tagNodes.getLength(); tagIndex++) {
                        Node tagNode = tagNodes.item(tagIndex);
                        String tag = tagNode.getNodeName();

                        //  Iterate across all tags to find those we wish to parse.
                        // .getTextContent() returns whatever is between the two XML tags as a String
                        if (XML_TAG_LINK.equalsIgnoreCase(tag)) {
                            itemURL = tagNode.getTextContent();
                        } else if (XML_TAG_TITLE.equalsIgnoreCase(tag)) {
                            itemTitle = tagNode.getTextContent();
                        } else if (XML_TAG_DESCRIPTION.equalsIgnoreCase(tag)) {
                            String descriptionText = tagNode.getTextContent();
                            itemImageURL = parseImageFromHTML(descriptionText);
                            // 56.4: Use parseTextFromHTML to strip unnecessary HTML tags and attributes,
                            // leaving only text
                            itemDescription = parseTextFromHTML(descriptionText);
                        } else if (XML_TAG_ENCLOSURE.equalsIgnoreCase(tag)) {
                            // Retrieve a map of all attributes and recover both the url and type entries
                            // Enclosure tag is different, its data comes in the form of attributes
                            NamedNodeMap enclosureAttributes = tagNode.getAttributes();
                            itemEnclosureURL = enclosureAttributes.getNamedItem(XML_ATTRIBUTE_URL).getTextContent();
                            itemEnclosureMIMEType = enclosureAttributes.getNamedItem(XML_ATTRIBUTE_TYPE).getTextContent();
                        } else if (XML_TAG_PUB_DATE.equalsIgnoreCase(tag)) {
                            itemPubDate = tagNode.getTextContent();
                        } else if (XML_TAG_GUID.equalsIgnoreCase(tag)) {
                            itemGUID = tagNode.getTextContent();
                        }
                        // 56.7: Treat <content:encoded> tag exactly like the <description> tag. Assume
                        // it is composed of HTML content and parse its plain text and first image
                        else if (XML_TAG_CONTENT_ENCODED.equalsIgnoreCase(tag)) {
                            String contentEncoded = tagNode.getTextContent();
                            itemImageURL = parseImageFromHTML(contentEncoded);
                            itemContentEncodedText = parseTextFromHTML(contentEncoded);
                        }
                        // 56.8: Treat <media:content> tag exactly like the <enclosure> tag.
                        else if (XML_TAG_MEDIA_CONTENT.equalsIgnoreCase(tag)) {
                            NamedNodeMap mediaAttributes = tagNode.getAttributes();
                            itemMediaURL = mediaAttributes.getNamedItem(XML_ATTRIBUTE_URL).getTextContent();
                            itemMediaMIMEType = mediaAttributes.getNamedItem(XML_ATTRIBUTE_TYPE).getTextContent();
                        }
                    }
                    // 56.3b: Use itemImageURL if and only if the RSS item did not provide enclosure
                    if (itemEnclosureURL == null) {
                        itemEnclosureURL = itemImageURL;
                    }
                    // 56.9: catches the case where an image was not retrieved from the description
                    // or content. We assign the values, if any, discovered within the  <media:content> tag
                    if (itemEnclosureURL == null) {
                        itemEnclosureURL = itemImageURL;
                        itemEnclosureMIMEType = itemMediaMIMEType;
                    }
                    // 56.10: replace the description with the content encoded text. We make the
                    // assumption that the data found in <content:encoded> is more robust than
                    // what was provided in <description>.
                    if (itemContentEncodedText != null) {
                        itemDescription = itemContentEncodedText;
                    }

                    // List<ItemResponse> needed for FeedResponse
                    responseItems.add(new ItemResponse(itemURL, itemTitle, itemDescription,
                            itemGUID, itemPubDate, itemEnclosureURL, itemEnclosureMIMEType));
                }
                // Add parsed items to responseFeeds and close InputStream
                responseFeeds.add(new FeedResponse(feedUrlString, channelTitle, channelURL,
                        channelDescription, responseItems));
                inputStream.close();
                // documentBuilder.parse throws IOException, SAXException
            } catch (IOException e) {
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            } catch (SAXException e) {
                e.printStackTrace();
                setErrorCode(ERROR_PARSING);
                return null;
                // DocumentBuilderFactory throws ParserConfigurationException
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                setErrorCode(ERROR_PARSING);
                return null;
            }
        }
        return responseFeeds;
    }

    // Method recovers every Node within the document representing a tag whose name matches tagName.
    // If one or more tags are found by that name, the first tag's content is returned
    private String optFirstTagFromDocument(Document document, String tagName){
        // NodeList provides the abstraction of an ordered collection of nodes
        // .getElementsByTagName(String tagName)
        NodeList elementsByTagName = document.getElementsByTagName(tagName);
        if (elementsByTagName.getLength() > 0) {
            // .getTextContent() returns the text content of the node
            return elementsByTagName.item(0).getTextContent();
        }
        return null;
    }

    // 56.1: Strips the HTML body of any text that's not displayed to user, including tag,
    // attribute, etc. This is the text to be presented to user as RSS item's content
    static String parseTextFromHTML(String htmlString) {
        org.jsoup.nodes.Document document = Jsoup.parse(htmlString);
        return document.body().text();
    }

    // 56.2: Pulls every img tag from HTML body, then returns source URL of the very first image. The
    // assumption is the first image in HTML body is the headline image.
    static String parseImageFromHTML(String htmlString) {
        org.jsoup.nodes.Document document = Jsoup.parse(htmlString);
        Elements imgElements = document.select("img");

        if (imgElements.isEmpty()) {
            return null;
        }
        return imgElements.attr("src");
    }


    // Returns the objects contained underneath the <channel>...<channel> tags of an XML doc
    // and a List<ItemResponse>
    public static class FeedResponse {
        public final String channelFeedURL;
        public final String channelTitle;
        public final String channelURL;
        public final String channelDescription;
        public final List<ItemResponse> channelItems;

        public FeedResponse(String channelFeedURL, String channelTitle, String channelURL,
                            String channelDescription, List<ItemResponse> channelItems) {
            this.channelFeedURL = channelFeedURL;
            this.channelTitle = channelTitle;
            this.channelURL = channelURL;
            this.channelDescription = channelDescription;
            this.channelItems = channelItems;
        }
    }

    // Returns the objects contained underneath the <item>...</item> tags of an XML doc and used
    // by FeedResponse
    public static class ItemResponse {
        public final String itemURL;
        public final String itemTitle;
        public final String itemDescription;
        public final String itemGUID;
        public final String itemPubDate;
        public final String itemEnclosureURL;
        public final String itemEnclosureMIMEType;

        public ItemResponse(String itemURL, String itemTitle, String itemDescription,
                            String itemGUID, String itemPubDate, String itemEnclosureURL,
                            String itemEnclosureMIMEType) {
            this.itemURL = itemURL;
            this.itemTitle = itemTitle;
            this.itemDescription = itemDescription;
            this.itemGUID = itemGUID;
            this.itemPubDate = itemPubDate;
            this.itemEnclosureURL = itemEnclosureURL;
            this.itemEnclosureMIMEType = itemEnclosureMIMEType;
        }
    }
}
