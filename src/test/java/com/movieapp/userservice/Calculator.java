package com.movieapp.userservice;

public class Calculator {
    public static int add(int a,int b) {
        return a + b;
    }

    public static int sub(int a,int b) {
        return a - b;
    }

    public static int mul(int a,int b) {
        return a * b;
    }

    public static int div(int a,int b) {
        if(b == 0) throw new ArithmeticException("Cannot divide with zero");
        return a / b;
    }

}
