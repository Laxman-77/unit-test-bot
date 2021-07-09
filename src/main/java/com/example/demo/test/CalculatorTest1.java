package com.example.demo.test;

import com.example.demo.services.Calculator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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

    @Test
    public void addtest3(){ assertEquals(200,calc.add(50,150));}

    @Test
    public void subtractTest3(){ assertEquals(300,calc.subtract(1000,700));}
}