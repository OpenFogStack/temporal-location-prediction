package me.mbe.prp.simulation.helpers

import me.mbe.prp.base.Location
import me.mbe.prp.simulation.state.Node


//assumes earth is flat
//todo: wrap around at -+180
fun linSpaceNodes(nwCorner: Location, seCorner: Location, noNodesWE: Int, noNodesNS: Int): List<Node> {
    val nsDistance = nwCorner.latitudeDeg - seCorner.latitudeDeg
    val weDistance = nwCorner.longitudeDeg - seCorner.longitudeDeg

    val nsStepSize = nsDistance / noNodesNS
    val weStepSize = weDistance / noNodesWE

    val nodes = ArrayList<Node>(noNodesNS * noNodesWE)

    for (i in 0 until noNodesNS) {
        for (j in 0 until noNodesWE) {
            val name = "$i-$j"// i * noNodesWE + j
            nodes.add(
                Node(
                    name,
                    Location(nwCorner.latitudeDeg - i * nsStepSize, nwCorner.longitudeDeg - j * weStepSize),
                    1 * TERRA_BYTE,
                )
            )
        }
    }
    assert(nodes.size == noNodesNS * noNodesWE)

    return nodes
}
