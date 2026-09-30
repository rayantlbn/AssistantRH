package com.solvia.assistantrh.controller;

import com.solvia.assistantrh.dto.joboffer.JobOfferRequest;
import com.solvia.assistantrh.dto.joboffer.JobOfferResponse;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import com.solvia.assistantrh.service.JobOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-offers")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService jobOfferService;

    @GetMapping
    public Page<JobOfferResponse> findAll(@RequestParam(required = false) JobOfferStatus status,
                                          @PageableDefault(size = 20) Pageable pageable) {
        return jobOfferService.findAll(status, pageable);
    }

    @GetMapping("/{id}")
    public JobOfferResponse findById(@PathVariable Long id) {
        return jobOfferService.findById(id);
    }

    @PostMapping
    public ResponseEntity<JobOfferResponse> create(@Valid @RequestBody JobOfferRequest request) {
        JobOfferResponse created = jobOfferService.create(request);
        return ResponseEntity.created(Locations.of(created.id())).body(created);
    }

    @PutMapping("/{id}")
    public JobOfferResponse update(@PathVariable Long id, @Valid @RequestBody JobOfferRequest request) {
        return jobOfferService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobOfferService.delete(id);
    }
}
