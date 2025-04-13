package io.neif.coworkingplacehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoworkingStatsForAdminDTO {
    private String name;
    private String location;
    private Integer totalSpots;
    private Integer availableSpots;
    private Integer bookedSpots;
    private Integer dailyPrice;
    private Integer monthlyPrice;
    private Boolean isFreeze;
}
