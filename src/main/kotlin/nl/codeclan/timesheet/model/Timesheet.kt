package nl.codeclan.timesheet.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class Timesheet(@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "M-yyyy") val month: YearMonth, val days: List<Day>) {

    override fun toString(): String {
        var result = getMonthDisplay()
        days.forEachIndexed { i, dayType -> result += "\n${(i+1).toString().padStart(2, '0')}: $dayType" }
        return result
    }

    fun getMonthDisplay() = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}"
    fun getMonth(): String? = month.format(DateTimeFormatter.ofPattern("MM-yyyy"))
    fun forEachColumn(fnc: (int: Int) -> Unit) = days.forEachIndexed { i, _ -> fnc(i + 1)}
    fun getDay(i: Int) = LocalDate.of(month.year, month.month, i + 1)
}
