package com.example.demo.test;

import com.example.demo.services.Calculator;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CalculatorTest2 {
    @Autowired
    private Calculator calc;

    @Test
    public void addTest(){
        Assertions.assertEquals(10,calc.add(4,6));
    }

    @Test
    public void subtractTest(){
        Assertions.assertEquals(3,calc.subtract(10,8));
    }

    @Test
    public void addTest2(){
        Assertions.assertNotEquals(7,calc.add(5,3));
    }

    @Test
    public void subtractTest2(){
        Assertions.assertEquals(13,calc.subtract(100,87));
    }
}
