package io.neif.coworkingplacehub.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "otp")
public class OneTimePassword {

    @Id
    private String email;
    @Column(name = "otp_code")
    private Integer otpCode;
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
    @Column(name = "exp_at")
    private LocalDateTime expAt;
}

