package org.kutsuki.zerotwo.rest;

import java.io.IOException;

import org.kutsuki.zerotwo.EmailService;
import org.kutsuki.zerotwo.document.Opening;
import org.kutsuki.zerotwo.openings.EitoOpenings;
import org.kutsuki.zerotwo.openings.PlatinumReefOpenings;
import org.kutsuki.zerotwo.repository.OpeningsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpeningsRest {
    private static final String EITO = "EITO";

    @Autowired
    private EmailService service;

    @Autowired
    private EitoOpenings eito;

    @Autowired
    private PlatinumReefOpenings platinumReef;

    @Autowired
    private OpeningsRepository repository;

    @GetMapping("/rest/openings/getLastChecked")
    public String getLastChecked(@RequestParam(value = "project", required = true) String project) {
	Opening v = repository.findByProject(project);
	return v.getLastChecked();
    }

    @GetMapping("/rest/openings/clearPlatinumReef")
    public ResponseEntity<String> clearPlatinumReef() {
	platinumReef.clear();
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/openings/parseEito")
    public ResponseEntity<String> parseEitoOpenings(@RequestParam("filename") String filename) {
	try {
	    eito.parseVacancies(filename);

	    // update last known filename
	    Opening vacancy = repository.findByProject(EITO);
	    vacancy.setLastChecked(filename);
	    repository.save(vacancy);
	} catch (IOException e) {
	    service.emailException("Error parsing EITO Vacancies: " + filename, e);
	}

	return ResponseEntity.ok().build();
    }

    @PostMapping("rest/openings/addPlatinumReef")
    public ResponseEntity<String> addPlatinumReef(@RequestBody PostBody body) {
	platinumReef.addOpening(body.getId(), body.getBody());
	return ResponseEntity.ok().build();
    }
}
