package com.am.marketdata.internal.repository;

import com.am.marketdata.internal.model.IngestionJobLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngestionJobLogRepository extends MongoRepository<IngestionJobLog, String> {
}
