package com.faithoasiscodes.PublicHoliday;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "public_holidays")
@Getter
@Setter
@NoArgsConstructor
public class PublicHoliday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    private String name;

    public PublicHoliday(LocalDate date, String name) {
        this.date = date;
        this.name = name;
    }
}
