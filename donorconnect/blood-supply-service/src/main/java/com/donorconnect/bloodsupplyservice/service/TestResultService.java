package com.donorconnect.bloodsupplyservice.service;


import com.donorconnect.bloodsupplyservice.dto.request.TestResultRequest;
import com.donorconnect.bloodsupplyservice.entity.TestResult;
import com.donorconnect.bloodsupplyservice.enums.TestStatus;
import com.donorconnect.bloodsupplyservice.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultRepository testResultRepository;

    public TestResult create(TestResultRequest req) {
        TestResult t = TestResult.builder()
                .donationId(req.getDonationId())
                .testType(req.getTestType())
                .result(req.getResult())
                .resultDate(req.getResultDate() != null ? req.getResultDate() : LocalDate.now())
                .enteredBy(req.getEnteredBy())
                .status(req.getResult() != null && req.getResult().equalsIgnoreCase("REACTIVE")
                        ? TestStatus.REACTIVE : TestStatus.COMPLETED)
                .build();
        return testResultRepository.save(t);
    }

    public TestResult getById(Long id) {
        return testResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", String.valueOf(id)));
    }

    public List<TestResult> getByDonation(Long donationId) {
        return testResultRepository.findByDonationId(donationId);
    }

    public TestResult update(Long id, TestResultRequest req) {
        TestResult t = getById(id);
        if (req.getResult() != null) {
            t.setResult(req.getResult());
            t.setStatus(req.getResult().equalsIgnoreCase("REACTIVE")
                    ? TestStatus.REACTIVE : TestStatus.COMPLETED);
        }
        if (req.getEnteredBy() != null) t.setEnteredBy(req.getEnteredBy());
        return testResultRepository.save(t);
    }

    public List<TestResult> getReactive() {
        return testResultRepository.findByStatus(TestStatus.REACTIVE);
    }

    public List<TestResult> getPending() {
        return testResultRepository.findByStatus(TestStatus.PENDING);
    }
}
