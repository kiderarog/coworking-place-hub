package io.neif.coworkingplacehub.dto;


import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EditProfileDTO {

    @Pattern(regexp = "^[A-Za-zА-Яа-яЁё]+$", message = "Имя должно содержать только буквы без цифр и спецсимволов.")
    private String newName;

    @Pattern(regexp = "^[A-Za-zА-Яа-яЁё]+$", message = "Фамилия должна содержать только буквы без цифр и спецсимволов.")
    private String newSurname;
}
