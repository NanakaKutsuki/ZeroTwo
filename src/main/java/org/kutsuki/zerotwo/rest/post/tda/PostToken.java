package org.kutsuki.zerotwo.rest.post.tda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class PostToken {
    private String access_token;
    private String refresh_token;
    private String token_type;
    private int expires_in;
    private String scope;
    private int refresh_token_expires_in;

    @Override
    public String toString() {
	String json = null;

	try {
	    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
	    json = ow.writeValueAsString(this);
	} catch (JsonProcessingException e) {
	    e.printStackTrace();
	}

	return json;
    }

    public String getAccess_token() {
	return access_token;
    }

    public String getRefresh_token() {
	return refresh_token;
    }

    public String getToken_type() {
	return token_type;
    }

    public int getExpires_in() {
	return expires_in;
    }

    public String getScope() {
	return scope;
    }

    public int getRefresh_token_expires_in() {
	return refresh_token_expires_in;
    }
}
