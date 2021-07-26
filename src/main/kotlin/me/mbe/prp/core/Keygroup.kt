package me.mbe.prp.core

class Keygroup(val name: String, val size: Capacity) {
    val members: MutableMap<String, KeygroupMember> = LinkedHashMap()
}