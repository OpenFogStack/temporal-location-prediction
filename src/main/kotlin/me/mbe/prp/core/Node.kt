package me.mbe.prp.core

import java.time.Duration


class Node(override val name: String, override var location: Location, var freeCapacity: Capacity) : NamedPositioned {
    override fun toString() = name

    fun toPrintNode(): PrintNode {
        return PrintNode(name, location.latitudeDeg, location.longitudeDeg)
    }
}

fun List<Node>.toPrintNodes(): List<PrintNode> {
    return this.map { it.toPrintNode() }
}

data class NodeWithLatency(val node: Node, val latency: Duration)

data class PrintNode(val name: String, val lat: Double, val lon: Double)
