package com.donorconnect.bloodsupplyservice.repository;
import com.donorconnect.bloodsupplyservice.entity.InventoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Long> {
    Optional<InventoryBalance> findByComponentId(Long componentId);
    List<InventoryBalance> findByBloodGroupAndRhFactor(String bg, String rh);
}
