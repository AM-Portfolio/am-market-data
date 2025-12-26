package com.am.marketdata.internal.repository;

import com.am.marketdata.internal.model.IngestionJobLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Ingestion Job Logs.
 * Accessed by MarketDataAdminController.
 */
@Repository
public interface IngestionJobLogRepository extends MongoRepository<IngestionJobLog, String> {
    java.util.Optional<IngestionJobLog> findByJobId(String jobId);
}
