package org.kutsuki.zerotwo.repository;

import org.kutsuki.zerotwo.document.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlertRepository extends MongoRepository<Alert, String> {
}
