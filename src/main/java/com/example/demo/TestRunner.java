package com.example.demo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CalculatorTestSuite1.class,
        CalculatorTestSuite2.class
})
public class TestRunner {
    private static final String testDir = "src/main/";
    private static final String FILE_PREFIX = testDir+"/java/";

    public static final String PADDING = " ";
    public static final String VERTICAL_SEPARATOR = "|";
    public static final String HORIZONTAL_SEPARATOR = "-";
    public static final String NEWLINE_SEPARATOR = "\n";
    public static final String INTERSECTION_CHAR = "+";
    public static HashMap<String,String> authorMap;

    public static String getAuthorMap() throws ClassNotFoundException, IOException {
        Class currentClass = new Object(){}.getClass().getEnclosingClass(); // TestRunner.class
        //Result result = JUnitCore.runClasses(currentClass);

        authorMap = new HashMap<>(); // map for @Test method() --> AuthorName

        Suite.SuiteClasses testSuiteClasses = (Suite.SuiteClasses) currentClass.getAnnotation(Suite.SuiteClasses.class);
        Class<?>[] allTestSuitesClasses = testSuiteClasses.value();
        ArrayList<String> testSuites = new ArrayList<>();
        for(Class suiteName: allTestSuitesClasses){
            //System.out.println(suiteName.getPackageName());
            testSuites.add(suiteName.getName());
        }

        for(String testSuite: testSuites) {
            Suite.SuiteClasses suiteClasses = Class.forName(testSuite).getAnnotation(Suite.SuiteClasses.class);

            Class<?>[] classesInSuite = suiteClasses.value();

            for (Class className : classesInSuite) {

                String fileName = FILE_PREFIX + className.getName().replace(".", "/") + ".java";
                // fileName = src/main/java/com/example/demo/xyz.java

                try {
                    BufferedReader buf = new BufferedReader(new FileReader(fileName));
                    LineNumberReader rdr = new LineNumberReader(buf);


                    try {
                        String line;
                        while ((line = rdr.readLine()) != null) {

                            if (line.contains("@Test")) {
                                line = rdr.readLine(); // this line contains the method name
                                String authorMailString = findGitBlameForLine(fileName, rdr.getLineNumber());

                                if(!(authorMailString.contains("@"))) System.out.println("# Not Committed yet");
                                String authorName = getAuthorMailFromGitBlame(authorMailString);
                                String methodName = getMethodName(line);
                                authorMap.put(className.getName() + ". " + methodName, authorName); // ". " added explicitly

                                //System.out.println(authorName+"\n"+methodName);
                                //Mapping testname (MongoPersistentPropertyCacheTest. testMongoPersistentPropertyCache_index_created)
                                // to AuthorName.
                            }
                        }
                    } catch (IOException ee) {
                        ee.printStackTrace();
                    }
                    finally {
                        buf.close();
                        rdr.close();
                    }
                    //pr.waitFor(5, TimeUnit.SECONDS);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        List<String> list = printAuthorMap();
        StringBuilder table = new StringBuilder();
        for( String entry: list){
            table.append(entry).append(NEWLINE_SEPARATOR);
        }
        return table.toString();
    }

    private static String findGitBlameForLine(String fileName,int lineNumber) throws IOException {
        String[] blameCmd = {
                "/bin/sh",
                "-c",
                "git blame -L "+lineNumber+",+1 " +" --line-porcelain " + fileName + " | egrep \"author-mail\""
        };

        Process pr;
        String line = null;
        try {
            pr = Runtime.getRuntime().exec(blameCmd);
            BufferedReader buf  = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            while((line = buf.readLine())!=null){  // reading the result of git blame command
                break;
            }
            buf.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        if(line == null) line = "No_Line_Readed";
        return line;
    }

    private static String getAuthorMailFromGitBlame(String line){
        String[] temp = line.split(" ");
        line = temp[temp.length-1];

        StringBuilder builder1 = new StringBuilder(line);
        if(builder1.indexOf("@") != -1) {
            builder1.delete(builder1.indexOf("@"),builder1.length());
            builder1.delete(0,builder1.indexOf("<")+1);
        }
        //System.out.println("## "+builder1.toString());
        return builder1.toString();
    }
    private static String getMethodName(String line){
        // line is a method definition line ex. public String fun1(){
        // remove line after (
        StringBuilder builder = new StringBuilder(line);
        builder.delete(builder.indexOf("("),line.length());
        line = builder.toString();

        String[] tmp = line.split(" ");
        return tmp[tmp.length-1];
    }

    private static List<String> printAuthorMap(){
        int maxAuthorNameLength = 0;
        int maxTestNameLength = 0;

        for(Map.Entry entry : authorMap.entrySet()){
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

        StringBuilder horizontal = new StringBuilder(); // +----------------+------------+
        horizontal.append(INTERSECTION_CHAR).append(StringUtils.repeat(HORIZONTAL_SEPARATOR,maxTestNameLength+2)).
                append(INTERSECTION_CHAR).append(StringUtils.repeat(HORIZONTAL_SEPARATOR,maxAuthorNameLength+2)).append(INTERSECTION_CHAR);
        //System.out.println(horizontal);

        String[] headings = {"Test Name", "Author Name"};
        StringBuilder headers = getPaddedEntry(headings,maxTestNameLength,maxAuthorNameLength);

        List<String> mapTable = new ArrayList<>();
        mapTable.add(horizontal.toString());
        mapTable.add(headers.toString());
        mapTable.add(horizontal.toString());

        for(Map.Entry entry: authorMap.entrySet()){
            String[] entries = {entry.getKey().toString(),entry.getValue().toString()};
            StringBuilder padded = getPaddedEntry(entries,maxTestNameLength,maxAuthorNameLength);
            mapTable.add(padded.toString());
        }

        mapTable.add(horizontal.toString());

        return mapTable;
    }

    private static StringBuilder getPaddedEntry(String[] headings,Integer maxTestNameLength,Integer maxAuthorNameLength)
    {
        StringBuilder heading = new StringBuilder();
        heading.append(VERTICAL_SEPARATOR).append(PADDING).append(headings[0]).append(StringUtils.repeat(PADDING,maxTestNameLength-headings[0].length()+1))
                .append(VERTICAL_SEPARATOR).append(PADDING).append(headings[1]).append(StringUtils.repeat(PADDING,maxAuthorNameLength-headings[1].length()+1))
                .append(VERTICAL_SEPARATOR);
        return heading;
    }

}
