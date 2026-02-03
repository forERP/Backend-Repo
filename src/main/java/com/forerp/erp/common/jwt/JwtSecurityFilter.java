package com.forerp.erp.common.jwt;

import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserStatus;
import com.forerp.erp.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j // log를 자동으로 생성
@Component
@RequiredArgsConstructor
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtUtil.resolveToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            try {
                String loginId = jwtUtil.getLoginIdFromToken(token);

                User user = userRepository.findByLoginId(loginId)
                        .orElseThrow(() -> new IllegalArgumentException("토큰은 유효하지만 해당 사용자가 없습니다."));

                if (user.getStatus() != UserStatus.ACTIVE) {
                    throw new IllegalStateException("비활성 사용자입니다.");
                }

                // role-only: 권한은 ROLE_{role} 1개만 부여
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("인증 성공: loginId={}, role={}", loginId, user.getRole());

            } catch (Exception e) {
                log.warn("인증 실패: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}