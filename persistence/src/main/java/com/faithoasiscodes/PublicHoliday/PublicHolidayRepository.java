package com.faithoasiscodes.PublicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
@Repository
public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, LocalDate> {
    boolean existsByDate(LocalDate date);
}
