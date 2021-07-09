package com.example.demo.test;

import org.joda.time.DateTime;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author laxman.goliya
 * @date 25/06/2021
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CalculatorTestSuite1.class,
        CalculatorTestSuite2.class
})
public class TestRunnerWithTimeFrame {
    private static final String testDir = "src/main";
    private static final String FILE_PREFIX = testDir+"/java/";
    private static HashMap<String, String> fullClassName = new HashMap<>();

    public static long startTime, endTime;
    public static void timeFrameSetup(String timeFrame) {
        switch (timeFrame) {
            case "LastWeek": {
                LocalDate localDate = LocalDate.now();
                LocalDateTime time =
                        localDate.atStartOfDay().with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                startTime = time.toEpochSecond(ZoneOffset.UTC);

                localDate = LocalDate.now();
                time = localDate.atStartOfDay().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endTime = time.toEpochSecond(ZoneOffset.UTC);
                break;
            }
            case "ThisWeek": {
                LocalDate localDate = LocalDate.now();
                LocalDateTime time = localDate.atStartOfDay().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                startTime = time.toEpochSecond(ZoneOffset.UTC);

                endTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                break;
            }
            case "LastSevenDays":
                startTime = new DateTime().withTimeAtStartOfDay().minusDays(6).getMillis() / 1000;
                endTime = new DateTime().getMillis() / 1000;
                break;
            case "ThisMonth": {
                LocalDate localDate = LocalDate.now();
                LocalDateTime time = localDate.atStartOfDay().with(TemporalAdjusters.firstDayOfMonth());
                startTime = time.toEpochSecond(ZoneOffset.UTC);

                endTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                break;
            }

            default:
                return;
        }
    }

    public static HashMap<String,String> getAuthorMap() throws ClassNotFoundException, IOException {
        Class currentClass = new Object(){}.getClass().getEnclosingClass(); // TestRunnerWithTimeFrame.class
        //Result result = JUnitCore.runClasses(currentClass);

        HashMap<String,String > authorMap = new HashMap<>(); // map for @Test method() --> AuthorName
        fullClassName = new HashMap<>(); // map for class.simpleName to class.fullName
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

                fullClassName.put(className.getSimpleName(),className.getName());
                String fileName = FILE_PREFIX + className.getName().replace(".", "/") + ".java";
                // fileName = src/main/java/com/example/demo/xyz.java

                try {
                    BufferedReader buf = new BufferedReader(new FileReader(fileName));
                    LineNumberReader rdr = new LineNumberReader(buf);

                    long fileTimeStamp = getFileTimeStamp(fileName);
                    if( fileTimeStamp < startTime) continue;
                    try {
                        String line;
                        while ((line = rdr.readLine()) != null) {

                            if (line.contains("@Test")) {
                                line = rdr.readLine(); // this line contains the method name
                                String gitBlameForLine = findGitBlameForLine(fileName, rdr.getLineNumber());

                                if(gitBlameForLine.split(" ").length<=1) continue;
                                String authorMailString = gitBlameForLine.split(" ")[0];
                                long lastCommitTime = Integer.parseInt(gitBlameForLine.split(" ")[1]);

                                if(lastCommitTime < startTime || lastCommitTime > endTime) continue;
                                //if(!(authorMailString.contains("@"))) System.out.println("# Not Committed yet");
                                String authorName = authorMailString;//getAuthorMailFromGitBlame(authorMailString);
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

    public static HashMap<String,String> getFullClassName(){
        return fullClassName;
    }



    /******************************   PRIVATE METHODS       ******************************/

    private static long getFileTimeStamp(String fileName){
        long fileTimeStamp = 0;
        Process pr;
        try {
            // File last commit check for early abort
            String[] logCmd = {
                    "/bin/sh",
                    "-c",
                    "git log -1 --format=%ct " + fileName
            };
            pr = Runtime.getRuntime().exec(logCmd);

            InputStream is = pr.getInputStream();
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

            try {
                fileTimeStamp = Integer.parseInt(buf.readLine());

            } finally {
                is.close();
                buf.close();
            }

            //pr.waitFor(5, TimeUnit.SECONDS);
        } catch (Throwable throwable) {
            System.out.println("Exception while running git log for " + fileName);
            throwable.printStackTrace();
        }
        return fileTimeStamp;
    }

    private static String findGitBlameForLine(String fileName,int lineNumber) throws IOException {
        String[] blameCmd = {
                "/bin/sh",
                "-c",
                "git blame -L "+lineNumber+",+1 " +" --line-porcelain " + fileName + " | egrep \"author-mail|committer-time\"",
        };


        Process pr;
        String line = null;
        String ret = "";
        try {
            pr = Runtime.getRuntime().exec(blameCmd);
            BufferedReader buf  = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            while((line = buf.readLine())!=null){  // reading the result of git blame command
                String commit = buf.readLine();
                if(commit == null) return "NIL";
                String authorMail = getAuthorMailFromGitBlame(line);
                long committerTime = Integer.parseInt(commit.split(" ")[1]);
                ret+=authorMail+" "+committerTime;
                break;
            }
            buf.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        if(line == null) line = "NIL"; // if no line readed from git blame,then line == null
        else line = ret;
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
