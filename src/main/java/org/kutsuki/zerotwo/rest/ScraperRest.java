package org.kutsuki.zerotwo.rest;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ScraperRest extends AbstractChrome {
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
	    taskKillChrome();
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

    @Scheduled(cron = "*/10 * 0,11-23 * * *")
    public void openHotelWindow() {
	if (!hotelOpen) {
	    String link = httpGet(hotelLink);

	    if (StringUtils.isNotEmpty(link)) {
		try {
		    openIngcognitoChrome(link);
		    hotelOpen = true;
		} catch (IOException e) {
		    service.emailException("Error opening Hotel Window: " + link, e);
		}
	    }
	}
    }

    @Scheduled(cron = "*/10 * 9-18 * * MON-FRI")
    public void openTradingWindow() {
	if (!tradingOpen) {
	    try {
		openChrome(shadowLink);
		tradingOpen = true;
	    } catch (IOException e) {
		service.emailException("Error opening Trading Window: " + shadowLink, e);
	    }
	}
    }

    private String httpGet(String link) {
	RestTemplate restTemplate = new RestTemplate();
	return restTemplate.getForObject(link, String.class);
    }
}
