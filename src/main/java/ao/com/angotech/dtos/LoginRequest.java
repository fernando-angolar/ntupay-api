package ao.com.angotech.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email ou telefone é obrigatório")
        String identifier,

        @NotBlank(message = "Senha é obrigatória")
        String password,

        @Size(min = 6, max = 6, message = "Código 2FA deve conter 6 dígitos")
        String twoFactorCode,

        String twoFactorSessionToken

) {
}
