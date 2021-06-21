package com.example.demo;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MyMapper {
    private String fileName;
    private String testDir;

    private HashMap<String,String> authorMapByMethods;

    public MyMapper(){

    }
    public MyMapper(String file, String dir){
        this.fileName = file;
        this.testDir = dir;
        authorMapByMethods = new HashMap<>();
    }


    public HashMap<String,String> getAuthorMap() throws IOException {
        System.out.println("Entered in getAuthor method");

        URL url = null;

        try {
            //InputStream in = new URL("https://github.com/Laxman-77/SlackDemo/blob/master/src/test/java/com/example/demo/Demo1ApplicationTests.java").openConnection().getInputStream();
            //LineNumberReader rdr = new LineNumberReader(new InputStreamReader(in));

            LineNumberReader rdr = new LineNumberReader(new FileReader(testDir+fileName));

            String line;
            //noinspection TryFinallyCanBeTryWithResources
            try {

                while ((line = rdr.readLine()) != null) {

                    System.out.println("line readed now : "+line);

                    if (line.contains("@Test")) { // Current line contains the keyword
                        line = rdr.readLine();
                        System.out.println("## "+line);
                        int lineNumber = rdr.getLineNumber();
                        String methodName = findMethodName(line);

                        Process pr;

                        // command for getting the authorMail of the line with specified lineNumber.
                        String[] cmd = {"/bin/sh", "-c", "git blame -L " + lineNumber + ",+1" + " --line-porcelain " + fileName + " | egrep -n \"author-mail\""};
                        ProcessBuilder builder = new ProcessBuilder(cmd);
                        builder = builder.directory(new File(testDir));

                        try {
                            System.out.println("Entered in TRY 3 !");
                            pr = builder.start();
                            InputStream is = pr.getInputStream();
                            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

                            String bufLine;
                            try {
                                while ((bufLine = buf.readLine()) != null) {
                                    String[] temp = bufLine.split(" ");
                                    String authorMail = temp[1];
                                    String authorName = findUsernameFromMail(authorMail);

                                    authorMapByMethods.put(methodName, authorName);
                                }
                            } catch (Exception e) {
                                System.out.println("Exception in while loop");
                            }
                        } catch (Exception e) {
                            System.out.println("Exception in buffered reader");
                            e.printStackTrace();
                        }
                    }
                }
            }
            catch (Exception e){
                System.out.println("Error in line reader");
            }
            finally {
                rdr.close();
            }

        }
        catch (Exception e){
            System.out.println("Exception in file reading");
            e.printStackTrace();
        }
        for(Map.Entry entry:authorMapByMethods.entrySet()){
            System.out.println(entry.getKey() +" : "+entry.getValue());
        }
        System.out.println("Finished ");

        return this.authorMapByMethods;
    }

    public static String findUsernameFromMail(String line){

        StringBuilder builder1 = new StringBuilder(line);
        builder1.delete(builder1.indexOf("@"),builder1.length());
        builder1.delete(0,builder1.indexOf("<")+1);
        //System.out.println(builder1.toString());
        return builder1.toString();
    }
    public static String findMethodName(String line){
        // line is a method definition line ex. public String fun1(){
        // remove line after (
        StringBuilder builder = new StringBuilder(line);
        builder.delete(builder.indexOf("("),line.length());
        line = builder.toString();

        String[] tmp = line.split(" ");
        return tmp[tmp.length-1];
    }
}
