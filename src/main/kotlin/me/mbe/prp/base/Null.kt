package me.mbe.prp.base


fun <T> T?.ifNullThrow(msg: String): T {
    if (this != null) return this
    throw IllegalArgumentException(msg)
}