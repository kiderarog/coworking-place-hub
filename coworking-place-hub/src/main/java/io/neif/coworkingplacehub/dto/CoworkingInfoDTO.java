package io.neif.coworkingplacehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoworkingInfoDTO {
    private String name;
    private String location;
    private Integer totalSpots;
    private Integer availableSpots;
    private Integer dailyPrice;
    private Integer monthlyPrice;
    private boolean isFreeze;
}
