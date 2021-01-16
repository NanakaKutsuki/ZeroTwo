package org.kutsuki.zerotwo.portfolio;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TokenHelper {
    private static final String GRANT_TYPE = "grant_type=refresh_token";
    private static final String REFRESH_TOKEN = "&refresh_token=";
    private static final String CLIENT_ID = "&access_type=&code=&client_id=";
    private static final String REDIRECT_URI = "&redirect_uri=";

    public String getToken(String refreshToken, String clientId) {
	StringBuilder sb = new StringBuilder();

	try {
	    sb.append(GRANT_TYPE);
	    sb.append(REFRESH_TOKEN);
	    sb.append(URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.toString()));
	    sb.append(CLIENT_ID);
	    sb.append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.toString()));
	    sb.append(REDIRECT_URI);
	} catch (UnsupportedEncodingException e) {
	    // do nothing
	}

	return sb.toString();
    }
}
