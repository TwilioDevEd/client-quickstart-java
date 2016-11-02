package com.twilio;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.github.javafaker.Faker;
import com.google.gson.Gson;

// Token generation imports
import com.twilio.jwt.Jwt;
import com.twilio.jwt.client.ClientCapability;
import com.twilio.jwt.client.IncomingClientScope;
import com.twilio.jwt.client.OutgoingClientScope;
import com.twilio.jwt.client.Scope;

// TwiML generation imports
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.Dial;
import com.twilio.twiml.Number;
import com.twilio.twiml.Client;
import com.twilio.twiml.Say;

public class Webapp {
  
  public static void main(String[] args) {
    // Serve static files from src/main/resources/public
    staticFileLocation("/public");
    
    // Create a Faker instance to generate a random username for the connecting user
    Faker faker = new Faker();
    
    // Create a capability token using our Twilio credentials
    get("/token", "application/json", (request, response) -> {
      String acctSid = System.getenv("TWILIO_ACCOUNT_SID");
      String authToken = System.getenv("TWILIO_AUTH_TOKEN");
      String applicationSid = System.getenv("TWILIO_TWIML_APP_SID");
      // Generate a random username for the connecting client
      String identity = faker.firstName() + faker.lastName() + faker.zipCode();
      
      // Generate capability token
      List<Scope> scopes = new ArrayList<>();
      scopes.add(new IncomingClientScope(identity));
      scopes.add(new OutgoingClientScope.Builder(applicationSid).build());
      Jwt jwt = new ClientCapability.Builder(acctSid, authToken).scopes(scopes).build();
      String token = jwt.toJwt();
        
      // create JSON response payload 
      HashMap<String, String> json = new HashMap<>();
      json.put("identity", identity);
      json.put("token", token);

      // Render JSON response
      response.header("Content-Type", "application/json"); 
      Gson gson = new Gson();
      return gson.toJson(json);
    });
    
    // Generate voice TwiML
    post("/voice", "application/x-www-form-urlencoded", (request, response) -> {
      VoiceResponse voiceTwimlResponse;
      String to = request.queryParams("To");
      if (to != null) {
        Dial.Builder dialBuilder = new Dial.Builder()
          .callerId(System.getenv("TWILIO_CALLER_ID"));

        // wrap the phone number or client name in the appropriate TwiML verb
        // by checking if the number given has only digits and format symbols
        if(to.matches("^[\\d\\+\\-\\(\\) ]+$")) {
          dialBuilder = dialBuilder.number(new Number.Builder(to).build());
        } else {
          dialBuilder = dialBuilder.client(new Client.Builder(to).build());
        }

        voiceTwimlResponse = new VoiceResponse.Builder()
          .dial(dialBuilder.build())
          .build();
      } else {
        voiceTwimlResponse = new VoiceResponse.Builder()
          .say(new Say.Builder("Thanks for calling!").build())
          .build();
      }

      response.header("Content-Type", "text/xml"); 
      return voiceTwimlResponse.toXml();
    });
  }
}
