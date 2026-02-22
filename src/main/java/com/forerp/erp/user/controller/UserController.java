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

@Tag(name = "User", description = "?ъ슜??愿由?API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "?ъ슜???앹꽦", description = "???ъ슜?먮? ?깅줉?⑸땲?? ADMIN 沅뚰븳 ?꾩슂.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "?앹꽦 ?깃났"),
            @ApiResponse(responseCode = "400", description = "?좏슚??寃???ㅽ뙣"),
            @ApiResponse(responseCode = "401", description = "?몄쬆 ?꾩슂"),
            @ApiResponse(responseCode = "403", description = "沅뚰븳 ?놁쓬")
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody UserCreateRequestDto request
    ) {
        return ResponseEntity.ok(userService.createUser(actor, request));
    }

    @Operation(summary = "?ъ슜????젣", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "??젣 ?깃났"),
            @ApiResponse(responseCode = "404", description = "?ъ슜???놁쓬")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "?ъ슜??ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "濡쒓렇??, description = "loginId + password濡?JWT ?좏겙 諛쒓툒")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "濡쒓렇???깃났, Authorization ?ㅻ뜑??Bearer ?좏겙 ?ы븿"),
            @ApiResponse(responseCode = "401", description = "?몄쬆 ?ㅽ뙣")
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

    @Operation(summary = "濡쒓렇?꾩썐", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "濡쒓렇?꾩썐 ?깃났")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User actor) {
        userService.logout(actor);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "POS 濡쒓렇??, description = "storeCode + employeeCode濡?POS 濡쒓렇??)
    @PostMapping("/login/pos")
    public ResponseEntity<LoginResponseDto> loginPos(@RequestBody Map<String, String> request) {
        String storeCode = request.get("storeCode");
        String employeeCode = request.get("employeeCode");
        return ResponseEntity.ok(userService.loginPos(storeCode, employeeCode));
    }

    @Operation(summary = "POS 濡쒓렇?꾩썐")
    @PostMapping("/logout/pos")
    public ResponseEntity<Void> logoutPos(@RequestBody Map<String, String> request) {
        String storeCode = request.get("storeCode");
        String employeeCode = request.get("employeeCode");
        userService.logoutPos(storeCode, employeeCode);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "?ъ슜???④굔 議고쉶", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @Parameter(description = "?ъ슜??ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @Operation(summary = "???뺣낫 議고쉶", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(userService.getCurrentUser(actor));
    }

    @Operation(summary = "?ъ썝踰덊샇 以묐났 ?뺤씤")
    @GetMapping("/check-employee-code")
    public ResponseEntity<Map<String, Object>> checkEmployeeCode(@RequestParam String employeeCode) {
        boolean available = userService.isEmployeeCodeAvailable(employeeCode);
        return ResponseEntity.ok(Map.of(
                "employeeCode", employeeCode,
                "available", available
        ));
    }

    @Operation(summary = "?꾩껜 ?ъ슜??紐⑸줉 議고쉶", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "?ъ슜??寃??(?섏씠吏?ㅼ씠??", security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "?ъ슜???뺣낫 ?섏젙", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "?ъ슜??ID") @PathVariable Long id,
            @RequestBody UserUpdateRequestDto request
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
