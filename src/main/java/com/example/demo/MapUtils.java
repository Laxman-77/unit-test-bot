package com.example.demo;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {

    private static final String PADDING = " ";
    private static final String VERTICAL_SEPARATOR = "|";
    private static final String HORIZONTAL_SEPARATOR = "-";
    private static final String NEWLINE_SEPARATOR = "\n";
    private static final String INTERSECTION_CHAR = "+";

    public static List<String> getMapAsList(HashMap<String,String> authorMap){
        int maxAuthorNameLength = 11; // " Test Name " = 11
        int maxTestNameLength = 13; // " Author Name " = 13

        for(Map.Entry entry : authorMap.entrySet()){
            if(entry.getValue() == null) entry.setValue("No Author Found ");
            maxAuthorNameLength = Math.max(maxAuthorNameLength,entry.getValue().toString().length());
            maxTestNameLength = Math.max(maxTestNameLength,entry.getKey().toString().length());
        }

        /*
            Table Format:
            +--------------------------+-------------------+
            | Test Name                | Author Name       |
            +--------------------------+-------------------+
            | class1.testMethod1       | goliyalaxman00    |
            | class1.testMethod2       | abhinav.bollam    |
            +--------------------------+-------------------+
         */

        maxAuthorNameLength += 2; // for extra padding
        maxTestNameLength += 2;
        StringBuilder horizontal = new StringBuilder(); // +----------------+------------+
        horizontal.append(INTERSECTION_CHAR).append(StringUtils.repeat(HORIZONTAL_SEPARATOR,maxTestNameLength+1)).
                append(INTERSECTION_CHAR).append(StringUtils.repeat(HORIZONTAL_SEPARATOR,maxAuthorNameLength+1)).append(INTERSECTION_CHAR);
        //System.out.println(horizontal);

        String[] headings = {"Test Name", "Author Name"};
        StringBuilder headers = getPaddedEntry(headings,maxTestNameLength,maxAuthorNameLength);

        List<String> mapTable = new ArrayList<>();
        mapTable.add(horizontal.toString());
        mapTable.add(headers.toString());
        mapTable.add(horizontal.toString());

        for(Map.Entry entry: authorMap.entrySet()){
            if(entry.getValue() == null) continue;
            String[] entries = {entry.getKey().toString(),entry.getValue().toString()};
            StringBuilder padded = getPaddedEntry(entries,maxTestNameLength,maxAuthorNameLength);
            mapTable.add(padded.toString());
        }

        mapTable.add(horizontal.toString());

        return mapTable;
    }

    public static String getMapAsTableString(HashMap<String,String> myMap){
        // converting authorMap to string in table format
        List<String> list = getMapAsList(myMap);
        StringBuilder table = new StringBuilder();
        for( String entry: list){
            table.append(entry).append(NEWLINE_SEPARATOR);
        }
        return table.toString();
    }

    private static StringBuilder getPaddedEntry(String[] headings,Integer maxTestNameLength,Integer maxAuthorNameLength)
    {
        StringBuilder heading = new StringBuilder();
        heading.append(VERTICAL_SEPARATOR).append(PADDING).append(headings[0]).append(StringUtils.repeat(PADDING,maxTestNameLength-headings[0].length()))
                .append(VERTICAL_SEPARATOR).append(PADDING).append(headings[1]).append(StringUtils.repeat(PADDING,maxAuthorNameLength-headings[1].length()))
                .append(VERTICAL_SEPARATOR);
        return heading;
    }

}
