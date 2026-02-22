package com.forerp.erp.user.controller;

import com.forerp.erp.user.dto.LoginRequestDto;
import com.forerp.erp.user.dto.LoginResponseDto;
import com.forerp.erp.user.dto.UserCreateRequestDto;
import com.forerp.erp.user.dto.UserResponseDto;
import com.forerp.erp.user.dto.UserUpdateRequestDto;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "User", description = "User management API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create user", description = "Register a new user. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody UserCreateRequestDto request
    ) {
        return ResponseEntity.ok(userService.createUser(actor, request));
    }

    @Operation(summary = "Delete user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Login", description = "Issue JWT token with loginId + password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login success. Bearer token in Authorization header"),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
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

    @Operation(summary = "Logout", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Logout success")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User actor) {
        userService.logout(actor);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "POS Login", description = "POS login with storeCode + employeeCode")
    @PostMapping("/login/pos")
    public ResponseEntity<LoginResponseDto> loginPos(@RequestBody Map<String, String> request) {
        String storeCode = request.get("storeCode");
        String employeeCode = request.get("employeeCode");
        return ResponseEntity.ok(userService.loginPos(storeCode, employeeCode));
    }

    @Operation(summary = "POS Logout")
    @PostMapping("/logout/pos")
    public ResponseEntity<Void> logoutPos(@RequestBody Map<String, String> request) {
        String storeCode = request.get("storeCode");
        String employeeCode = request.get("employeeCode");
        userService.logoutPos(storeCode, employeeCode);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @Operation(summary = "Get current user", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(userService.getCurrentUser(actor));
    }

    @Operation(summary = "Check employee code availability")
    @GetMapping("/check-employee-code")
    public ResponseEntity<Map<String, Object>> checkEmployeeCode(@RequestParam String employeeCode) {
        boolean available = userService.isEmployeeCodeAvailable(employeeCode);
        return ResponseEntity.ok(Map.of(
                "employeeCode", employeeCode,
                "available", available
        ));
    }

    @Operation(summary = "Get all users", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Search users (paginated)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
            @RequestParam(required = false) String storeKeyword,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String storeCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.searchUsers(
                storeKeyword, storeName, storeCode, name, status, role,
                createdFrom, createdTo, pageable
        ));
    }

    @Operation(summary = "Update user", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody UserUpdateRequestDto request
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}