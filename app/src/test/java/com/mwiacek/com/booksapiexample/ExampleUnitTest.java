package com.mwiacek.com.booksapiexample;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void checkSearchUrl() throws Exception {
        String toCompare = "https://www.googleapis.com/books/v1/volumes?" +
                "q=android" +
                "&startIndex=0" +
                "&maxResults=" + BooksListListViewAdapter.ITEMS_READ_WITH_SINGLE_JSON;
        String generated = BooksListListViewAdapter.buildSearchURL("android",0);

        assertEquals(toCompare, generated);
    }
}