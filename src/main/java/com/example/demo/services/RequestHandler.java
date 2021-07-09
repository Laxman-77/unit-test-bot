package com.example.demo.services;


import com.example.demo.components.SlackResponse;
import com.example.demo.test.TestRunner;
import com.example.demo.test.TestRunnerWithTimeFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RequestHandler {
    public static SlackResponse getAllFailures() throws IOException, ClassNotFoundException {
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

    public static SlackResponse getFailuresByAuthor(String author) throws IOException, ClassNotFoundException {
        SlackResponse response = new SlackResponse();

        if(author.length()==0){
            response.setResponseType("ephemeral");
            response.setText("Invalid Command! \nYou haven't passed the author name after the command.\nExample command: /get_failures_by_author laxman.goliya \n");
            return response;
        }

        HashMap<String,String> authorMap = TestRunner.getAuthorMap();
        HashMap<String,String> fullClassName = TestRunner.getFullClassName();
        //String mapTable = MapUtils.getMapAsTableString(authorMap);

        /*
        ArrayList<String> failureList = new ArrayList<>(); // jenkins report list
        failureList.add("CalculatorTest1. addTest2");
        failureList.add("CalculatorTest2. subtractTest1");
        failureList.add("CalculatorTest2. addTest");
        */

        List<String> testsByAuthor = MapUtils.getAllTestsOfAuthor(authorMap,author);
        String listTable = MapUtils.getListAsTableString(testsByAuthor,fullClassName);

        String link = "<https://www.google.com|Google >\n";
        response.setResponseType("in_channel");
        response.setText("```"+"Here are your tests!\n"+link+listTable+"```");
        return response;
    }

    public static SlackResponse getTestAddedByAuthorInTimeframe(String text) throws IOException, ClassNotFoundException {
        Set<String> allowedTimeFrames = Set.of("LastWeek","LastSevenDays","ThisWeek","ThisMonth");
        SlackResponse response = new SlackResponse();
        response.setResponseType("in_channel");

        String[] temp = text.split(" ");
        if(temp.length!=2){
            response.setResponseType("ephemeral");
            response.setText("Invalid Command. \n there should be authorName and timeFrame after command.");
            return response;
        }
        String author = temp[0];
        String timeFrame = temp[1];

        if(!allowedTimeFrames.contains(timeFrame)){
            response.setResponseType("ephemeral");;
            response.setText("Invalid TimeFrame.\nThese are allowed timeFrames: LastWeek, LastSevenDays,ThisWeek,ThisMonth.\n");
            return response;
        }
        TestRunnerWithTimeFrame.timeFrameSetup(timeFrame);
        HashMap<String,String> authorMap = TestRunnerWithTimeFrame.getAuthorMap();
        HashMap<String,String> fullClassName = TestRunnerWithTimeFrame.getFullClassName();

        List<String> testsByAuthor = MapUtils.getAllTestsOfAuthor(authorMap,author);
        String embededListTable = MapUtils.getListAsTableString(testsByAuthor,fullClassName);

        System.out.println(embededListTable);
        response.setText("```Tests added by "+author+" in "+timeFrame +" : "+testsByAuthor.size()+"\nHere are all the tests: \n" + embededListTable+"\nHere are the failed tests:\n"+"```");
        return response;
    }
}
