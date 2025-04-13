package io.neif.coworkingplacehub.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PriceChangingDTO {
    @Min(value = 1, message = "Цена должна быть целым числом.")
    private Integer newDailyPrice;

    @Min(value = 1, message = "Цена должна быть целым числом.")
    private Integer newMonthlyPrice;
}
