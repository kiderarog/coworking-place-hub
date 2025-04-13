package io.neif.coworkingplacehub.models;

import io.neif.coworkingplacehub.security.Roles;
import io.neif.coworkingplacehub.validation.PasswordChangeGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clients")
public class Client {

    @Id
    private UUID id = UUID.randomUUID();

    private Roles role;

    @NotEmpty(message = "Имя не должно быть пустым.", groups = {Default.class, PasswordChangeGroup.class})
    @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9]+$", message = "Использованы запрещенные спецсимволы.")
    @Indexed(unique = true)
    private String username;

    @NotEmpty(groups = {Default.class, PasswordChangeGroup.class})
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[\\W_]).{8,}$",
            message = "Пароль должен содержать минимум 8 символов, 1 заглавную букву и 1 спецсимвол.")
    private String password;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[\\W_]).{8,}$",
            message = "Пароль должен содержать минимум 8 символов, 1 заглавную букву и 1 спецсимвол.",
            groups = {Default.class, PasswordChangeGroup.class})
    private String newPassword;

    @NotEmpty
    private String name;

    @NotEmpty
    private String surname;

    @NotEmpty
    @Email(message = "Указан некорректный формат адреса электронной почты.")
    @Indexed(unique = true)
    private String email;

    @NotEmpty
    @Pattern(regexp = "^\\+7\\d{10}$", message = "Введите номер телефона в формате +7XXXXXXXXXX")
    @Indexed(unique = true)
    private String phone;

    private Double balance;

    private UUID bookedSpotId;

    @Column(name = "is_blocked")
    private boolean isBlocked;

    public boolean isBlocked() {
        return isBlocked;
    }


}