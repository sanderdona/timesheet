package nl.codeclan.timesheet.entities

import jakarta.persistence.*
import nl.codeclan.timesheet.model.DayType
import java.time.LocalDate

@Entity
class TimesheetDay(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, columnDefinition = "DATE")
    var date: LocalDate? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: DayType? = null,

    @ManyToOne
    var location: Location? = null
)
