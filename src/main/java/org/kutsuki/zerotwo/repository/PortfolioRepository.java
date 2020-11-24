package org.kutsuki.zerotwo.repository;

import org.kutsuki.zerotwo.document.Position;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PortfolioRepository extends MongoRepository<Position, String> {
}
