package com.mwiacek.com.booksapiexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.LruCache;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.mwiacek.com.booksapiexample", appContext.getPackageName());
    }

    @Test
    public void checkDownloadingJSON() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        ArrayList<Books> books = new ArrayList<>();
        String url = "https://www.googleapis.com/books/v1/volumes?" +
                "q=android" +
                "&startIndex=0" +
                "&maxResults=25";
        DownloadBooksInfoTask task = new DownloadBooksInfoTask(null, new WeakReference<>(appContext),
                books, url, null);

        assert books.size() == 0;
        task.execute();
        TimeUnit.SECONDS.sleep(10); //FIXME: we should not have fixed wait time.
        assert books.size() == 1;
        assert books.get(0).totalItems > 100;
    }
}
