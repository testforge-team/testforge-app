package com.testforge.email.repository;

import com.testforge.email.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    /** Newest log entries first, for the admin's reminder screen. */
    List<EmailLog> findAllByOrderByLogIdDesc();
}
