package me.mbe.prp.network

typealias Bandwidth = Long //in BYTE_PER_SECOND

const val BYTE_PER_SECOND: Bandwidth = 1L
const val KILO_BYTE_PER_SECOND: Bandwidth = 1024 * BYTE_PER_SECOND
const val MEGA_BYTE_PER_SECOND: Bandwidth = 1024 * KILO_BYTE_PER_SECOND
const val GIGA_BYTE_PER_SECOND: Bandwidth = 1024 * MEGA_BYTE_PER_SECOND
const val TERRA_BYTE_PER_SECOND: Bandwidth = 1024 * GIGA_BYTE_PER_SECOND
