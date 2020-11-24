package org.kutsuki.scrapermanager.repository;

import org.kutsuki.scrapermanager.document.Position;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PortfolioRepository extends MongoRepository<Position, String> {
}
