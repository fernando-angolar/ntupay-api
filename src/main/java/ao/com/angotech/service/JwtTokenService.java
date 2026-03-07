package ao.com.angotech.service;

import ao.com.angotech.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Date;

@Service
public class JwtTokenService {

    private final Algorithm algorithm;

    public JwtTokenService(@Value("${ntupay.jwt.secret}") String jwtSecret) {
        this.algorithm = Algorithm.HMAC256(jwtSecret);
    }

    public String generateAccessToken(User user, OffsetDateTime expiresAt) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("phone", user.getPhone())
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(expiresAt.toInstant()))
                .sign(algorithm);
    }

    public String generateTwoFactorSessionToken(User user, OffsetDateTime expiresAt) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("purpose", "2fa")
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(expiresAt.toInstant()))
                .sign(algorithm);
    }
}

