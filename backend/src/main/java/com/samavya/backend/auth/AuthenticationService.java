package com.samavya.backend.auth;

import com.samavya.backend.AuthenticationResponse;
import com.samavya.backend.UserAccount;
import com.samavya.backend.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration accessTokenTtl;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            org.springframework.core.env.Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.issuer = environment.getRequiredProperty("security.jwt.issuer");
        this.accessTokenTtl = Duration.parse(
                environment.getRequiredProperty("security.jwt.access-token-ttl"));
    }

    public UserAccount register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidRegistrationException();
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        try {
            return userRepository.create(
                    request.name().trim(),
                    email,
                    passwordEncoder.encode(request.password()));
        } catch (DataIntegrityViolationException exception) {
            if (userRepository.findByEmail(email).isPresent()) {
                throw new EmailAlreadyExistsException();
            }
            throw exception;
        }
    }

    public UserAccount getCurrentUser(long userId) {
        return userRepository.findAccountById(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user no longer exists"));
    }

    public AuthenticationResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid email or password");
        }
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email().trim().toLowerCase(Locale.ROOT),
                        request.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        long userId = Long.parseLong(userDetails.getUsername());
        UserAccount account = userRepository.findAccountById(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user disappeared during login"));

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(accessTokenTtl);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(Long.toString(account.id()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                        .build(),
                claims)).getTokenValue();

        return new AuthenticationResponse(
                token,
                "Bearer",
                accessTokenTtl.toSeconds(),
                account);
    }
}
