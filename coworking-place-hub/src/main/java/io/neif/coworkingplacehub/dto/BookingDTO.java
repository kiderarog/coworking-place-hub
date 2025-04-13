package io.neif.coworkingplacehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class BookingDTO {
    private UUID coworkingId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean month;

    public Boolean isMonth() {
        return month != null && month;
    }



}
