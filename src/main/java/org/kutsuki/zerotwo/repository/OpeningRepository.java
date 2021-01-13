package org.kutsuki.zerotwo.repository;

import org.kutsuki.zerotwo.document.Opening;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OpeningRepository extends MongoRepository<Opening, String> {
    public Opening findByProject(String project);
}
