package hu.ait.familia.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun getChatTimeDisplay(chatDateString: String): String {
    val currentCalendar = Calendar.getInstance()
    val chatDateCalendar = Calendar.getInstance()

    // Assuming the chatDateString is in the format "yyyy-MM-dd'T'HH:mm:ss" (e.g., ISO 8601)
    val chatDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    chatDateCalendar.time = chatDateFormat.parse(chatDateString) ?: return "Invalid Date"

    // Get the time difference in milliseconds
    val timeDifference = currentCalendar.timeInMillis - chatDateCalendar.timeInMillis

    // Time difference thresholds
    val oneDayMillis = 24 * 60 * 60 * 1000
    val twoDayMillis = 2 * oneDayMillis

    return when {
        timeDifference < oneDayMillis -> {
            // Chat was within the last 24 hours, show time in 12-hour format
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(chatDateCalendar.time)
        }
        timeDifference < twoDayMillis -> {
            // Chat was between 24 and 48 hours ago, show "Yesterday"
            "Yesterday"
        }
        else -> {
            // Chat was more than 48 hours ago, show date in day/month/year format
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(chatDateCalendar.time)
        }
    }
}

private fun getFirstCalendarId(context: Context): Long? {
    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
    )
    val cursor: Cursor? = context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        null,
        null,
        null
    )
    cursor?.use {
        if (it.moveToFirst()) {
            val idIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            return it.getLong(idIndex)
        }
    }
    return null
}

fun addEventToCalendar(context: Context, title: String, description: String, startDate: Date) {
    val calendarId = getFirstCalendarId(context)
    if (calendarId != null) {
        val contentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startDate.time)
            put(CalendarContract.Events.DTEND, startDate.time + 60 * 60 * 1000) // 1 hour duration
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
    } else {
        // Handle the case where no calendar was found
    }
}

data class Event(val id: Long, val title: String, val description: String, val start: Long, val end: Long)

fun getTodayCalendarEvents(context: Context): List<CalendarEvent> {
    val events = mutableListOf<CalendarEvent>()

    // Get the start and end time of today
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startTime = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endTime = calendar.timeInMillis

    // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
    val projection = arrayOf(
        CalendarContract.Events._ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.DESCRIPTION
    )

    // Define the selection criteria
    val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
    val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

    // Query the calendar provider
    val cursor: Cursor? = context.contentResolver.query(
        CalendarContract.Events.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        CalendarContract.Events.DTSTART + " ASC"
    )

    cursor?.use {
        val idIndex = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
        val titleIndex = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
        val dtstartIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
        val dtendIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
        val locationIndex = it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)
        val descriptionIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)

        while (it.moveToNext()) {
            val id = it.getLong(idIndex)
            val title = it.getString(titleIndex) ?: "No Title"
            val dtstart = it.getLong(dtstartIndex)
            val dtend = it.getLong(dtendIndex)
            val location = it.getString(locationIndex) ?: "No Location"
            val description = it.getString(descriptionIndex) ?: "No Description"

            events.add(CalendarEvent(id, title, dtstart, dtend, location, description))
        }
    }

    return events
}

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val location: String,
    val description: String
)