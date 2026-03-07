package ao.com.angotech.entity;

import ao.com.angotech.enums.AccountType;
import ao.com.angotech.enums.UserStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "test_api_key", nullable = false, length = 120)
    private String testApiKey;

    @Column(name = "prod_api_key", nullable = false, length = 120)
    private String prodApiKey;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "activation_token", nullable = false, unique = true, length = 120)
    private String activationToken;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public String getTestApiKey() { return testApiKey; }
    public void setTestApiKey(String testApiKey) { this.testApiKey = testApiKey; }
    public String getProdApiKey() { return prodApiKey; }
    public void setProdApiKey(String prodApiKey) { this.prodApiKey = prodApiKey; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getActivationToken() { return activationToken; }
    public void setActivationToken(String activationToken) { this.activationToken = activationToken; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
