package org.example.studylog.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.example.studylog.entity.RefreshEntity;
import org.example.studylog.exception.TokenValidationException;
import org.example.studylog.jwt.JWTUtil;
import org.example.studylog.repository.RefreshRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    private final RefreshRepository refreshRepository;
    private final JWTUtil jwtUtil;

    public TokenService(RefreshRepository refreshRepository, JWTUtil jwtUtil) {
        this.refreshRepository = refreshRepository;
        this.jwtUtil = jwtUtil;
    }

    public void addRefreshEntity(String oauthId, String refresh, Long expiredMs){
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .oauthId(oauthId)
                .refresh(refresh)
                .expiration(date.toString())
                .build();

        refreshRepository.save(refreshEntity);
    }

    public void validateRefreshToken(String refresh){
        // refresh가 없을 때
        if(refresh == null){
            throw new TokenValidationException("refresh token null");
        }

        // refresh 토큰의 만료 여부 확인
        try{
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e){
            throw new TokenValidationException("invalid refresh token");
        }

        // 토큰이 refresh 인지 확인
        String category = jwtUtil.getCategory(refresh);
        if(!category.equals("refresh")){
            throw new TokenValidationException("invalid refresh token");
        }

        // refreshToken이 존재하는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if(!isExist){
            throw new TokenValidationException("invalid refresh token");
        }
    }

    public void replaceRefreshToken(String oldRefresh, String newRefresh, String oauthId, long expiredMs){
        refreshRepository.deleteByRefresh(oldRefresh);
        addRefreshEntity(oauthId, newRefresh, expiredMs);
    }
}
