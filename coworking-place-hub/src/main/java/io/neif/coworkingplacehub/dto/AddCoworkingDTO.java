package io.neif.coworkingplacehub.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class AddCoworkingDTO {

    @NotEmpty
    private String name;

    @NotEmpty
    private String location;

    @NotEmpty
    private Integer totalSpots;

    @NotEmpty
    private Integer availableSpots;

    @NotNull
    private Integer dailyPrice;


    @NotNull
    private Integer monthlyPrice;

}
