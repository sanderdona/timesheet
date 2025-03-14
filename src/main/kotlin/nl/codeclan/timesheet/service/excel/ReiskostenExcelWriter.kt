package nl.codeclan.timesheet.service.excel

import nl.codeclan.timesheet.entities.Location
import nl.codeclan.timesheet.model.Day
import nl.codeclan.timesheet.model.Timesheet
import nl.codeclan.timesheet.repository.LocationRepository
import org.apache.poi.ss.usermodel.*
import org.springframework.stereotype.Service
import java.time.LocalDate


@Service
class ReiskostenExcelWriter(val locationRepository: LocationRepository) : AbstractExcelWriter() {

    override fun name(): String = "Reiskosten"

    override fun write(workbook: Workbook, sheet: Sheet, timesheet: Timesheet) {
        val locations = locationRepository.findAll().associateBy { it.id!! }
        IntRange(0, 3).forEach{ i -> sheet.setColumnWidth(i, 4000) }

        sheet.createRow(0).createCell(0).setCellValue(timesheet.getMonthDisplay())
        createHeaders(workbook, sheet, 2)
        var rowIdx = 2
        timesheet.days
            .forEachIndexed { i, day ->
                if (day.location != null && locations[day.location]?.name != Location.HOME) {
                    rowIdx++
                    val row = sheet.createRow(rowIdx)
                    val date = timesheet.getDay(i)
                    createRow(row, rowIdx + 1, date, day, workbook, locations)
                }
            }

        val headerStyle = workbook.createCellStyle()
        headerStyle.borderTop = BorderStyle.THIN

        rowIdx++
        val total = sheet.createRow(rowIdx)
        IntRange(0, 3).forEach { i -> createTotalCell(total, i, workbook) }
        total.getCell(0).setCellValue("Totaal")
        total.getCell(2).cellFormula = sumFormula(2, 4, 2, rowIdx)
        total.getCell(2).cellStyle.dataFormat = workbook.creationHelper.createDataFormat().getFormat("# k\\m")
        total.getCell(3).cellFormula = sumFormula(3, 4, 3, rowIdx)
        total.getCell(3).cellStyle.dataFormat = workbook.creationHelper.createDataFormat().getFormat("\\€ #,##0.00")
    }

    private fun createTotalCell(row: Row, idx: Int, workbook: Workbook): Cell? {
        val kmCell = row.createCell(idx)
        kmCell.cellStyle = boldStyle(workbook)
        kmCell.cellStyle.borderTop = BorderStyle.THIN
        kmCell.cellStyle.verticalAlignment = VerticalAlignment.CENTER
        return kmCell
    }

    private fun createHeaders(workbook: Workbook, sheet: Sheet, index: Int) {
        val row = sheet.createRow(index)
        listOf("Datum", "Locatie", "Kilometers", "Kosten").forEachIndexed { i, it -> boldCell(row, i, workbook, it) }
    }

    private fun boldCell(row: Row, i: Int, workbook: Workbook, value: String) {
        val cell = row.createCell(i)
        cell.cellStyle = boldStyle(workbook)
        cell.setCellValue(value)
    }

    private fun createRow(
        row: Row,
        rowIdx: Int,
        date: LocalDate,
        day: Day,
        workbook: Workbook,
        locations: Map<Long, Location>
    ) {
        val location = locations[day.location]!!
        val dateCell = row.createCell(0)
        dateCell.cellStyle = formatStyle(workbook, "dd-mm-yyyy")
        dateCell.cellStyle.alignment = HorizontalAlignment.LEFT
        dateCell.setCellValue(date)

        val locCell = row.createCell(1)
        locCell.setCellValue(location.name)

        val kmCell = row.createCell(2, CellType.NUMERIC)
        kmCell.cellStyle = formatStyle(workbook, "# k\\m")
        kmCell.setCellValue(location.distance!!.toDouble())

        val costsCell = row.createCell(3)
        costsCell.cellFormula = "C${rowIdx}*0.23"
        costsCell.cellStyle = formatStyle(workbook, "\\€ #,##0.00")
    }

    private fun formatStyle(workbook: Workbook, format: String): CellStyle? {
        val cellStyle = workbook.createCellStyle()
        cellStyle.dataFormat = workbook.creationHelper.createDataFormat().getFormat(format)
        return cellStyle
    }
}
