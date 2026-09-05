package com.movieapp.userservice;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShoppinCartTests {

    ShoppingCart cart;

    @BeforeEach
    void clearCart() {
        cart = new ShoppingCart();
    }

    @Test
    void startWithEmptyCart() {
        assertThat(cart.getItemCount()).isEqualTo(0);
    }

    @Test
    void shouldContainAddedItems() {
        cart.addItem("Pen");
        cart.addItem("Book");

//        SoftAssertions softly = new SoftAssertions();
//        softly.assertThat(cart.getItems().contains("Pen")).isTrue();
//        softly.assertThat(cart.getItems().contains("Book")).isTrue();
//
//        softly.assertAll();

        assertThat(cart.getItems())
                .hasSize(2)
                .containsExactly("Pen","Book");
    }

    @Test
    void removeItemFromCart() {
        cart.addItem("Book");
        cart.addItem("Pen");
        cart.removeItem("Book");
        assertThat(cart.getItems())
                .doesNotContain("Book");
    }

    @Test
    void shouldReturnCorrectItemCount() {
        cart.addItem("Book");
        cart.addItem("Watch");

        assertThat(cart.getItemCount()).isEqualTo(2);

    }

    @Test
    void shouldReturnAllItems() {
        cart.addItem("Book");
        cart.addItem("Watch");
        cart.addItem("Shirt");

        assertThat(cart.getItems())
                .containsExactly("Book","Watch","Shirt");
    }





}
