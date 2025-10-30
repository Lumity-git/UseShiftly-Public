package com.useshiftly.scheduler.repository;

import com.useshiftly.scheduler.model.UserActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    List<UserActionLog> findByBuildingId(Long buildingId);
    List<UserActionLog> findByUserUuid(String userUuid);
    List<UserActionLog> findByRole(String role);
}
