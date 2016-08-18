package com.twilio;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

import java.util.HashMap;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.twilio.sdk.client.TwilioCapability;
import com.twilio.sdk.verbs.TwiMLResponse;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.Say;
import com.twilio.sdk.verbs.Dial;
import com.twilio.sdk.verbs.Number;
import com.twilio.sdk.verbs.Client;

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
      TwilioCapability capability = new TwilioCapability(acctSid, authToken);
      capability.allowClientOutgoing(applicationSid);
      capability.allowClientIncoming(identity);
      String token = capability.generateToken();
        
      // create JSON response payload 
      HashMap<String, String> json = new HashMap<String, String>();
      json.put("identity", identity);
      json.put("token", token);

      // Render JSON response
      response.header("Content-Type", "application/json"); 
      Gson gson = new Gson();
      return gson.toJson(json);
    });
    
    // Generate voice TwiML
    post("/voice", "application/x-www-form-urlencoded", (request, response) -> {
      TwiMLResponse twiml = new TwiMLResponse();
      try {
        String to = request.queryParams("To");
        if (to != null) {
          Dial dial = new Dial();
          dial.setCallerId("+15017250604");

          // wrap the phone number or client name in the appropriate TwiML verb
          // by checking if the number given has only digits and format symbols
          if(to.matches("^[\\d\\+\\-\\(\\) ]+$")) {
            dial.append(new Number(to));
          } else {
            dial.append(new Client(to));
          }

          twiml.append(dial);
        } else {
          twiml.append(new Say("Thanks for calling!"));
        }
      } catch (TwiMLException e) {
        e.printStackTrace();
      }

      response.header("Content-Type", "text/xml"); 
      return twiml.toXML();
    });
  }
}
