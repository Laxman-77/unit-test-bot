package com.example.demo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author laxman.goliya
 * @date 25/06/2021
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CalculatorTestSuite1.class,
        CalculatorTestSuite2.class
})
public class TestRunner {
    private static final String testDir = "demo1/src/main/";
    private static final String FILE_PREFIX = testDir+"/java/";

    public static HashMap<String,String> getAuthorMap() throws ClassNotFoundException, IOException {
        Class currentClass = new Object(){}.getClass().getEnclosingClass(); // TestRunner.class
        //Result result = JUnitCore.runClasses(currentClass);

        HashMap<String,String > authorMap = new HashMap<>(); // map for @Test method() --> AuthorName

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
                                authorMap.put(className.getSimpleName() + ". " + methodName, authorName); // ". " added explicitly

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

        return authorMap;

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
        if(line == null) line = "No_Line_Readed"; // if no line readed from git blame,then line == null
        return line;
    }

    private static String getAuthorMailFromGitBlame(String line){
        // line as "author_mail <laxman.goliya@sprinklr.com>"
        // we have to extract laxman.goliya from it.

        StringBuilder builder1 = new StringBuilder(line);
        if(builder1.indexOf("@") != -1) {
            builder1.delete(builder1.indexOf("@"),builder1.length()); // remove after @
            builder1.delete(0,builder1.indexOf("<")+1); // remove before <
        }

        return builder1.toString(); // laxman.goliya
    }
    private static String getMethodName(String line){
        // line is a method definition line ex. public String fun1(){
        // remove line after (
        StringBuilder builder = new StringBuilder(line);
        builder.delete(builder.indexOf("("),line.length());
        line = builder.toString();

        String[] tmp = line.split(" "); // tmp = {"public","String","fun1"}
        return tmp[tmp.length-1]; // fun1
    }


}
