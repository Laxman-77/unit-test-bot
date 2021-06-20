package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class Calculator {
    public int add(int a,int b){
        return a+b;
    }

    public int subtract(int a,int b){
        return a-b;
    }
}
