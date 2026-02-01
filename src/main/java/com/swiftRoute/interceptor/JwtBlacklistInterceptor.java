package com.swiftRoute.interceptor;

import com.swiftRoute.response.ApiResponse;
import com.swiftRoute.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@AllArgsConstructor
public class JwtBlacklistInterceptor implements HandlerInterceptor {
//    private final RedisService redisService;
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
//            throws Exception {
//        log.info("HTTP Request : {}",request.getHeader("Authorization"));
//
//        String token = request.getHeader("Authorization");
//        if(token != null){
//
//            if(redisService.isBlacklistToken(token.substring(7))){
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.setContentType("application/json");
//                response.setCharacterEncoding("UTF-8");
//                ObjectMapper objectMapper = new ObjectMapper();
//                objectMapper.writeValue(
//                        response.getWriter(),
//                        ApiResponse.builder()
//                                .Status(HttpStatus.UNAUTHORIZED)
//                                .Message("Token expired")
//                );
//            }
//        }
//        return true;
//    }
}
