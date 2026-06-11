package com.swiftRoute.service;

import com.swiftRoute.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
}
