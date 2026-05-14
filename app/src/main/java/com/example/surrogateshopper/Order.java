package com.example.surrogateshopper;

import java.util.*;

public class Order {
    String id;
    String basketName;
    String shopperName;
    String status;
    List<String> items;

    public Order(String ID, String Basket, String Name, String Stst, List<String> ListItems) {
        id = ID;
        basketName = Basket;
        shopperName = Name;
        status = Stst;
        items = ListItems;
    }
}