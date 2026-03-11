package com.engie.api.repository;

import com.engie.api.entity.ValidatieFoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidatieFoutRepository extends JpaRepository<ValidatieFoutEntity, Long> {

    List<ValidatieFoutEntity> findByBerichtId(String berichtId);

    List<ValidatieFoutEntity> findByFoutCode(String foutCode);

    List<ValidatieFoutEntity> findByFoutType(String foutType);
}
