package ntu26.ss.parkinpeace.android.models

import ntu26.ss.parkinpeace.models.VehicleType

enum class Agency { HDB, URA, LTA }

sealed interface NamedFilter {
    fun apply(list: List<CarparkAvailability>): List<CarparkAvailability>

    data class AgencyFilter(val accept: Map<Agency, Boolean>) : NamedFilter {
        private val only = accept.filter { it.value }.keys
        private val predicate = { it: CarparkAvailability ->
            it.agency in only
        }

        override fun apply(list: List<CarparkAvailability>): List<CarparkAvailability> {
            return list.filter(predicate)
        }

        private val CarparkAvailability.agency
            get(): Agency? {
                return when (info?.ref?.split("/", limit = 2)?.firstOrNull()?.uppercase()) {
                    "HDB" -> Agency.HDB
                    "URA" -> Agency.URA
                    "LTA" -> Agency.LTA
                    else -> null
                }
            }
    }

    data class VacancyFilter(val minimum: Int?, val vehicleType: VehicleType) : NamedFilter {
        override fun apply(list: List<CarparkAvailability>): List<CarparkAvailability> {
            return if (minimum == null) list
            else list.filter { (it.lots[vehicleType]?.current ?: 0) >= minimum }
        }
    }
}

data class NamedFilters(val agencyFilter: NamedFilter.AgencyFilter, val vacancyFilter: NamedFilter.VacancyFilter) {
    fun toList(): List<NamedFilter> = listOf(agencyFilter, vacancyFilter)

    fun apply(list: List<CarparkAvailability>): List<CarparkAvailability> =
        toList().fold(list) { lst, filt -> filt.apply(lst) }

    fun updatedVehicle(vehicleType: VehicleType): NamedFilters =
        copy(vacancyFilter = vacancyFilter.copy(vehicleType = vehicleType))

    fun updated(value: NamedFilter): NamedFilters = when (value) {
        is NamedFilter.AgencyFilter -> copy(agencyFilter = value)
        is NamedFilter.VacancyFilter -> copy(vacancyFilter = value)
    }
}