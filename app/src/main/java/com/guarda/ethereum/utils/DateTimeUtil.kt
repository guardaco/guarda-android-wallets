package com.guarda.ethereum.utils

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

object DateTimeUtil {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    fun dateFromTimestamp(timestamp: Long) : String {
        val dt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault())
        return dt.format(dateFormatter)
    }

    fun timeFromTimestamp(timestamp: Long) : String {
        val dt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault())
        return dt.format(timeFormatter)
    }

    fun parseToTimestamp(stringDateTime: String) : Long {
        val dateTime = ZonedDateTime.parse(stringDateTime)
        return dateTime.toEpochSecond()
    }

    fun parseToTimestampWithFormat(stringDateTime: String) : Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dateTime = ZonedDateTime.parse(stringDateTime, formatter)
        return dateTime.toEpochSecond()
    }

}