package com.example.demo;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Configuration
@RunWith(SpringJUnit4ClassRunner.class)
public class CalculatorTest1 {
    @Autowired
    private Calculator calc;

    @Test
    public void addTest(){
        assertEquals(10,calc.add(4,6));
    }

    @Test
    public void subtractTest(){
        assertEquals(2,calc.subtract(10,8));
    }

    @Test
    public void addTest2(){
        assertNotEquals(7,calc.add(5,3));
    }

    @Test
    public void subtractTest2(){
        assertEquals(13,calc.subtract(100,87));
    }

}