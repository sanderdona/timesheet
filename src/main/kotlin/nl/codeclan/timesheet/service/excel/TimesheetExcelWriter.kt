package nl.codeclan.timesheet.service.excel

import nl.codeclan.timesheet.model.Day
import nl.codeclan.timesheet.model.DayType
import nl.codeclan.timesheet.model.Timesheet
import org.apache.poi.ss.usermodel.*
import org.springframework.stereotype.Service


@Service
class TimesheetExcelWriter: AbstractExcelWriter() {

    override fun name(): String = "Timesheet"

    override fun write(workbook: Workbook, sheet: Sheet, timesheet: Timesheet) {
        sheet.setColumnWidth(0, 6000)
        timesheet.forEachColumn { i -> sheet.setColumnWidth(i, 1200) }

        sheet.createRow(0).createCell(0).setCellValue(timesheet.getMonthDisplay())
        dateRow(workbook, sheet, timesheet)

        createRow(workbook, sheet, timesheet, 3, "Klant uren", DayType.WORK)
        createRow(workbook, sheet, timesheet, 4, "Ziekte", DayType.SICK)
        createRow(workbook, sheet, timesheet, 5, "Verlof", DayType.LEAVE)
        createRow(workbook, sheet, timesheet, 6, "Feestdagen", DayType.HOLIDAY)
        createRow(workbook, sheet, timesheet, 7, "Clandays", DayType.CLANDAY)

        totalRow(workbook, sheet, timesheet)
    }

    private fun dateRow(workbook: Workbook,  sheet: Sheet, timesheet: Timesheet) {
        val dates = sheet.createRow(2)
        timesheet.days.forEachIndexed { i, day ->
            val type = day.type
            val cell = dates.createCell(i + 1)
            cell.setCellValue("${i + 1}")
            cell.cellStyle = style(workbook, if (type == DayType.WEEKEND || type == DayType.HOLIDAY) IndexedColors.GREY_25_PERCENT else IndexedColors.WHITE)
        }
    }

    private fun createRow(workbook: Workbook, sheet: Sheet, timesheet: Timesheet, index: Int, label: String, type: DayType) {
        val row = sheet.createRow(index)
        row.createCell(0).setCellValue(label)
        addIfType(workbook, row, timesheet.days, type, index)
    }

    private fun addIfType(workbook: Workbook, row: Row, types: List<Day>, target: DayType, index: Int) {
        types.forEachIndexed{ i, day ->
            run {
                val cell = row.createCell(i + 1)
                val style = workbook.createCellStyle()
                cell.cellStyle = style
                if (day.type == target) {
                    cell.setCellValue(8.0)
                    style.alignment = HorizontalAlignment.CENTER
                }
                if (day.type == DayType.WEEKEND || day.type == DayType.HOLIDAY) {
                    style.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                }
            }
        }

        val total = row.createCell(types.size + 1)
        total.cellFormula = sumFormula(1, index + 1, types.size, index + 1)
        total.cellStyle = style(workbook, IndexedColors.WHITE)
    }

    private fun totalRow(workbook: Workbook, sheet: Sheet, timesheet: Timesheet) {
        val total: Row = sheet.createRow(8)
        val totalLabel = total.createCell(0)
        totalLabel.setCellValue("Totaal aantal uren")
        totalLabel.cellStyle = boldStyle(workbook)

        timesheet.days.forEachIndexed { i, day ->
            val column = i + 1
            val cell = total.createCell(column)
            val holiday = day.type == DayType.WEEKEND || day.type == DayType.HOLIDAY
            if (!holiday) {
                cell.cellFormula = sumFormula(column, 4, column, 8)
            }
            cell.cellStyle = style(workbook, if (holiday) IndexedColors.GREY_25_PERCENT else IndexedColors.WHITE)
        }

        val colIndex = timesheet.days.size + 1
        val sum = total.createCell(colIndex)
        sum.cellFormula = sumFormula(colIndex, 4, colIndex, 8)
        sum.cellStyle = style(workbook, IndexedColors.LIME)

    }

    private fun style(workbook: Workbook, color: IndexedColors): CellStyle {
        val headerStyle = boldStyle(workbook)
        headerStyle.fillForegroundColor = color.index
        headerStyle.borderBottom = BorderStyle.THIN
        headerStyle.borderTop = BorderStyle.THIN
        headerStyle.borderRight = BorderStyle.THIN
        headerStyle.borderLeft = BorderStyle.THIN
        headerStyle.alignment = HorizontalAlignment.CENTER
        return headerStyle
    }
}
