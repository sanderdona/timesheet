import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import nl.codeclan.timesheet.entities.Location
import kotlin.reflect.KClass

class LocationValidator : ConstraintValidator<ValidLocation, Location> {
    override fun isValid(location: Location?, context: ConstraintValidatorContext): Boolean {
        if (location == null || location.name == Location.HOME) {
            return true
        }

        if (!isWithinNetherlands(location.lat, location.lon)) {
            context.disableDefaultConstraintViolation() // Disable default message
            context.buildConstraintViolationWithTemplate(
                "Latitude (${location.lat}) and Longitude (${location.lon}) must be within the Netherlands."
            ).addConstraintViolation()
            return false
        }

        if (location.distance == null || location.distance!! <= 0) {
            context.disableDefaultConstraintViolation() // Disable default message
            context.buildConstraintViolationWithTemplate(
                "Distance should be greater than zero (${location.distance})."
            ).addConstraintViolation()
            return false
        }

        return true
    }

    private fun isWithinNetherlands(lat: Double, lon: Double): Boolean = lat in 50.75..53.7 && lon in 3.36..7.22
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [LocationValidator::class])
annotation class ValidLocation(
    val message: String = "Invalid location.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
