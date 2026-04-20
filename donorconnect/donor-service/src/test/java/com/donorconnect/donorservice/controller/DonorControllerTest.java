package com.donorconnect.donorservice.controller;

import com.donorconnect.donorservice.dto.request.DonorRequest;
import com.donorconnect.donorservice.dto.response.ApiResponse;
import com.donorconnect.donorservice.entity.Donor;
import com.donorconnect.donorservice.enums.DonorStatus;
import com.donorconnect.donorservice.service.DonorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("DonorController Test Suite")
public class DonorControllerTest {

    @Mock
    private DonorService donorService;

    @InjectMocks
    private DonorController donorController;

    private Donor testDonor;
    private DonorRequest donorRequest;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();

        testDonor = new Donor();
        testDonor.setDonorId(1L);
        testDonor.setName("John Doe");
        testDonor.setContactInfo("donor@test.com");
        testDonor.setBloodGroup("O+");

        donorRequest = new DonorRequest();
        donorRequest.setName("John Doe");
        donorRequest.setContactInfo("donor@test.com");
        donorRequest.setBloodGroup("O+");
    }

    @Test
    @DisplayName("Should create donor successfully")
    void testCreateDonorSuccess() {
        when(donorService.create(any(DonorRequest.class))).thenReturn(testDonor);

        ResponseEntity<ApiResponse<?>> response = donorController.create(donorRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @DisplayName("Should get donor by ID successfully")
    void testGetDonorByIdSuccess() {
        when(donorService.getById(1L)).thenReturn(testDonor);

        ResponseEntity<ApiResponse<?>> response = donorController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should get all donors successfully")
    void testGetAllDonorsSuccess() {
        List<Donor> donors = new ArrayList<>();
        donors.add(testDonor);

        when(donorService.getAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(donors));

        ResponseEntity<ApiResponse<?>> response = donorController.getAll(PageRequest.of(0, 20));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should update donor successfully")
    void testUpdateDonorSuccess() {
        when(donorService.update(1L, donorRequest)).thenReturn(testDonor);

        ResponseEntity<ApiResponse<?>> response = donorController.update(1L, donorRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should update donor status successfully")
    void testUpdateDonorStatusSuccess() {
        when(donorService.updateStatus(1L, DonorStatus.ACTIVE)).thenReturn(testDonor);

        ResponseEntity<ApiResponse<?>> response = donorController.updateStatus(1L, DonorStatus.ACTIVE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should search donors by blood group")
    void testSearchByBloodGroupSuccess() {
        List<Donor> donors = new ArrayList<>();
        donors.add(testDonor);

        when(donorService.getByBloodGroup("O+")).thenReturn(donors);

        ResponseEntity<ApiResponse<?>> response = donorController.getByBloodGroup("O+");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }
}