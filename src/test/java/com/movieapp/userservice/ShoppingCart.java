package com.movieapp.userservice;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private final List<String> items;

    public ShoppingCart() {
        this.items = new ArrayList<>();
    }

    public void addItem(String item) {
        items.add(item);
    }

    public void removeItem(String item) {
        items.remove(item);
    }

    public boolean contains(String item) {
        return items.contains(item);
    }

    public int getItemCount() {
        return items.size();
    }

    public List<String> getItems() {
        return items;
    }
}
