package ao.com.angotech.mapper;

import ao.com.angotech.dtos.UserRegistrationResponse;
import ao.com.angotech.entity.User;

public final class UserMapper {

    private UserMapper() {

    }

    public static UserRegistrationResponse toUserRegistrationResponse(User user) {
        return new UserRegistrationResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                "Conta criada com sucesso, Verifique o seu email para activação.",
                user.getCreatedAt()
        );
    }
}
