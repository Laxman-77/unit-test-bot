package com.example.demo;


import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@RestController
public class SlackController {
    private static final Set<String> allowedChannels = Set.of("unit-test-bot","paid-backend","random");
    private static final Set<String> allowedDomains = Set.of("unit-test-bot","sprinklr");

    @RequestMapping(value = "/getAllFailures",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public SlackResponse onReceiveGetAllFailures(@RequestParam("team_id") String teamId,
                                               @RequestParam("team_domain") String teamDomain,
                                               @RequestParam("channel_id") String channelId,
                                               @RequestParam("channel_name") String channelName,
                                               @RequestParam("user_id") String userId,
                                               @RequestParam("user_name") String userName,
                                               @RequestParam("command") String command,
                                               @RequestParam("text") String text,
                                               @RequestParam("token") String token,
                                               @RequestParam("response_url") String responseUrl) throws IOException, ClassNotFoundException
    {

        if(!allowedDomains.contains(teamDomain)) return new SlackResponse("Your teamDomain is not authorized to use this bot.");
        if(!allowedChannels.contains(channelName)) return new SlackResponse("This channel is not authorized to use this bot.");
        try {
            SlackResponse response = RequestHandler.getAllFailures();
            return response;
        }
        catch(Exception e){
            e.printStackTrace();
            SlackResponse response = new SlackResponse();
            response.setResponseType("ephemeral");
            response.setText("Error occurred in execution");

            return response;
        }
    }

    @RequestMapping(value = "/getFailuresByAuthor",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public SlackResponse onReceiveGetFailuresByAuthor(@RequestParam("team_id") String teamId,
                                               @RequestParam("team_domain") String teamDomain,
                                               @RequestParam("channel_id") String channelId,
                                               @RequestParam("channel_name") String channelName,
                                               @RequestParam("user_id") String userId,
                                               @RequestParam("user_name") String userName,
                                               @RequestParam("command") String command,
                                               @RequestParam("text") String text,
                                               @RequestParam("token") String token,
                                               @RequestParam("response_url") String responseUrl) throws IOException, ClassNotFoundException
    {

        if(!allowedDomains.contains(teamDomain)) return new SlackResponse("Your teamDomain is not authorized to use this bot.");
        if(!allowedChannels.contains(channelName)) return new SlackResponse("This channel is not authorized to use this bot.");
        try {
            SlackResponse response = RequestHandler.getFailuresByAuthor(text);
            return response;
        }
        catch(Exception e){
            e.printStackTrace();
            SlackResponse response = new SlackResponse();
            response.setResponseType("ephemeral");
            response.setText("Error occurred in execution");

            return response;
        }
    }



    @RequestMapping("/")
    public SlackResponse home(){
        return new SlackResponse("This is home page. ");
    }
}
