package io.neif.coworkingplacehub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "spot")
public class Spot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID clientId;

    @Column(name = "start_date")
    private LocalDateTime startBookDate;

    @Column(name = "end_date")
    private LocalDateTime endBookDate;


    @Column(name = "active_booking")
    private Boolean activeBooking;

    @ManyToOne
    @JoinColumn(name = "coworking_id")
    @ToString.Exclude
    private Coworking coworking;

}