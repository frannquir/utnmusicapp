package com.musicspring.app.music_app.security.repository;

import com.musicspring.app.music_app.security.entity.PermitEntity;
import com.musicspring.app.music_app.security.enums.Permit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermitRepository extends JpaRepository<PermitEntity,Long> {
    Optional<PermitEntity> findByPermit(Permit permit);
}
