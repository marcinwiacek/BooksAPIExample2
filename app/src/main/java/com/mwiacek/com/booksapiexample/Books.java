package com.mwiacek.com.booksapiexample;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Books {
    // Number of items in the search (not number of items in current structure).
    public int totalItems;

    public Book[] items;
}
