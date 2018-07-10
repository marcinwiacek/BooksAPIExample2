package com.mwiacek.com.booksapiexample;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwiacek.com.booksapiexample.Books;
import com.mwiacek.com.booksapiexample.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Task for downloading and decoding JSON with books info.
 */
public class DownloadBooksInfoTask extends AsyncTask<Object, Void, Boolean> {
    private BaseAdapter mAdapter;
    private WeakReference<Context> mContext;
    private WeakReference<ProgressBar> mProgressBar;
    private ArrayList<Books> mData;
    private String mUrl;

    DownloadBooksInfoTask(BaseAdapter adapter, WeakReference<Context> context,
                          ArrayList<Books> data, String url, WeakReference<ProgressBar> progressBar) {
        mAdapter = adapter;
        mContext = context;
        mData = data;
        mUrl = url;
        mProgressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        if (mProgressBar!=null) mProgressBar.get().setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            URL url = new URL(mUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(5000); // 5 seconds
            connection.connect();
            try {
                InputStream in = new BufferedInputStream(connection.getInputStream());

                Books books = new ObjectMapper().readValue(in, Books.class);

                Boolean firstGroup = mData.size() == 0;
                if (books.totalItems != 0) mData.add(books);
                return firstGroup;
            } finally {
                connection.disconnect();
            }
        } catch (IOException ignore) {
        }
        return false;
    }

    protected void onPostExecute(Boolean firstGroup) {
        if (firstGroup) {
            int results = mData.size() == 0 ? 0 : mData.get(0).totalItems;
            Toast.makeText(mContext.get(), String.format(mContext.get().getResources().getString(R.string.resultsTooltip), results),
                    Toast.LENGTH_SHORT).show();
        }
        if (mProgressBar != null) mProgressBar.get().setVisibility(View.INVISIBLE);
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }
}