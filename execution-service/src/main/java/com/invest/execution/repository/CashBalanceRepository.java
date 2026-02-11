package com.invest.execution.repository;

import com.invest.execution.repository.entity.CashBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashBalanceRepository extends JpaRepository<CashBalance, String> {
}