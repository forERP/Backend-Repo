package com.forerp.erp.user.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.dto.LoginRequestDto;
import com.forerp.erp.user.dto.LoginResponseDto;
import com.forerp.erp.user.dto.UserCreateRequestDto;
import com.forerp.erp.user.dto.UserResponseDto;
import com.forerp.erp.user.dto.UserUpdateRequestDto;
import com.forerp.erp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "사용자", description = "사용자 인증 및 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 생성", description = "새 사용자를 등록합니다. HQ_ADMIN 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody UserCreateRequestDto request
    ) {
        return ResponseEntity.ok(userService.createUser(actor, request));
    }

    @Operation(summary = "사용자 삭제", description = "사용자 ID로 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "로그인 (백오피스)", description = "loginId 또는 employeeCode + password 로 JWT 토큰을 발급합니다. 응답 Authorization 헤더에 Bearer 토큰 포함.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (아이디/비밀번호 불일치)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        LoginResponseDto loginResponse = userService.login(request);
        response.setHeader("Authorization", "Bearer " + loginResponse.getToken());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "로그아웃")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User actor) {
        userService.logout(actor);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "POS 로그인", description = "storeCode + employeeCode 로 POS 단말 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login/pos")
    public ResponseEntity<LoginResponseDto> loginPos(@RequestBody Map<String, String> request) {
        String storeCode = request.get("storeCode");
        String employeeCode = request.get("employeeCode");
        return ResponseEntity.ok(userService.loginPos(storeCode, employeeCode));
    }

    @Operation(summary = "POS 로그아웃")
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @PostMapping("/logout/pos")
    public ResponseEntity<Void> logoutPos(@RequestBody Map<String, String> request) {
        String storeCode = request.get("storeCode");
        String employeeCode = request.get("employeeCode");
        userService.logoutPos(storeCode, employeeCode);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @Operation(summary = "현재 로그인 사용자 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(userService.getCurrentUser(actor));
    }

    @Operation(summary = "사원번호 중복 확인", description = "해당 사원번호가 사용 가능한지 여부를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "확인 성공")
    @GetMapping("/check-employee-code")
    public ResponseEntity<Map<String, Object>> checkEmployeeCode(
            @Parameter(description = "확인할 사원번호", example = "EMP001") @RequestParam String employeeCode) {
        boolean available = userService.isEmployeeCodeAvailable(employeeCode);
        return ResponseEntity.ok(Map.of("employeeCode", employeeCode, "available", available));
    }

    @Operation(summary = "전체 사용자 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "사용자 검색 (페이지네이션)", description = "복수 조건으로 사용자를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
            @Parameter(description = "매장 통합 키워드") @RequestParam(required = false) String storeKeyword,
            @Parameter(description = "매장 이름") @RequestParam(required = false) String storeName,
            @Parameter(description = "매장 코드") @RequestParam(required = false) String storeCode,
            @Parameter(description = "사용자 이름") @RequestParam(required = false) String name,
            @Parameter(description = "계정 상태 (ACTIVE / INACTIVE)") @RequestParam(required = false) String status,
            @Parameter(description = "역할 (HQ_ADMIN / STORE_ADMIN / STORE_HALL_STAFF / STORE_KITCHEN_STAFF)") @RequestParam(required = false) String role,
            @Parameter(description = "가입일 시작 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @Parameter(description = "가입일 종료 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.searchUsers(
                storeKeyword, storeName, storeCode, name, status, role,
                createdFrom, createdTo, pageable));
    }

    @Operation(summary = "사용자 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id,
            @RequestBody UserUpdateRequestDto request
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}