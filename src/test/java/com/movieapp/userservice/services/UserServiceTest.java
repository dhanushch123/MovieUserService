package com.movieapp.userservice.services;

import com.movieapp.userservice.models.AuthResponse;
import com.movieapp.userservice.models.Role;
import com.movieapp.userservice.models.Users;
import com.movieapp.userservice.repositories.UserRepository;
import com.movieapp.userservice.repositories.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository sessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    @Spy
    private UserService userService;



    //    public void registerUser(Users user) {
//        System.out.println(user.getFirstName() + " " + user.getLastName());
//        if(repo.existsByEmail(user.getEmail()) || repo.existsByMobile(user.getMobile())) {
//            // User already exists with this email or mobile
//            throw new RuntimeException("User already exists with same email or mobile");
//        }
//        user.setPassword(encoder.encode(user.getPassword()));
//        user.setUsername(user.getFirstName()+user.getMobile().substring(4,9));;
//        repo.save(user);
//    }

    @Test
    void registerUserAndShouldEncodePasswordAndSaveUser() {
        Users user = new Users();
        user.setFirstName("Dhanush");
        user.setLastName("Ch");
        user.setEmail("dhanush@example.com");
        user.setMobile("9876543210");
        user.setUsername(user.getFirstName()+user.getMobile().substring(4,9));
        user.setPassword("Dhanush@12345");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByMobile(user.getMobile())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword()))
                .thenReturn("encoded-password");

        userService.registerUser(user);
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getUsername()).isEqualTo("Dhanush54321");
        verify(passwordEncoder).encode("Dhanush@12345");
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).existsByMobile(user.getMobile());
        verify(userRepository).save(user);
    }




    @Test
    void registerUser_throwExceptionWhenUserWithEmailAlreadyExists() {
        Users user = new Users();
        user.setFirstName("Dhanush");
        user.setLastName("Ch");
        user.setEmail("dhanush@example.com");
        user.setMobile("9876543210");
        user.setUsername(user.getFirstName()+user.getMobile().substring(4,9));
        user.setPassword("Dhanush@12345");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(RuntimeException.class)
                        .hasMessage("User already exists with same email or mobile");
        verify(userRepository).existsByEmail(user.getEmail());

    }

    @Test
    void registerUser_throwExceptionWhenUserWithMobileAlreadyExists() {
        Users user = new Users();
        user.setFirstName("Dhanush");
        user.setLastName("Ch");
        user.setEmail("dhanush@example.com");
        user.setMobile("9876543210");
        user.setUsername(user.getFirstName()+user.getMobile().substring(4,9));
        user.setPassword("Dhanush@12345");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByMobile(user.getMobile())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User already exists with same email or mobile");

        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).existsByMobile(user.getMobile());

    }

//    public AuthResponse authenticate(String username, String password, HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException {
//        // User may enter username or email
//        System.out.println(username + " " +password);
//        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username,password);
//        Authentication authentication = authManager.authenticate(authToken);
//        if(authentication.isAuthenticated()) {
//            Users user = repo.findByUsernameOrEmail(username,username).get();
//            return getAuthResponse(user,request,response);
//        }
//        throw new BadCredentialsException("Invalid Credentials");
//    }

    @Test
    void authenticate_shouldReturnAuthTokenWhenCredentialsAreValid()
            throws NoSuchAlgorithmException {

        String username = "dhanushch3@gmail.com";
        String password = "12345678";


        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        Users dbUser = new Users();
        dbUser.setEmail(username);
        dbUser.setRoles(List.of(Role.USER, Role.MANAGER));

        AuthResponse expected = new AuthResponse();
        expected.setAccessToken("access-token");
        expected.setRoles(List.of("USER"));
        expected.setUserId(UUID.randomUUID());

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.isAuthenticated()).thenReturn(true);

        when(userRepository.findByUsernameOrEmail(username, username))
                .thenReturn(Optional.of(dbUser));

        doReturn(expected)
                .when(userService)
                .getAuthResponse(dbUser, request, response);

        AuthResponse actual =
                userService.authenticate(username, password, request, response);

        assertThat(expected).isEqualTo(actual);

        verify(authenticationManager)
                .authenticate(authCaptor.capture());
        UsernamePasswordAuthenticationToken passedToken = authCaptor.getValue();
        assertThat(passedToken.getPrincipal()).isEqualTo(username);
        assertThat(passedToken.getCredentials()).isEqualTo(password);
        verify(authentication).isAuthenticated();
        verify(userRepository).findByUsernameOrEmail(username, username);
        verify(userService).getAuthResponse(dbUser, request, response);
    }
    @Test
    void authentication_shouldThrowExceptionWhenCredentialsAreInvalid() throws NoSuchAlgorithmException {
        String username = "dhanushch3@gmail.com";
        String password = "123456";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        assertThatThrownBy(() -> userService.authenticate(username,password,request,response))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid Credentials");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication).isAuthenticated();
    }

}
