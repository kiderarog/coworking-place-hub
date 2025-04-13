package io.neif.coworkingplacehub.dto;

import io.neif.coworkingplacehub.validation.PasswordChangeGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;


@Data
public class AuthClientDTO {

    @NotEmpty(message = "Имя не должно быть пустым.", groups = {Default.class, PasswordChangeGroup.class})
    @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Использованы запрещенные спецсимволы.")
    @Indexed(unique = true)
    private String username;

    @NotEmpty(groups = {Default.class, PasswordChangeGroup.class})
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[\\W_]).{8,}$",
            message = "Пароль должен содержать минимум 8 символов, 1 заглавную букву и 1 спецсимвол.")
    private String password;

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

    private Integer totalPenaltySum;

}
