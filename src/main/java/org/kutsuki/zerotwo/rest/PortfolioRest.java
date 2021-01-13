package org.kutsuki.zerotwo.rest;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.StringUtils;
import org.kutsuki.zerotwo.document.Opening;
import org.kutsuki.zerotwo.portfolio.PortfolioManager;
import org.kutsuki.zerotwo.repository.OpeningRepository;
import org.kutsuki.zerotwo.rest.post.PostShadow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioRest {
    private static final String SHADOW = "Shadow";

    @Autowired
    private OpeningRepository alertRepository;

    @Autowired
    private PortfolioManager manager;

    private Opening alert;

    @PostConstruct
    public void postConstruct() {
	reloadCache();
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
}