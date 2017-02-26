package io.bloc.android.blocly;

import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<BloclyApplication> {
    public ApplicationTest() {
        super(BloclyApplication.class);
    }

    // 59.6: When providing multiple tests within a single file, it is preferable to perform the
    // initial configuration within the setUp().
    // RenamingDelegatingContent() uses underlying context we supply, except when reading
    // and writing from a database. When writing to database, the prefix "test_" is prepended
    // to the database file name.
    // Test case will not call onCreate() until createApplication() is called to allow
    // for setup and before getting an instance of Application
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setContext(new RenamingDelegatingContext(getContext(), "test_"));
        createApplication();
    }

    // 59.1: Any "public void" method beginning w "test" in a class extending TestCase is
    // identified as a single test.
    public void testApplicationHasDataSource() {

        // 59.3: After setUp() is called, get an instance of BloclyApplication
        BloclyApplication application = getApplication();

        // 59.4: Manually invoke onCreate(), as we need to test that onCreate() method properly
        // instantiates a new DataSource object
        application.onCreate();

        // 59.5: assetNotNull(Object) verifies that the Object passed in is present. If Object is
        // null, then test will fail.
        assertNotNull(application.getDataSource());
    }

    // 59.6: Verify that BloclyApplication initializes the UniversalImageLoader library
    public void testApplicationImageLoaderInitialization() {
        BloclyApplication application = getApplication();
        application.onCreate();
        // 59.7: assertTrue(boolean) to test if library initialization has occurred
        assertTrue(ImageLoader.getInstance().isInited());
    }
}