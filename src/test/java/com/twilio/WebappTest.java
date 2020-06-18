package com.twilio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class WebappTest {
    @Test
    void testDoCreateJsonAccessTokenReturnValidString() {
        String token = Webapp.createJsonAccessToken("nezuko");
        assertTrue(token.contains("\"identity\":\"nezuko\""));
        assertTrue(token.contains("\"token\":"));
    }

    @Test
    void testVoiceResponseHasToParameter() {
        String response = Webapp.createVoiceResponse("+5939999999");
        assertTrue(response.contains("<Number>+5939999999</Number>"));
    }

    @Test
    void testVoiceResponseHasNoParameters() {
        String response = Webapp.createVoiceResponse(null);
        assertTrue(response.contains("<Say>Thanks for calling!</Say>"));
    }
}
