package org.kutsuki.zerotwo.repository;

import org.kutsuki.zerotwo.document.Skip;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SkipRepository extends MongoRepository<Skip, String> {
    public Skip findByTradeId(int tradeId);
}
