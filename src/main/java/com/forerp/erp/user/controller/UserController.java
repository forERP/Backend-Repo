package com.forerp.erp.user.controller;

import com.forerp.erp.user.dto.LoginRequestDto;
import com.forerp.erp.user.dto.UserSetupRequestDto;
import com.forerp.erp.user.dto.UserResponseDto;
import com.forerp.erp.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> setupUser(
            @PathVariable Long id,
            @RequestBody UserSetupRequestDto request)
       {
        UserResponseDto responseDto = userService.setupUser(id,request);
        return ResponseEntity.ok(responseDto);
    }

    // 로그인 API
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        String token = userService.login(request);

        response.setHeader("Authorization", "Bearer" + token);

        return ResponseEntity.ok("로그인에 성공했습니다.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        UserResponseDto responseDto = userService.getUser(id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> userList =userService.getAllUser();

        return ResponseEntity.ok(userList);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
