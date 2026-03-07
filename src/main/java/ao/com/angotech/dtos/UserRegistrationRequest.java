package ao.com.angotech.dtos;

import ao.com.angotech.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserRegistrationRequest(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Email é obrigatório")
        @Pattern(
                regexp = "(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\\[(?:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
                message = "Email inválido"
        )
        String email,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "^\\+2449\\d{8}$", message = "Telefone angolano inválido. Ex: +2449XXXXXXXX")
        String phone,

        @NotBlank(message = "Senha é obrigatória")
        String password,

        @NotBlank(message = "Confirmação de senha é obrigatória")
        String confirmPassword,

        @NotNull(message = "Tipo de conta é obrigatório")
        AccountType accountType
) {
}
