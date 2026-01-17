package com.swiftRoute.service;

import com.swiftRoute.entity.User;
import com.swiftRoute.enums.UserRole;
import com.swiftRoute.records.user.RegisterRequest;
import com.swiftRoute.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public void register(RegisterRequest registerRequest){
        try{
            if(emailAlreadyExist(registerRequest.email(),registerRequest.role())){
                log.info("{} email and his {} role already exist",registerRequest.email(),registerRequest.role());
                throw new RuntimeException("User Already exist");
            }

            User user = User.builder()
                    .name(registerRequest.name())
                    .email(registerRequest.email())
                    .password(registerRequest.password())
                    .role(registerRequest.role())
                    .build();
            log.info("User information : {}",user.getRole());
            userRepository.save(user);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private boolean emailAlreadyExist(String email, UserRole role){
            log.info("check email and Role already exist");
            return userRepository.existsByEmailAndRole(email,role).orElse(false);
    }
}
