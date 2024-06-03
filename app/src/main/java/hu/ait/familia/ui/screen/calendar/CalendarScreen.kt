package hu.ait.familia.ui.screen.calendar

import android.Manifest
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import hu.ait.familia.R
import hu.ait.familia.util.CalendarEvent
import hu.ait.familia.util.addEventToCalendar
import hu.ait.familia.util.getTodayCalendarEvents
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    var showEventDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val context = LocalContext.current


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar Events") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showEventDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                val currentDate = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date())


                Image(
                    painter = painterResource(id = R.drawable.calendar),
                    contentDescription = null)
                Text(
                    text = currentDate,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                CalendarItemList()
            }

            if (showEventDialog) {
                AddEventDialog(
                    onDismiss = { showEventDialog = false },
                    onShowDatePicker = { showDatePickerDialog = true },
                    datePickerState = datePickerState,
                    context = context
                )
            }

            if (showDatePickerDialog) {
                DatePickerDialog(
                    onDismissRequest = { showDatePickerDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePickerDialog = false }) {
                            Text("Ok")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePickerDialog = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CalendarItemList() {
    val context = LocalContext.current
    var events = remember { listOf<CalendarEvent>() }

    var permissionGranted by remember { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        ),
        onPermissionsResult = {
            if (it[Manifest.permission.READ_CALENDAR]!! &&
                it[Manifest.permission.WRITE_CALENDAR]!!
            ) {
                permissionGranted = true
            }
        }
    )

    LaunchedEffect(true) {
        permissionsState.launchMultiplePermissionRequest()
    }

    if (permissionGranted) {
        events = getTodayCalendarEvents(context)
    }


    LazyColumn {
        items(events) { event ->
            val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val startTimeFormatted = dateFormat.format(Date(event.startTime))
            val endTimeFormatted = dateFormat.format(Date(event.endTime))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Title: ${event.title}", style = MaterialTheme.typography.titleSmall)
                Text(text = "Description: ${event.description}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "$startTimeFormatted - $endTimeFormatted ", style = MaterialTheme.typography.bodyMedium)
            }
            Divider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onShowDatePicker: () -> Unit,
    datePickerState: DatePickerState,
    context: Context
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    val selectedDate = datePickerState.selectedDateMillis?.let { Date(it) } ?: Calendar.getInstance().time

    var permissionGranted by remember { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        ),
        onPermissionsResult = {
            if (it[Manifest.permission.READ_CALENDAR]!! &&
                it[Manifest.permission.WRITE_CALENDAR]!!
            ) {
                permissionGranted = true
            }
        }
    )

    LaunchedEffect(true) {
        permissionsState.launchMultiplePermissionRequest()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Event") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                OutlinedButton(onClick = onShowDatePicker) {
                    Text("Select Date")
                }
                Text(text = "Selected Date: $selectedDate")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (permissionGranted) {
                        addEventToCalendar(context, title.text, description.text, selectedDate)

                    }
                    onDismiss()
                }
            ) {
                Text("Add")
            }
        }
    )
}

