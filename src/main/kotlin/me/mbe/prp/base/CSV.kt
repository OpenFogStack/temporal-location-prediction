package me.mbe.prp.base

import java.io.File
import kotlin.reflect.full.memberProperties

/*
inline fun <reified T : Any> writeCsv(l: List<T>, fileName: String) {
    val file = File(fileName)
    file.parentFile.mkdirs()
    castingCSV().toCSV(l, FileOutputStream(file))
}

class DateTimeAdapter : TypeAdapter<Instant>() {
    override fun serialize(value: Instant?): String? =
        value?.atOffset(ZoneOffset.UTC)?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    override fun deserialize(token: String): Instant? =
        OffsetDateTime.parse(token, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
}

class NodeAdapter : TypeAdapter<Node>() {
    override fun serialize(value: Node?): String? = value?.toString()

    override fun deserialize(token: String): Node? = null
}
*/

fun <T> writeCsv(l: List<T>, fileName: String) {
    val file = File(fileName)
    file.parentFile.mkdirs()

    val out = file.outputStream().bufferedWriter()

    val obj1 = l[0]!!
    val props = obj1.javaClass.kotlin.memberProperties

    val header = props.joinToString(",") { it.name }
    out.write(header)
    out.write("\n")

    l.forEach { obj ->
        out.write(props.joinToString(",") { it.get(obj!!).toString() })
        out.write("\n")
    }

    out.close()
}