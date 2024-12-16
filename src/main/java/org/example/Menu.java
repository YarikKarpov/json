package org.example;

public  class Menu {
    public String name;
    public double price;

    public Menu() {}
    public Menu(String name,double price){
        this.name = name;
        this.price = price;

    }
    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}

