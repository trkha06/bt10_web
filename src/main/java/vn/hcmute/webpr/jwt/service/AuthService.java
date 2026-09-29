package vn.hcmute.webpr.jwt.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.webpr.jwt.entity.User;
import vn.hcmute.webpr.jwt.model.LoginRequest;
import vn.hcmute.webpr.jwt.model.LoginResponse;
import vn.hcmute.webpr.jwt.model.RegisterRequest;
import vn.hcmute.webpr.jwt.repository.UserRepository;
import vn.hcmute.webpr.jwt.security.JwtService;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public User signup(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }
        return userRepository.save(new User(request.fullName(), request.email(), passwordEncoder.encode(request.password())));
    }

    public LoginResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        return new LoginResponse(jwtService.generateToken(user), jwtService.getExpirationTime());
    }
}
