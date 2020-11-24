package org.kutsuki.zerotwo.rest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.EmailService;
import org.kutsuki.zerotwo.document.Alert;
import org.kutsuki.zerotwo.portfolio.PortfolioManager;
import org.kutsuki.zerotwo.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioRest {
    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private EmailService service;

    @Autowired
    private PortfolioManager manager;

    private Alert lastAlert;

    @PostConstruct
    public void postConstruct() {
	this.lastAlert = new Alert();
	reloadCache();
    }

    @GetMapping("/rest/portfolio/getLastAlertId")
    public String getLastAlertId() {
	return lastAlert.getAlertId();
    }

    @GetMapping("/rest/portfolio/getSymbols")
    public Set<String> getSymbols() {
	return manager.getSymbols();
    }

    @GetMapping("/rest/portfolio/reloadCache")
    public ResponseEntity<String> reloadCache() {
	if (alertRepository.count() > 0) {
	    this.lastAlert = alertRepository.findAll().get(0);
	}

	manager.reloadCache();

	// return finished
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/portfolio/updateAlertId")
    public ResponseEntity<String> updateAlertId(@RequestParam("id") String id) {
	lastAlert.setAlertId(id);
	alertRepository.save(lastAlert);
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/portfolio/updateQty")
    public String updateQty(@RequestParam("symbol") String symbol, @RequestParam("qty") String qty) {
	return manager.updateQty(symbol, qty);
    }

    @GetMapping("/rest/portfolio/uploadAlert")
    public ResponseEntity<String> uploadAlert(@RequestParam("id") String id, @RequestParam("alert") String uriAlert) {
	if (!StringUtils.equalsIgnoreCase(id, lastAlert.getAlertId())) {
	    try {
		// decode URI Alert
		String escaped = URLDecoder.decode(uriAlert, StandardCharsets.UTF_8.toString());
		manager.parseAlert(escaped);

		// update alert id
		updateAlertId(id);
	    } catch (Exception e) {
		service.emailException(uriAlert, e);
	    }
	}

	// return finished
	return ResponseEntity.ok().build();
    }
}