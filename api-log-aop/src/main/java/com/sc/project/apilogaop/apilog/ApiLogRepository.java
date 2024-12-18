package com.sc.project.apilogaop.apilog;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

    @Transactional
    @Modifying
    @Query("""
                UPDATE ApiLog a
                SET a.responseStatus = :status, a.response = :response, a.responseTime = current_timestamp
                WHERE a.seq = :seq
            """)
    void updateResponse(Long seq, Integer status, String response);
}
