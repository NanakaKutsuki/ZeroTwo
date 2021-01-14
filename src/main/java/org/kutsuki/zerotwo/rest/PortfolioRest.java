package org.kutsuki.zerotwo.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.StringUtils;
import org.kutsuki.zerotwo.EmailService;
import org.kutsuki.zerotwo.document.Opening;
import org.kutsuki.zerotwo.portfolio.PortfolioManager;
import org.kutsuki.zerotwo.repository.OpeningRepository;
import org.kutsuki.zerotwo.rest.post.PostShadow;
import org.kutsuki.zerotwo.rest.post.tda.PostToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class PortfolioRest {
    private static final String SHADOW = "Shadow";
    private static final String GRANT_TYPE = "grant_type=refresh_token";
    private static final String REFRESH_TOKEN = "&refresh_token=";
    private static final String CLIENT_ID = "&access_type=&code=&client_id=";
    private static final String REDIRECT_URI = "&redirect_uri=";

    @Autowired
    private OpeningRepository alertRepository;

    @Autowired
    private PortfolioManager manager;

    @Autowired
    private EmailService service;

    @Value("${tda.refreshToken}")
    private String refreshToken;

    @Value("${tda.clientId}")
    private String clientId;

    private Opening alert;
    private PostToken token;
    private String tokenBody;

    @PostConstruct
    public void postConstruct() {
	reloadCache();

	try {
	    StringBuilder sb = new StringBuilder();
	    sb.append(GRANT_TYPE);
	    sb.append(REFRESH_TOKEN);
	    sb.append(URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.toString()));
	    sb.append(CLIENT_ID);
	    sb.append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.toString()));
	    sb.append(REDIRECT_URI);
	    this.tokenBody = sb.toString();
	} catch (UnsupportedEncodingException e) {
	    service.emailException("Error encoding for Token Body!", e);
	}
    }

    @GetMapping("/rest/portfolio/getLastChecked")
    public String getLastChecked() {
	return alert.getLastChecked();
    }

    @GetMapping("/rest/portfolio/getSymbols")
    public Set<String> getSymbols() {
	return manager.getSymbols();
    }

    @GetMapping("/rest/portfolio/reloadCache")
    public ResponseEntity<String> reloadCache() {
	this.alert = alertRepository.findByProject(SHADOW);
	manager.reloadCache();

	// return finished
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/portfolio/updateLastChecked")
    public ResponseEntity<String> updateLastChecked(@RequestParam(value = "id", required = true) String id) {
	alert.setLastChecked(id);
	alertRepository.save(alert);
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/portfolio/updateQty")
    public String updateQty(@RequestParam(value = "symbol", required = true) String symbol,
	    @RequestParam(value = "qty", required = true) String qty) {
	return manager.updateQty(symbol, qty);
    }

    @GetMapping("/rest/portfolio/updateTradeId")
    public String updateTradeId(@RequestParam(value = "symbol", required = true) String symbol,
	    @RequestParam(value = "id", required = true) String id) {
	return manager.updateTradeId(symbol, id);
    }

    @PostMapping("/rest/portfolio/uploadMessage")
    public ResponseEntity<String> uploadMessage(@RequestBody PostShadow postData) {
	if (!StringUtils.equals(postData.getId(), alert.getLastChecked())) {
	    manager.parseMessage(postData.getMessage(), postData.getImage());
	    updateLastChecked(postData.getId());
	}

	// return finished
	return ResponseEntity.ok().build();
    }

    public void refreshToken() {
	RestTemplate restTemplate = new RestTemplate();

	HttpHeaders headers = new HttpHeaders();
	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	HttpEntity<String> request = new HttpEntity<String>(tokenBody, headers);

	token = restTemplate.postForObject("https://api.tdameritrade.com/v1/oauth2/token", request, PostToken.class);
    }

    public void getAccount() {
	HttpHeaders headers = new HttpHeaders();
	headers.set("Authorization", "Bearer " + token.getAccess_token());

	HttpEntity<String> entity = new HttpEntity<String>(headers);
	RestTemplate restTemplate = new RestTemplate();
	ResponseEntity<String> response = restTemplate.exchange("https://api.tdameritrade.com/v1/accounts/232536976",
		HttpMethod.GET, entity, String.class);
	System.out.println(response.getStatusCodeValue());
	System.out.println(response.getBody());
    }
}