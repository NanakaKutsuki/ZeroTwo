package org.kutsuki.zerotwo.repository;

import org.kutsuki.zerotwo.document.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {
    public Account findByProject(String project);
}
