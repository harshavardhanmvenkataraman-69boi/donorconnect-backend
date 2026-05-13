package com.donorconnect.inventoryservice.repository;

import com.donorconnect.inventoryservice.entity.StockTransaction;
import com.donorconnect.inventoryservice.enums.TransactionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByComponentId(Long componentId);

    List<StockTransaction> findByTxnType(TransactionType txnType);

    Page<StockTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<StockTransaction> findByTxnDateBetween(LocalDate from, LocalDate to);
}