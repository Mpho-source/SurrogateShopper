package com.example.surrogateshopper;

import java.util.*;

public class Order {
    String id;
    String basketName;
    String shopperName;
    String status;
    List<String> items;

    public Order(String ID, String Basket, String Name, String Stst, List<String> ListItems) {
        this.id = ID;
        this.basketName = Basket;
        this.shopperName = Name;
        this.status = Stst;
        this.items = ListItems;
    }
}