package com.example.demo;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TestRunner {
    public static void main(String[] args) throws ClassNotFoundException {
        Result result = JUnitCore.runClasses(CalculatorTestSuite1.class,CalculatorTestSuite2.class);

        HashMap<String,String> authorMap = new HashMap<>(); // map for @Test method() --> AuthorName

        String testDir = "src/test/";
        Suite.SuiteClasses suiteClasses = Class.forName("com.example.demo.CalculatorTestSuite1").getAnnotation(Suite.SuiteClasses.class);

        Class<?>[] classesInSuite = suiteClasses.value();
        System.out.println(classesInSuite.length);
        for(Class className: classesInSuite){
            System.out.println(className.getName());
            String fileName = testDir +"java/"+ className.getName().replace(".","/") +".java";

            System.out.println(fileName);

            try{
                //System.out.println(is.toString());
                BufferedReader buf = new BufferedReader(new FileReader(fileName));
                LineNumberReader rdr = new LineNumberReader(buf);

                System.out.println(buf.toString());
                try{
                    String line;
                    while((line = rdr.readLine())!=null){
                        //System.out.println(line);
                        if(line.contains("@Test")){
                            line = rdr.readLine();
                            System.out.println(line);
                            String authorMailString = findGitBlameForLine(fileName, rdr.getLineNumber());
                            System.out.println(authorMailString);

                            String authorName = getAuthorMailFromGitBlame(authorMailString);
                            String methodName = getMethodName(line);
                            authorMap.put(className.getName()+". "+methodName,authorName); // ". " added explicitly

                            //Mapping testname (MongoPersistentPropertyCacheTest. testMongoPersistentPropertyCache_index_created)
                            // to AuthorName.
                        }
                    }
                }
                catch (Exception ee){
                    ee.printStackTrace();
                }
                finally {
                    System.out.println("gone");
                    buf.close();
                    rdr.close();
                }
                //pr.waitFor(5, TimeUnit.SECONDS);
            }
            catch (Throwable throwable){
                throwable.printStackTrace();
            }
            for(Method method:className.getMethods()){
                // System.out.println(method.getName());
            }
        }

        System.out.println("------------------AuthorMap-------------------");
        for(Map.Entry entry:authorMap.entrySet()){
            System.out.println(entry.getKey() +" | "+ entry.getValue());
        }
        System.out.println("----------------------------------------------");
        for(Failure failure:result.getFailures()){
            System.out.println(failure.toString());
        }

        System.out.println(result.wasSuccessful());
    }

    public static String findGitBlameForLine(String fileName,int lineNumber) throws IOException {
        String[] blameCmd = {
                "/bin/sh",
                "-c",
                "git blame -L "+lineNumber+",+1 " +" --line-porcelain " + fileName + " | egrep \"author-mail\""
        };

        Process pr;
        String line = "No line readed.";
        try {
            pr = Runtime.getRuntime().exec(blameCmd);
            BufferedReader buf  = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            while((line = buf.readLine())!=null){
                //System.out.println(line);
                break;
            }
            buf.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return line;
    }

    public static String getAuthorMailFromGitBlame(String line){
        String[] temp = line.split(" ");
        line = temp[temp.length-1];

        StringBuilder builder1 = new StringBuilder(line);
        System.out.println("BB "+builder1.toString());
        if(builder1.indexOf("@") != -1) {
            builder1.delete(builder1.indexOf("@"),builder1.length());
            builder1.delete(0,builder1.indexOf("<")+1);
        }
        System.out.println(builder1.toString());
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
}
