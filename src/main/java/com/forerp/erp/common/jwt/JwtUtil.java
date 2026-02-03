package com.forerp.erp.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // JWT를 만들 떄 사용하는 암호화 키
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    private SecretKey key;

    // 토큰 유효시간(24시간)
    private static final long tokenValidityInMilliseconds = 24 * 60 * 60 * 1000L;

    @PostConstruct
    public void init() {
        // secretKey를 암호화된 키 객체로 변환
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰에 담을 정보 설정
    public String generateToken (String loginId, String role){
            Date now = new Date();
            Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

            return Jwts.builder()
                    .subject(loginId)
                    .claim("role", role) // 정보 설정
                    .issuedAt(now) // 발급 시간 설정
                    .expiration(validity) // 만료 시간 설정
                    .signWith(key) // 사용할 암호화 키 설정
                    .compact(); // JWT 문자열 생성
        }

    public boolean validateToken(String token){
        try{
            // 설정한 키를 사용하여 토큰을 파싱하고, 유효성 검사
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰을 파싱하여, subject에 저장했던 로그인 ID 반환
    public String getLoginIdFromToken(String token){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    // 토큰을 파싱하여, role 이라는 이름으로 저장했던 역할 정보 반환
    // 토큰 파싱이란, 암호화된 토큰을 열어서, 누가 보냈는지, 무슨 내용인지, 진짜인지 종합적으로 확인하는 작업
    public String getRoleFromToken(String token){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role",String.class);
    }


    // BearerToken = 토큰을 소지하고 있는 사람, 토큰 = 출입증
    // Bearer 라는 불필요한 글자를 잘라내고, 뒤에 붙어있는 실제 토큰만 깔끔하게 꺼내옵니다.
    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }


}
