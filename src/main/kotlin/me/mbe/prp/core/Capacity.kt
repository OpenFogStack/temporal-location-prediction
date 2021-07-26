package me.mbe.prp.core

typealias Capacity = Long

const val ZERO_BYTE: Capacity = 0L
const val BYTE: Capacity = 1L
const val KILO_BYTE: Capacity = 1024 * BYTE
const val MEGA_BYTE: Capacity = 1024 * KILO_BYTE
const val GIGA_BYTE: Capacity = 1024 * MEGA_BYTE
const val TERRA_BYTE: Capacity = 1024 * GIGA_BYTE

class CapacityError : Throwable("not enough capacity on this node")
