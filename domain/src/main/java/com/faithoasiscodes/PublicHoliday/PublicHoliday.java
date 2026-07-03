package com.faithoasiscodes.PublicHoliday;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "public_holiday")
@Getter
@Setter
@NoArgsConstructor
public class PublicHoliday {
    @Id
    private LocalDate date;

    private String name;

    public PublicHoliday(LocalDate date, String name) {
        this.date = date;
        this.name = name;
    }
}
