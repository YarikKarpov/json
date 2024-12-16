package org.example;

import java.util.List;

public class Client {
    public String name;
    public List<String> order;

    public Client(){

    }
    public Client(String name, List<String>order){
        this.name = name;
        this.order=order;
    }
}
