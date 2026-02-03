package com.forerp.erp.common.jwt;

import com.forerp.erp.user.domain.User;
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
import java.util.stream.Collectors;

@Slf4j // log를 자동으로 생성
@Component
@RequiredArgsConstructor
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 헤더에서 토큰 꺼내기
        String token = jwtUtil.resolveToken(request);

        // 토큰이 존재하고 유효한 경우에만 인증 처리
        try{
            if(token != null && jwtUtil.validateToken(token)) {
                String loginId = jwtUtil.getLoginIdFromToken(token);

                User user = userRepository.findByLoginId(loginId)
                        .orElseThrow(() -> new IllegalArgumentException("토큰은 유효하지만 해당하는 사용자가 없습니다."));

                List<SimpleGrantedAuthority> authorities = user.getPermissions().stream()
                        .map(permission -> new SimpleGrantedAuthority("ROLE_" + permission)).collect(Collectors.toList());

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user.getLoginId(),
                        null,
                        authorities); // 권한 목록

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("인증 성공: 사용자 '{}', 권한 : {}", loginId, authorities);
            }
        }catch (Exception e){
            log.error("인증 처리 중 에러 발생:{}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request,response);
    }
}
