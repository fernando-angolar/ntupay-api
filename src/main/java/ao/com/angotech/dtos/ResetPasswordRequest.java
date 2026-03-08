package ao.com.angotech.dtos;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Nova senha é obrigatória")
        String newPassword,

        @NotBlank(message = "Confirmação da senha é obrigatória")
        String confirmPassword
) {
}
