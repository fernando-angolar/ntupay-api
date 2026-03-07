package ao.com.angotech.dtos;

import ao.com.angotech.enums.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserRegistrationResponse(

        UUID id,
        String name,
        String email,
        String phone,
        UserStatus status,
        String message,
        OffsetDateTime createdAt
) {
}
