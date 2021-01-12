package org.kutsuki.zerotwo.rest.openings;

import java.io.IOException;

import org.kutsuki.zerotwo.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EitoRest {
    private static final String EITO = "EITO";

    @Autowired
    private EmailService service;

    @Autowired
    private EitoOpenings eito;

    @GetMapping("/rest/openings/parseEito")
    public ResponseEntity<String> parseEitoOpenings(@RequestParam("filename") String filename) {
	try {
	    eito.parseVacancies(filename);

	    // update last known filename
//	    Opening vacancy = repository.findByProject(EITO);
//	    vacancy.setLastChecked(filename);
//	    repository.save(vacancy);
	} catch (IOException e) {
	    service.emailException("Error parsing EITO Vacancies: " + filename, e);
	}

	return ResponseEntity.ok().build();
    }

}
