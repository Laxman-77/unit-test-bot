package com.example.demo;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.springframework.util.StreamUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.apache.commons.lang3.StringUtils.removeEnd;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CalculatorTestSuite1.class,
        CalculatorTestSuite2.class
})
public class TestRunner {
    private static final String testDir = "src/main/";
    private static final String FILE_PREFIX = testDir+"/java/";

    public static final String FIRST_HEADING = "Test Name ";
    public static final String TOTAL = "Total";
    public static final String TEST_SUITE_SUFFIX = "TestSuite";
    public static final String EMAIL_SUFFIX = "@sprinklr.com>";
    public static final String EMAIL_PREFIX = "<";

    public static final String NEWLINE_SEPARATOR = "%n";
    public static final String PADDING = " ";
    public static final String VERTICAL_SEPARATOR = "|";
    public static final String HORIZONTAL_SEPARATOR = "-";
    public static final String INTERSECTION_CHAR = "+";
    public static HashMap<String,String> authorMap;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        getAuthorMap();
        printAuthorMap();
    }
    public static HashMap<String,String> getAuthorMap() throws ClassNotFoundException, IOException {
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
            //System.out.println(classesInSuite.length);
            for (Class className : classesInSuite) {
                //System.out.println(className.getName());
                String fileName = FILE_PREFIX + className.getName().replace(".", "/") + ".java";
                // fileName = src/main/java/com/example/demo/xyz.java

                try {
                    //System.out.println(is.toString());
                    BufferedReader buf = new BufferedReader(new FileReader(fileName));
                    LineNumberReader rdr = new LineNumberReader(buf);

                    //System.out.println(buf.toString());

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
                        //System.out.println("gone");
                        buf.close();
                        rdr.close();
                    }
                    //pr.waitFor(5, TimeUnit.SECONDS);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        printAuthorMap();
        /*
        System.out.println("---------------------------AuthorMap-----------------------------");
        for(Map.Entry entry:authorMap.entrySet()){
            System.out.println(entry.getKey() +" | "+ entry.getValue());
        }
        System.out.println("------------------------------------------------------------------");
        /*
        for(Failure failure:result.getFailures()){
            System.out.println(failure.toString());
        }

        System.out.println(result.wasSuccessful());
        */
        return authorMap;
    }

    public static String findGitBlameForLine(String fileName,int lineNumber) throws IOException {
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
                //System.out.println(line);
                break;
            }
            buf.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        if(line == null) System.out.println("Git blame error");
        line = "noLineReadedFromGitBlame";
        return line;
    }

    public static String getAuthorMailFromGitBlame(String line){
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
    public static String getMethodName(String line){
        // line is a method definition line ex. public String fun1(){
        // remove line after (
        StringBuilder builder = new StringBuilder(line);
        builder.delete(builder.indexOf("("),line.length());
        line = builder.toString();

        String[] tmp = line.split(" ");
        return tmp[tmp.length-1];
    }

    public static void printAuthorMap(){
        int maxAuthorNameLength = 0;
        int maxTestNameLength = 0;

        for(Map.Entry entry : authorMap.entrySet()){
            maxAuthorNameLength = Math.max(maxAuthorNameLength,entry.getValue().toString().length());
            maxTestNameLength = Math.max(maxTestNameLength,entry.getKey().toString().length());
        }

        StringBuilder horizontal = new StringBuilder();
        horizontal.append(INTERSECTION_CHAR).append(StringUtils.repeat(HORIZONTAL_SEPARATOR,maxTestNameLength+2)).
                append(INTERSECTION_CHAR).append(StringUtils.repeat(HORIZONTAL_SEPARATOR,maxAuthorNameLength+2)).append(INTERSECTION_CHAR);
        //System.out.println(horizontal);

        String[] headings = {"Test Name", "Author Name"};
        StringBuilder headers = getPaddedEntry(headings,maxTestNameLength,maxAuthorNameLength);


        System.out.println(horizontal);
        System.out.println(headers);
        System.out.println(horizontal);
        for(Map.Entry entry: authorMap.entrySet()){
            String[] entries = {entry.getKey().toString(),entry.getValue().toString()};
            StringBuilder padded = getPaddedEntry(entries,maxTestNameLength,maxAuthorNameLength);
            System.out.println(padded);
            System.out.println(horizontal);
        }
        //System.out.println(horizontal);
    }

    public static StringBuilder getPaddedEntry(String[] headings,Integer maxTestNameLength,Integer maxAuthorNameLength)
    {
        StringBuilder heading = new StringBuilder();
        heading.append(VERTICAL_SEPARATOR).append(PADDING).append(headings[0]).append(StringUtils.repeat(PADDING,maxTestNameLength-headings[0].length()+1))
                .append(VERTICAL_SEPARATOR).append(PADDING).append(headings[1]).append(StringUtils.repeat(PADDING,maxAuthorNameLength-headings[1].length()+1))
                .append(VERTICAL_SEPARATOR);
        return heading;
    }

}
