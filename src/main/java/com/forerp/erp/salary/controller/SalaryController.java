package com.forerp.erp.salary.controller;


import com.forerp.erp.salary.dto.SalaryDto;
import com.forerp.erp.salary.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @PostMapping
    public ResponseEntity<Void> registerSalary(@RequestBody SalaryDto.Request request){
        salaryService.registerSalary(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calculate")
    public ResponseEntity<SalaryDto.Response> calculatePayroll(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {

        return ResponseEntity.ok(salaryService.calculatePayroll(userId,year,month));
    }

}
