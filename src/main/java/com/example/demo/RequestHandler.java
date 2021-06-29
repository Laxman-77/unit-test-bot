package com.example.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RequestHandler {
    public static SlackResponse handleOnReceive() throws IOException, ClassNotFoundException {
        SlackResponse response = new SlackResponse();
        response.setResponseType("in_channel");

        HashMap<String,String> authorMap = TestRunner.getAuthorMap();
        String mapTable = MapUtils.getMapAsTableString(authorMap);

        ArrayList<String> failureList = new ArrayList<>(); // jenkins report list
        failureList.add("CalculatorTest1. addTest2");
        failureList.add("CalculatorTest2. subtractTest1");
        failureList.add("CalculatorTest2. addTest");

        String failureTestAuthorMapTable = MapUtils.getMapAsTableString(ListMapper.getAuthorMapForFailedTests(authorMap,failureList));

        response.setText("```" + "Unit Testing Test Author Map\n" +mapTable +"\nUnit Testing Failure Author Map\n"+ failureTestAuthorMapTable + " ```");

        System.out.println(mapTable);
        System.out.println(failureTestAuthorMapTable);

        return response;
    }
}
