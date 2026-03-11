package com.engie.api.repository;

import com.engie.api.entity.BerichtLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BerichtLogRepository extends JpaRepository<BerichtLogEntity, Long> {

    List<BerichtLogEntity> findByBerichtIdOrderByTijdstipDesc(String berichtId);

    List<BerichtLogEntity> findByActie(String actie);
}
