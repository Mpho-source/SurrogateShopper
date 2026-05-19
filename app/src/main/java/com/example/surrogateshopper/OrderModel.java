package com.example.surrogateshopper;

import java.util.HashMap;

public class OrderModel {
    public String basketName;
    public HashMap<String, Integer> items;
    public String timestamp;
    public String status;

    public OrderModel(String basketName, HashMap<String, Integer> items,
                      String timestamp, String status) {
        this.basketName = basketName;
        this.items      = items;
        this.timestamp  = timestamp;
        this.status     = status;
    }
}
