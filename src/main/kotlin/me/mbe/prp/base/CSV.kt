package me.mbe.prp.base

import com.floern.castingcsv.castingCSV
import com.floern.castingcsv.typeadapter.TypeAdapter
import java.io.File
import java.io.FileOutputStream
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

inline fun <reified T : Any> writeCsv(l: List<T>, fileName: String) {
    val file = File(fileName)
    file.parentFile.mkdirs()
    castingCSV().toCSV(l, FileOutputStream(file))
}

class DateTimeAdapter : TypeAdapter<OffsetDateTime>() {
    override fun serialize(value: OffsetDateTime?): String? =
        value?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    override fun deserialize(token: String): OffsetDateTime? =
        OffsetDateTime.parse(token, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}