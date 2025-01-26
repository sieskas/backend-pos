package com.rotules.backend.outcall.db.repository;

import com.rotules.backend.domain.Location;
import com.rotules.backend.domain.LocationTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository  extends JpaRepository<Location, Long> {
    List<Location> findByParentIsNull();
    List<Location> findByType_Name(LocationTypeEnum type);
}
