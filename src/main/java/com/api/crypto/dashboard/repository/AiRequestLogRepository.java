package com.api.crypto.dashboard.repository;

import com.api.crypto.dashboard.model.AiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {
}
