package com.github.yozdemir.security;

import com.github.yozdemir.config.MessageSourceConfig;
import com.github.yozdemir.domain.entity.Users;
import com.github.yozdemir.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.github.yozdemir.common.MessageKeys.ERROR_USERNAME_NOT_FOUND;

/**
 * Service used for UserDetails related operations.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MessageSourceConfig messageConfig;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(messageConfig.getMessage(ERROR_USERNAME_NOT_FOUND, username)));
        return UserDetailsImpl.build(user);
    }
}
