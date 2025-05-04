package com.github.yozdemir.service;

import com.github.yozdemir.config.MessageSourceConfig;
import com.github.yozdemir.domain.entity.Users;
import com.github.yozdemir.dto.mapper.SignupRequestMapper;
import com.github.yozdemir.dto.request.LoginRequest;
import com.github.yozdemir.dto.request.SignupRequest;
import com.github.yozdemir.exception.ElementAlreadyExistsException;
import com.github.yozdemir.repository.UserRepository;
import com.github.yozdemir.security.JwtUtils;
import com.github.yozdemir.security.UserDetailsImpl;
import com.github.yozdemir.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SignupRequestMapper signupRequestMapper;

    @Mock
    private MessageSourceConfig messageConfig;

    private LoginRequest loginRequest;
    private UserDetailsImpl userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("testuser", "password");
        userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "password",
                "Test",
                "User",
                "12345678901",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @Test
    void login_shouldReturnJwtResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication))
                .thenReturn("test.jwt.token");

        var response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test.jwt.token", response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertEquals(Collections.singletonList("ROLE_USER"), response.getRoles());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
    }

    @Test
    void signup_shouldCreateNewUser() {
        var signupRequest = new SignupRequest(
                1L,
                "1234567890",
                "New",
                "User",
                "newuser",
                "new@example.com",
                "password",
                Set.of("ROLE_USER")
        );
        var newUser = new Users();
        newUser.setId(2L);
        newUser.setUsername("newuser");

        when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(signupRequestMapper.toUser(signupRequest)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(newUser);

        var response = authService.signup(signupRequest);

        assertNotNull(response);
        assertEquals(2L, response.id());

        verify(userRepository).existsByUsernameIgnoreCase("newuser");
        verify(userRepository).existsByEmailIgnoreCase("new@example.com");
        verify(signupRequestMapper).toUser(signupRequest);
        verify(userRepository).save(newUser);
    }

    @Test
    void signup_shouldThrowExceptionWhenUsernameExists() {
        var signupRequest = new SignupRequest(
                1L,
                "1234567890",
                "Existing",
                "User",
                "existinguser",
                "existing@example.com",
                "password",
                Set.of("ROLE_USER")
        );

        when(userRepository.existsByUsernameIgnoreCase("existinguser")).thenReturn(true);

        assertThrows(ElementAlreadyExistsException.class, () -> authService.signup(signupRequest));

        verify(userRepository).existsByUsernameIgnoreCase("existinguser");
        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
        verify(signupRequestMapper, never()).toUser(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_shouldThrowExceptionWhenEmailExists() {
        var signupRequest = new SignupRequest(
                1L,
                "12345678901",
                "New",
                "User",
                "newuser",
                "existing@example.com",
                "password",
                Set.of("ROLE_USER")
        );

        when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        assertThrows(ElementAlreadyExistsException.class, () -> authService.signup(signupRequest));

        verify(userRepository).existsByUsernameIgnoreCase("newuser");
        verify(userRepository).existsByEmailIgnoreCase("existing@example.com");
        verify(signupRequestMapper, never()).toUser(any());
        verify(userRepository, never()).save(any());
    }
}
