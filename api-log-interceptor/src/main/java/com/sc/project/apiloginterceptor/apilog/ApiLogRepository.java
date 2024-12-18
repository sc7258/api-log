package com.sc.project.apiloginterceptor.apilog;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

//    @Transactional
//    @Modifying
//    @Query("""
//                UPDATE ApiLog a
//                SET a.responseStatus = :status, a.response = :response, a.responseTime = current_timestamp
//                WHERE a.seq = :seq
//            """)
//    void updateResponse(Long seq, Integer status, String response);

    @Transactional
    @Modifying
    @Query("""
                UPDATE ApiLog a
                SET a.responseStatus = :status, a.response = :response, a.responseTime = current_timestamp
                WHERE a.traceId = :traceId
            """)
    void updateResponse(String traceId, Integer status, String response);

    @Transactional
    @Modifying
    @Query("""
                UPDATE ApiLog a
                SET a.request = :request, a.requestTime = current_timestamp
                WHERE a.traceId = :traceId
            """)
    void updateRequest(String traceId, String request);
}
