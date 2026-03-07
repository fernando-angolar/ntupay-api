package ao.com.angotech.dtos;

public record LoginResponse(
        boolean twoFactorRequired,
        String twoFactorSessionToken,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds
) {
}
