package com.forerp.erp.employee.controller;

import com.forerp.erp.employee.dto.EmployeeCreateRequestDto;
import com.forerp.erp.employee.dto.EmployeeCreateResponseDto;
import com.forerp.erp.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeCreateResponseDto> createEmployee(
            @RequestBody EmployeeCreateRequestDto request
    ) {
        EmployeeCreateResponseDto responseDto = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/{employeeId}/clock_in")
    public ResponseEntity<Void> clockIn(
            @PathVariable Long employeeId
    ) {
        employeeService.clockIn(employeeId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{employeeId}/clock_out")
    public ResponseEntity<Void> clockOut(
            @PathVariable Long employeeId
    ) {
        employeeService.clockOut(employeeId);

        return ResponseEntity.ok().build();
    }
}