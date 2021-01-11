package org.kutsuki.zerotwo.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.StringUtils;
import org.kutsuki.zerotwo.EmailService;
import org.kutsuki.zerotwo.document.Opening;
import org.kutsuki.zerotwo.portfolio.PortfolioManager;
import org.kutsuki.zerotwo.repository.OpeningsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioRest {
    private static final String SHADOW = "Shadow";

    @Autowired
    private OpeningsRepository alertRepository;

    @Autowired
    private EmailService service;

    @Autowired
    private PortfolioManager manager;

    private Opening alert;

    @PostConstruct
    public void postConstruct() {
	reloadCache();
    }

    @GetMapping("/rest/portfolio/getAlertId")
    public Integer getAlertId() {
	return Integer.parseInt(alert.getLastChecked());
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

    @GetMapping("/rest/portfolio/updateAlertId")
    public ResponseEntity<String> updateAlertId(@RequestParam("id") String id) {
	alert.setLastChecked(id);
	alertRepository.save(alert);
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/portfolio/updateQty")
    public String updateQty(@RequestParam("symbol") String symbol, @RequestParam("qty") String qty) {
	return manager.updateQty(symbol, qty);
    }

    @GetMapping("/rest/portfolio/updateTradeId")
    public String updateTradeId(@RequestParam("symbol") String symbol, @RequestParam("id") String id) {
	return manager.updateTradeId(symbol, id);
    }

    @GetMapping("/rest/portfolio/uploadAlert")
    public ResponseEntity<String> uploadAlert(@RequestParam("id") String id, @RequestParam("alert") String uriAlert) {
	if (StringUtils.equals(id, alert.getLastChecked())) {
	    try {
		// decode URI Alert
		String escaped = URLDecoder.decode(uriAlert, StandardCharsets.UTF_8.toString());
		manager.parseAlert(escaped);

		// update alert id
		updateAlertId(id);
	    } catch (UnsupportedEncodingException e) {
		service.emailException(uriAlert, e);
	    }
	}

	// return finished
	return ResponseEntity.ok().build();
    }
}