package ao.com.angotech.controller;

import ao.com.angotech.common.ApiErrorResponse;
import ao.com.angotech.dtos.LoginRequest;
import ao.com.angotech.dtos.LoginResponse;
import ao.com.angotech.dtos.UserRegistrationRequest;
import ao.com.angotech.dtos.UserRegistrationResponse;
import ao.com.angotech.service.LoginUserService;
import ao.com.angotech.service.RegisterUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Gestão de usuários")
public class UserController {

    private final RegisterUserService registerUserService;
    private final LoginUserService loginUserService;

    public UserController(RegisterUserService registerUserService, LoginUserService loginUserService) {
        this.registerUserService = registerUserService;
        this.loginUserService = loginUserService;
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

    @PostMapping("/login")
    @Operation(summary = "UC002 - Login de usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Conta bloqueada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return loginUserService.execute(request, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
    }

}
