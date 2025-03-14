package nl.codeclan.timesheet.repository

import nl.codeclan.timesheet.entities.TimesheetDay
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.YearMonth

@Repository
interface DayRepository : CrudRepository<TimesheetDay, LocalDate> {
    fun findTimesheetDayByDateBetween(start: LocalDate, end: LocalDate): List<TimesheetDay>
}
fun DayRepository.findByMonth(month: YearMonth): Map<LocalDate, TimesheetDay> =
    findTimesheetDayByDateBetween(month.atDay(1), month.atEndOfMonth())
        .associateBy { it.date!! }
