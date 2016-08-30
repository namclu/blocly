package io.bloc.android.blocly.api.network;

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

                // Method to recover the title, link and description of the feed
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
                            itemDescription = tagNode.getTextContent();
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
                    }
                    // List<ItemResponse> needed for FeedResponse
                    responseItems.add(new ItemResponse(itemURL, itemTitle, itemDescription,
                            itemGUID, itemPubDate, itemEnclosureURL, itemEnclosureMIMEType));
                }
                responseFeeds.add(new FeedResponse(feedUrlString, channelTitle, channelURL,
                        channelDescription, responseItems));
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            } catch (SAXException e) {
                e.printStackTrace();
                setErrorCode(ERROR_PARSING);
                return null;
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
            // .getTextContent() returns a String
            return elementsByTagName.item(0).getTextContent();
        }
        return null;
    }

    //
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

    //
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
