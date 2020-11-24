package org.kutsuki.scrapermanager.repository;

import org.kutsuki.scrapermanager.document.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlertRepository extends MongoRepository<Alert, String> {
}
