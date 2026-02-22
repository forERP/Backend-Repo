package com.forerp.erp.salary.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.salary.dto.SalaryDto;
import com.forerp.erp.salary.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "급여", description = "급여 정보 등록 및 계산 API")
@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SalaryController {

    private final SalaryService salaryService;

    @Operation(summary = "급여 정보 등록/수정")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Void> registerSalary(@RequestBody SalaryDto.Request request) {
        salaryService.registerSalary(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "급여 계산 (월별 정산)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "계산 성공",
                content = @Content(schema = @Schema(implementation = SalaryDto.Response.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "사용자 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/calculate")
    public ResponseEntity<SalaryDto.Response> calculatePayroll(
            @Parameter(description = "사용자 ID", example = "1") @RequestParam Long userId,
            @Parameter(description = "년도", example = "2026") @RequestParam int year,
            @Parameter(description = "월 (1-12)", example = "3") @RequestParam int month
    ) {
        return ResponseEntity.ok(salaryService.calculatePayroll(userId, year, month));
    }
}