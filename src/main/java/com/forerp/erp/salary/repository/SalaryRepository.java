package com.forerp.erp.salary.repository;

import com.forerp.erp.salary.domain.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    Optional<Salary> findByUser_Id(Long userId);
}
