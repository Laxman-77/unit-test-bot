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
                                        @RequestParam("token") String token,
                                        @RequestParam("response_url") String responseUrl) throws IOException, ClassNotFoundException
    {

        try {
            SlackResponse response = new SlackResponse();

            response.setResponseType("in_channel");

            String mapTable = TestRunner.getAuthorMap();
            //response.setText("```" + "Unit Testing Test Author Map\n" + mapTable + " ```");
            System.out.println(mapTable);

            StringBuilder res = new StringBuilder();
            res.append("teamId :").append(teamId).append("\ntoken: ").append(token).append("channel : ")
                    .append(channelName);
            response.setText(res.toString());
            return response;
        }
        catch(IOException e){
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
