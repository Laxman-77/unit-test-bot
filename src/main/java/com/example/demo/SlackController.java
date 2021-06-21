package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;

@RestController
public class SlackController {
    @RequestMapping(value = "/slack",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public SlackResponse onReceiveSlashCommand(@RequestParam("team_id") String teamId,
                                        @RequestParam("team_domain") String teamDomain,
                                        @RequestParam("channel_id") String channelId,
                                        @RequestParam("channel_name") String channelName,
                                        @RequestParam("user_id") String userId,
                                        @RequestParam("user_name") String userName,
                                        @RequestParam("command") String command,
                                        @RequestParam("text") String text,
                                        @RequestParam("response_url") String responseUrl) throws IOException {
        try {
            System.out.println("teamID : " + teamId + "\nuserId : " + userId + "\n teamName: " + channelName + "\n userName: " + userName);

            SlackResponse response = new SlackResponse();
            response.setText("This is the response text");
            response.setResponseType("in_channel");

            String fileName = "README.md";
            String testDir = "/Users/laxmangoliya/Test/";

            //URL dirURL = new URL("https://github.com/Laxman-77/Test/blob/main/");
            //String testDir = "https://github.com/Laxman-77/Test/blob/main";

            System.out.println("Controller Starting");
            MyMapper mapper = new MyMapper(fileName, testDir);
            String myMap = mapper.getAuthorMap().toString();

            Attachment attachment = new Attachment();
            attachment.setText("This is attachment text\n"+
                    " teamID : " + teamId + "\n userId : " + userId
                    + "\n teamName: " + channelName + "\n userName: "
                    + userName + "\n");

            attachment.setColor("#0000ff");
            attachment.setMap(myMap);

            response.getAttachments().add(attachment);
            return response;
        }
        catch(Exception e){
            SlackResponse response = new SlackResponse();
            response.setText("Error Occurred In execution");
            response.setResponseType("in_channel");

            Attachment attachment = new Attachment();
            attachment.setColor("Error Attachment Text");
            attachment.setColor("#0000ff");

            System.out.println("We are In Exception.");
            response.getAttachments().add(attachment);
            return response;
        }
    }


    @RequestMapping("/")
    public SlackResponse home(){
        return new SlackResponse("This is home page. ");
    }
}
