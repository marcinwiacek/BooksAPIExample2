package com.mwiacek.com.booksapiexample;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class MainActivity extends Activity {
    private ViewSwitcher mViewSwitcher;
    // Elements from first layout in ViewSwitcher (book list).
    private Button mBookListSearchButton;
    private EditText mBookListCriteriaEditText;
    // Elements from second layout in ViewSwitcher (single book page).
    private TextView mBookPageTextView;

    private void addBookInfoTextToBookPage(int resource, String info) {
        if (info != null) {
            mBookPageTextView.append(String.format(getResources().getString(resource), info));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewSwitcher = findViewById(R.id.viewSwitcher);
        // Elements from first layout in ViewSwitcher (book list).
        ProgressBar bookListProgressBar = findViewById(R.id.searchProgressBar);
        mBookListCriteriaEditText = findViewById(R.id.searchEditText);
        mBookListSearchButton = findViewById(R.id.searchButton);
        ListView mBookListListView = findViewById(R.id.booksListListView);
        // Elements from second layout in ViewSwitcher (single book page).
        mBookPageTextView = findViewById(R.id.bookInfoTextView);
        Button mBookPageBackButton = findViewById(R.id.backButton);

        BooksListListViewAdapter bookListAdapter = new BooksListListViewAdapter(getApplicationContext(),
                bookListProgressBar);
        mBookListListView.setAdapter(bookListAdapter);
        mBookListListView.setOnItemClickListener((parent, view, position, id) -> {
            Book book = (Book) parent.getAdapter().getItem(position);
            if (book == null) return;

            mBookPageTextView.setText("");
            addBookInfoTextToBookPage(R.string.bookTitleInfoString, book.volumeInfo.title);
            if (book.volumeInfo.authors != null) {
                for (String author : book.volumeInfo.authors) {
                    addBookInfoTextToBookPage(R.string.bookAuthorInfoString, author);
                }
            }
            addBookInfoTextToBookPage(R.string.bookDescriptionInfoString, book.volumeInfo.description);
            addBookInfoTextToBookPage(R.string.bookPublishedDateInfoString, book.volumeInfo.publishedDate);
            addBookInfoTextToBookPage(R.string.bookPublisherInfoString, book.volumeInfo.publisher);

            mViewSwitcher.showNext();
        });

        mBookListCriteriaEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                mBookListSearchButton.callOnClick();
                return true;
            }
            return false;
        });

        mBookListSearchButton.setOnClickListener(v -> {
            if (mBookListCriteriaEditText.getText().length() == 0) {
                Toast.makeText(getApplicationContext(), R.string.emptySearchStringTooltip,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Hide keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                    Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            bookListAdapter.startSearch(mBookListCriteriaEditText.getText().toString(),
                    getApplicationContext());
        });

        mBookPageTextView.setMovementMethod(new ScrollingMovementMethod());

        mBookPageBackButton.setOnClickListener(v -> mViewSwitcher.showPrevious());

        // Initiate search on application start.
        bookListAdapter.startSearch(mBookListCriteriaEditText.getText().toString(),
                getApplicationContext());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Return from the book page to book list on the Back button.
        if (keyCode == KeyEvent.KEYCODE_BACK && mViewSwitcher.getDisplayedChild() == 1) {
            mViewSwitcher.showPrevious();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
