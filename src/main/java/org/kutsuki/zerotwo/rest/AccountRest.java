package org.kutsuki.zerotwo.rest;

import org.kutsuki.zerotwo.document.Account;
import org.kutsuki.zerotwo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountRest {
    @Autowired
    private AccountRepository repository;

    @GetMapping("/rest/account/getAccount")
    public Account getAccount(@RequestParam(value = "project", required = true) String project) {
	return repository.findByProject(project);
    }
}
