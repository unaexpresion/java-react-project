package com.carlosmedina.javareactproject.repository;

import com.carlosmedina.javareactproject.model.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface ExecutionJpaRepository extends JpaRepository<Execution, Long> {


}
