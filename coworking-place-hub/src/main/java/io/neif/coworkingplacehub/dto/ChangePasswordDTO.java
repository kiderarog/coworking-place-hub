package io.neif.coworkingplacehub.dto;

import io.neif.coworkingplacehub.validation.PasswordChangeGroup;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class ChangePasswordDTO {

    @NotEmpty(groups = {Default.class, PasswordChangeGroup.class})
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[\\W_]).{8,}$",
            message = "Пароль должен содержать минимум 8 символов, 1 заглавную букву и 1 спецсимвол.")
    private String password;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[\\W_]).{8,}$",
            message = "Пароль должен содержать минимум 8 символов, 1 заглавную букву и 1 спецсимвол.",
            groups = {Default.class, PasswordChangeGroup.class})
    private String newPassword;
}
