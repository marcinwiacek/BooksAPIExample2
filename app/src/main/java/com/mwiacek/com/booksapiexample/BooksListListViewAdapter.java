package com.mwiacek.com.booksapiexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Adapter for List View displaying books list.
 */
public class BooksListListViewAdapter extends BaseAdapter {
    public static final int ITEMS_READ_WITH_SINGLE_JSON = 25;

    private final String mDiskCachePath;
    private LruCache<String, Bitmap> mMemoryCache;

    private ArrayList<Books> mData = new ArrayList<>();
    private String mSearchText;
    private ProgressBar mProgressBar;

    /**
     * @param context - context in which we create our adapter.
     * @param progressBar - progressBar indicating that we're doing search.
     */
    BooksListListViewAdapter(Context context, ProgressBar progressBar) {
        // Use 1/4 of available memory for cache
        final int mCacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024) / 4;
        mMemoryCache = new LruCache<String, Bitmap>(mCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        mDiskCachePath = context.getExternalCacheDir() == null ?
                context.getCacheDir().getPath() : context.getExternalCacheDir().getPath();

        mProgressBar = progressBar;
    }

    /**
     * Generates URL text for searching books.
     *
     * @param searchText       - keyword.
     * @param startSearchGroup - information, from which entry we should start.
     * @return - URL.
     */
    public static String buildSearchURL(String searchText, int startSearchGroup) {
        return "https://www.googleapis.com/books/v1/volumes?" +
                "q=" + searchText +
                "&startIndex=" + (startSearchGroup * ITEMS_READ_WITH_SINGLE_JSON) +
                "&maxResults=" + ITEMS_READ_WITH_SINGLE_JSON;
    }

    /**
     * Start books search.
     *
     * @param searchText - text to search.
     * @param context - context in which we search.
     */
    public void startSearch(String searchText, Context context) {
        boolean isConnectedToInternet = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                isConnectedToInternet = activeNetwork.isConnectedOrConnecting();
            }
        }
        if (!isConnectedToInternet) {
            Toast.makeText(context, R.string.noNetworkStringTooltip, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mData.size() != 0) {
            mData.clear();
            notifyDataSetInvalidated();
        }

        mSearchText = searchText;

        new DownloadBooksInfoTask(this, new WeakReference<>(context),
                mData, buildSearchURL(mSearchText, 0),
                new WeakReference<>(mProgressBar))
                .execute();
    }

    /**
     * Get number of entries in List View.
     * @return - number of entries.
     */
    @Override
    public int getCount() {
        return mData.size() == 0 ? 0 :
                (mData.size() - 1) * ITEMS_READ_WITH_SINGLE_JSON
                        + mData.get(mData.size() - 1).items.length;
    }

    /**
     * Generates unique ID of concrete List View entry.
     * @param position - position.
     * @return - ID.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get item for concrete List View entry.
     * @param itemPosition - item position.
     * @return - data item.
     */
    @Override
    public Object getItem(int itemPosition) {
        int itemGroup = itemPosition / ITEMS_READ_WITH_SINGLE_JSON;
        int itemPositionInsideGroup = itemPosition - itemGroup * ITEMS_READ_WITH_SINGLE_JSON;

        return mData.get(itemGroup).items.length <= itemPositionInsideGroup ?
                null : mData.get(itemGroup).items[itemPositionInsideGroup];
    }

    /**
     * Getting view for concrete entry of List View.
     * @param position - entry position.
     * @param convertView - view.
     * @param parent - List View parent.
     * @return - view.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.book_list_item, parent, false);

            holder = new ViewHolder();
            holder.titleText = convertView.findViewById(R.id.BookName);
            holder.thumbnailPicture = convertView.findViewById(R.id.BookImage);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;

        int itemGroup = position / ITEMS_READ_WITH_SINGLE_JSON;

        // Read next page if we see one of last entries and there is something to read.
        if (position > getCount() - 5 && position < mData.get(0).totalItems - 1) {
            new DownloadBooksInfoTask(this, new WeakReference<>(convertView.getContext()),
                    mData, buildSearchURL(mSearchText, itemGroup + 1),
                    new WeakReference<>(mProgressBar))
                    .execute();
        }

        Book book = (Book) getItem(position);
        if (book == null) {
            Toast.makeText(convertView.getContext(), R.string.errorInDataTooltip, Toast.LENGTH_SHORT).show();
            holder.titleText.setText("");
            holder.thumbnailPicture.setImageBitmap(null);
            return convertView;
        }

        holder.titleText.setText(book.volumeInfo.title);

        if (book.volumeInfo.imageLinks == null ||
                book.volumeInfo.imageLinks.smallThumbnail == null) {
            holder.thumbnailPicture.setImageBitmap(null);
            return convertView;
        }

        if (mMemoryCache.get(book.id) != null) {
            holder.thumbnailPicture.setImageBitmap(mMemoryCache.get(book.id));
            return convertView;
        }

        File file = new File(mDiskCachePath + File.separator + book.id);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(mDiskCachePath + File.separator + book.id);
            holder.thumbnailPicture.setImageBitmap(bitmap);
            mMemoryCache.put(book.id, bitmap);
            return convertView;
        }

        holder.thumbnailPicture.setImageBitmap(null);
        new DownloadThumbnailTask(position, holder,
                book.volumeInfo.imageLinks.smallThumbnail, book.id, mDiskCachePath, mMemoryCache).execute();
        return convertView;
    }

    /**
     * Holder for elements of List View entries.
     */
    public static class ViewHolder {
        public int position;
        public ImageView thumbnailPicture;
        private TextView titleText;
    }
}
