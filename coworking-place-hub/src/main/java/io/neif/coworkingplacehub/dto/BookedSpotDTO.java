package io.neif.coworkingplacehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BookedSpotDTO {
    private UUID id;
    private UUID clientId;
    private LocalDateTime startBookDate;
    private LocalDateTime endBookDate;
}
