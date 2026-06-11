package com.swiftRoute.controller;

import com.swiftRoute.service.CustomerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
@Slf4j
@AllArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
}
