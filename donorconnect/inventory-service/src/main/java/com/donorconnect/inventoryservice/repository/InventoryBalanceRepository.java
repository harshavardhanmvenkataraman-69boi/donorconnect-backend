package com.donorconnect.inventoryservice.repository;

import com.donorconnect.inventoryservice.entity.InventoryBalance;
import com.donorconnect.inventoryservice.enums.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Long> {

    Optional<InventoryBalance> findByComponentId(Long componentId);

    List<InventoryBalance> findByBloodGroup(BloodGroup bloodGroup);

    List<InventoryBalance> findByBloodGroupAndRhFactor(BloodGroup bloodGroup, RhFactor rhFactor);

    List<InventoryBalance> findByBloodGroupAndComponentTypeAndStatus(
            BloodGroup bloodGroup, ComponentType componentType, InventoryStatus status);

    List<InventoryBalance> findByStatus(InventoryStatus status);

    List<InventoryBalance> findByBloodGroupAndRhFactorAndComponentTypeAndStatus(
            BloodGroup bloodGroup, RhFactor rhFactor, ComponentType type, InventoryStatus status);

    /** Low stock: AVAILABLE components where quantity <= threshold */
    @Query("SELECT i FROM InventoryBalance i WHERE i.status = 'AVAILABLE' AND i.quantity <= :threshold")
    List<InventoryBalance> findLowStock(@Param("threshold") int threshold);

    /** Summary grid query: group by bloodGroup + componentType */
    @Query("SELECT i.bloodGroup, i.componentType, SUM(i.quantity) FROM InventoryBalance i " +
           "WHERE i.status = 'AVAILABLE' GROUP BY i.bloodGroup, i.componentType")
    List<Object[]> getSummaryGrid();

    boolean existsByComponentId(Long componentId);
}