package ntu26.ss.parkinpeace.android.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ntu26.ss.parkinpeace.android.R
import ntu26.ss.parkinpeace.android.models.Agency
import ntu26.ss.parkinpeace.android.models.NamedFilter
import ntu26.ss.parkinpeace.android.models.NamedFilters
import ntu26.ss.parkinpeace.models.VehicleType

@Composable
fun RenderAgencyFilter(agencyFilter: NamedFilter.AgencyFilter, onChange: (NamedFilter.AgencyFilter) -> Unit) {
    val update = { agency: Agency ->
        { value: Boolean ->
            onChange(
                agencyFilter.copy(
                    accept = agencyFilter.accept.alter(
                        agency,
                        value
                    )
                )
            )
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Agency", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            for ((ag, c) in agencyFilter.accept) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = c, onCheckedChange = update(ag))
                    Spacer(Modifier.width(4.dp))
                    Text(ag.name)
                }
            }
        }
    }
}

@Composable
fun RenderVacancyFilter(vacancyFilter: NamedFilter.VacancyFilter, onChange: (NamedFilter.VacancyFilter) -> Unit) {
    val (value, setValue) = remember { mutableFloatStateOf(vacancyFilter.minimum?.toFloat() ?: 0f) }
    val onChangeW = { onChange(vacancyFilter.copy(minimum = value.toInt().let { if (it == 0) null else it })) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Min. Vacancy", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(4.dp))
        Slider(
            value = value,
            onValueChange = setValue,
            onValueChangeFinished = onChangeW,
            valueRange = 0f..100f,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(value.toInt().toString(), modifier = Modifier.width(30.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(filters: NamedFilters, show: Boolean, onFilterChanged: (NamedFilter) -> Unit, onDismiss: () -> Unit) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss, properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(shape = RoundedCornerShape(48.dp), shadowElevation = 8.dp) {
                Column(modifier = Modifier
                    .padding(24.dp, 32.dp)
                    .fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.filter),
                            contentDescription = "Filters",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Filters", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.height(24.dp))
                    RenderAgencyFilter(agencyFilter = filters.agencyFilter, onChange = onFilterChanged)
                    Spacer(Modifier.height(8.dp))
                    RenderVacancyFilter(vacancyFilter = filters.vacancyFilter, onChange = onFilterChanged)
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    val filters = NamedFilters(
        NamedFilter.AgencyFilter(
            mapOf(
                Agency.URA to true,
                Agency.LTA to true,
                Agency.HDB to true
            )
        ), NamedFilter.VacancyFilter(10, vehicleType = VehicleType.CAR)
    )
    Surface {
        FilterDialog(filters = filters, onFilterChanged = {}, show = true, onDismiss = {})
    }
}

private fun <K, V> Map<K, V>.alter(k: K, v: V): Map<K, V> = toMutableMap().apply { set(k, v) }.toMap()