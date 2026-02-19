package com.swiftRoute.controller;


import com.swiftRoute.entity.Driver;
import com.swiftRoute.enums.DriverStatus;
import com.swiftRoute.records.driver.DriverCreateRequest;
import com.swiftRoute.service.DriverService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
class DriverControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DriverService driverService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getProfile_success() throws Exception {

        var driver = Driver.builder()
                .name("John Doe")
                .phone("1234567890")
                .status(DriverStatus.OFFLINE)
                .build();

        Mockito.when(driverService.getProfile("driver"))
                .thenReturn(driver);


        mockMvc.perform(get("/api/driver/profile")
                        .principal(() -> "driver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Status").value("OK"))
                .andExpect(jsonPath("$.Message")
                        .value("User Profile retried successfully"))
                .andExpect(jsonPath("$.Data").value("PROFILE_DATA"));
    }



}
