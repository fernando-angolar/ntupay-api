package ao.com.angotech.controller;

import ao.com.angotech.common.ApiErrorResponse;
import ao.com.angotech.dtos.UserRegistrationRequest;
import ao.com.angotech.dtos.UserRegistrationResponse;
import ao.com.angotech.service.RegisterUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Gestão de usuários")
public class UserController {

    private final RegisterUserService registerUserService;

    public UserController(RegisterUserService registerUserService) {
        this.registerUserService = registerUserService;
    }


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "UC001 - Cadastro de novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de email ou telefone",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UserRegistrationResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        return registerUserService.execute(request);
    }
}
