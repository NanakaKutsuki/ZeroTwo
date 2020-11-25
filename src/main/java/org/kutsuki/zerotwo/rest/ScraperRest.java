package org.kutsuki.zerotwo.rest;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
public class ScraperRest {
    private static final String CHROME = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
    private static final String DEV_TOOLS = "--auto-open-devtools-for-tabs";
    private static final String INCOGNITO = "--incognito";
    private static final String TASK_KILL = "taskkill /F /IM chrome.exe /T";

    @Autowired
    private EmailService service;

    @Value("${scraper.heartbeat}")
    private String heartbeatLink;

    @Value("${scraper.hotel}")
    private String hotelLink;

    @Value("${scraper.shadow}")
    private String shadowLink;

    private boolean hotelOpen;
    private boolean tradingOpen;

    @PostConstruct
    public void postConstruct() {
	this.hotelOpen = false;
	this.tradingOpen = false;
    }

    @GetMapping("/rest/scraper/closeWindows")
    public void closeWindows() {
	try {
	    Runtime.getRuntime().exec(TASK_KILL);
	    hotelOpen = false;
	    tradingOpen = false;
	} catch (Exception e) {
	    service.emailException("Error closing Chrome!", e);
	}
    }

    @Scheduled(cron = "55 */10 0,11-23 * * *")
    public void closeWindowsIfBusy() {
	if (hotelOpen && StringUtils.isNotEmpty(httpGet(hotelLink))) {
	    closeWindows();
	}
    }

    @Scheduled(cron = "0 * * * * *")
    public void heartbeat() {
	httpGet(heartbeatLink);
    }

    @Scheduled(cron = "0 * 0,11-23 * * *")
    public void openHotelWindow() {
	if (!hotelOpen) {
	    String link = httpGet(hotelLink);

	    if (StringUtils.isNotEmpty(link)) {
		try {
		    Runtime.getRuntime().exec(new String[] { CHROME, INCOGNITO, DEV_TOOLS, link });
		    hotelOpen = true;
		} catch (Exception e) {
		    service.emailException("Error opening Hotel Window: " + link, e);
		}
	    }
	}
    }

    @Scheduled(cron = "0 * 9-18 * * MON-FRI")
    public void openTradingWindow() {
	if (!tradingOpen) {
	    try {
		Runtime.getRuntime().exec(new String[] { CHROME, DEV_TOOLS, shadowLink });
		tradingOpen = true;
	    } catch (Exception e) {
		service.emailException("Error opening Trading Window: " + shadowLink, e);
	    }
	}
    }

    private String httpGet(String link) {
	String response = null;

	try {
	    RestTemplate restTemplate = new RestTemplate();
	    response = restTemplate.getForObject(link, String.class);
	} catch (RestClientException e) {
	    service.emailException("Error with httpGet: " + link, e);
	}

	return response;
    }
}
