package com.example;

public class App {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello from Java app");

        while (true) {
            Thread.sleep(60000);
        }
    }

    public static int add(int a, int b) {
        return a + b;
    }
}
