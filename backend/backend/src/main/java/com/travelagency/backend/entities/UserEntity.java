package com.travelagency.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    public enum Role { CLIENT, ADMIN }
    public enum Status {ACTIVE, INACTIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true)
    private String keycloakId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String identityDocument;
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate (){
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {this.status = Status.ACTIVE;}
        if (this.role == null) {this.role = Role.CLIENT;}
        if (this.failedLoginAttempts == null) {this.failedLoginAttempts = 0;}
    }

}
