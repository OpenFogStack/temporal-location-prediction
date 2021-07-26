package me.mbe.prp.base

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset


fun parseDateTime(date: String, time: String): Instant {
    return OffsetDateTime.of(
        date.substring(0, 4).toInt(), date.substring(5, 7).toInt(), date.substring(8, 10).toInt(),
        time.substring(0, 2).toInt(), time.substring(3, 5).toInt(), time.substring(6, 8).toInt(),
        0, ZoneOffset.UTC
    ).toInstant()

}