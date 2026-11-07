package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByIdCard(String idCard);
    boolean existsByPhone(String phone);
}
