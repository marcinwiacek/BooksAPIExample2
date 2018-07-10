package com.mwiacek.com.booksapiexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Task for downloading book thumbnail and displaying it in the books list.
 */
public class DownloadThumbnailTask extends AsyncTask<Object, Void, String> {
    private int mPosition;
    private BooksListListViewAdapter.ViewHolder mViewHolder;
    private String mThumbnailURL;
    private String mBookID;

    private String mDiskCachePath;
    private LruCache<String, Bitmap> mMemoryCache;

    DownloadThumbnailTask(int position, BooksListListViewAdapter.ViewHolder viewHolder, String searchURL, String bookID,
                          String diskCachePath, LruCache<String, Bitmap> memoryCache) {
        mPosition = position;
        mViewHolder = viewHolder;
        mThumbnailURL = searchURL;
        mBookID = bookID;
        mDiskCachePath = diskCachePath;
        mMemoryCache = memoryCache;
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            URL url = new URL(mThumbnailURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(5000); // 5 seconds
            connection.connect();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                mMemoryCache.put(mBookID, bitmap);
                if (mDiskCachePath != null) {
                    try {
                        // TODO: implementing deleting oldest cache files
                        FileOutputStream fos = new FileOutputStream(mDiskCachePath + File.separator + mBookID);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                    } catch (IOException ignore) {
                    }
                }
            } finally {
                connection.disconnect();
            }
            return mBookID;
        } catch (IOException ignore) {
        }
        return null;
    }

    protected void onPostExecute(String bookId) {
        if (bookId != null && mPosition == mViewHolder.position) {
            mViewHolder.thumbnailPicture.setImageBitmap(mMemoryCache.get(bookId));
        }
    }
}
