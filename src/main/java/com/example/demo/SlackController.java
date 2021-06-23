package com.example.demo;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            //System.out.println("teamID : " + teamId + "\nuserId : " + userId + "\n teamName: " + channelName + "\n userName: " + userName);

            SlackResponse response = new SlackResponse();
            response.setText("Unit Testing Author-Test Map");
            response.setResponseType("in_channel");


            String mapTable = TestRunner.getAuthorMap();
            Attachment attachment = new Attachment();
            attachment.setText(mapTable);
            attachment.setColor("#0000ff");
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
