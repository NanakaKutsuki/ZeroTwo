package org.kutsuki.zerotwo.repository;

import org.kutsuki.zerotwo.document.TdaPosition;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TdaPositionRepository extends MongoRepository<TdaPosition, String> {
}
