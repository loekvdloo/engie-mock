package com.engie.api.repository;

import com.engie.api.entity.BerichtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BerichtRepository extends JpaRepository<BerichtEntity, String> {

    Optional<BerichtEntity> findByMessageId(String messageId);

    boolean existsByMessageId(String messageId);

    List<BerichtEntity> findAllByOrderByOntvangstTijdDesc();

    List<BerichtEntity> findBySenderEan(String senderEan);

    List<BerichtEntity> findByStatus(String status);

    @Query("SELECT COUNT(b) FROM BerichtEntity b WHERE b.status = :status")
    long countByStatus(String status);
}
