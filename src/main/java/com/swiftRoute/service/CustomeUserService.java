package com.swiftRoute.service;

import com.swiftRoute.annotation.RedisCacheable;
import com.swiftRoute.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service class responsible for loading user details from the database.
 * <p>
 * This class implements the {@link UserDetailsService} interface, which provides the
 * {@link #loadUserByUsername(String)} method for retrieving user details by username.
 * The {@link UserRepository} is used to fetch user details from the database.
 * <p>
 * The {@link #loadUserByUsername(String)} method is annotated with {@link RedisCacheable}.
 * This cache stores user details for a short duration (500 seconds) and can be accessed using
 * the key "user:detail:<username>".
 *
 * @author Santosh Kumar
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class CustomeUserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by username.
     *
     * @param username the username of the user
     * @return the user details
     * @throws UsernameNotFoundException if the user is not found in the database
     */
    @Override
    // @RedisCacheable(
    //         key = "'user:detail:' + #username",
    //         ttl = 500,
    //         unit = TimeUnit.SECONDS
    // )
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
