package com.example.demo;


import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class GitBlame implements Callable<ArrayList<String>> {
    private String fileName;
    private long startTime;
    private long endTime;

    public GitBlame(String fileName, long startTime, long endTime) {
        this.fileName = fileName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public ArrayList<String> call() throws Exception {
        //Logging
        System.out.println("START: File " + this.fileName);

        // Runs grep -n to get line numbers
        //******//StopWatch started = StopWatch.createStarted();
        ArrayList<String> authors = getAuthorsForFile();

        //Logging
        //*****//System.out.println("END: File: " + this.fileName + ", time_taken: " + started.getTime() + "ms");
        return authors;
    }

    public ArrayList<String> getAuthorsForFile() throws IOException {
        ArrayList<String> authors = new ArrayList<>();

        Process pr;
        try {
            // File last commit check for early abort
            String[] logCmd = {
                    "/bin/sh",
                    "-c",
                    "git log -1 --format=%ct " + this.fileName
            };
            pr = Runtime.getRuntime().exec(logCmd);

            InputStream is = pr.getInputStream();
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

            try {
                int fileTimeStamp = Integer.parseInt(buf.readLine());
                if (fileTimeStamp < startTime) { // Irrelevant file => Early abort
                    return authors;
                }
            } finally {
                is.close();
                buf.close();
            }

            pr.waitFor(5, TimeUnit.SECONDS);
        } catch (Throwable throwable) {
            System.out.println("Exception while running git log for " + this.fileName);
            throwable.printStackTrace();
            return authors;
        }

        // Getting lineNumbers containing @Test in the given source file
        // Gives same performance as using the grep -n command
        LineNumberReader rdr = new LineNumberReader(new FileReader(this.fileName));
        ArrayList<Integer> lineNumbers = new ArrayList<>();
        String line;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            while ((line = rdr.readLine()) != null) {
                if (line.contains("@Test")) { // Current line contains the keyword
                    lineNumbers.add(rdr.getLineNumber());
                }
            }
        } finally {
            rdr.close();
        }

        try {
            // Runs git blame --line-porcelain for the given file and returns author names
            String[] blameCmd = {
                    "/bin/sh",
                    "-c",
                    "git blame --line-porcelain " + this.fileName + " | egrep \"author-mail|committer-time\" "
            };
            pr = Runtime.getRuntime().exec(blameCmd);

            InputStream is = pr.getInputStream();
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            int currLineNumber = 1;
            String authorLine;

            try {
                for (Integer testLineNumber : lineNumbers) {
                    while (currLineNumber < testLineNumber) {
                        buf.readLine();
                        buf.readLine();
                        currLineNumber += 1;
                    }

                    authorLine = buf.readLine();
                    // Checking if log lies in the given TimeFrame
                    int timeStamp = Integer.parseInt(buf.readLine().split(" ")[1]);
                    if (timeStamp >= this.startTime && timeStamp <= this.endTime) {
                        String authorPrintName = " ";//StringUtils.removeStart(removeEnd(authorLine.split(" ")[1], EMAIL_SUFFIX), EMAIL_PREFIX);
                        // ****************** Resolve upper line
                        authors.add(authorPrintName);
                    }

                    currLineNumber += 1;
                }
            } finally {
                is.close();
                buf.close();
            }

            pr.waitFor(5, TimeUnit.SECONDS);
        } catch (Throwable throwable) {
            System.out.println("Exception in git blame for " + this.fileName);
            throwable.printStackTrace();
        }

        return authors;
    }
}
