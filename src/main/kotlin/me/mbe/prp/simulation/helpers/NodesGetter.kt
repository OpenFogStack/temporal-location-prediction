package me.mbe.prp.simulation.helpers

import me.mbe.prp.base.Location
import me.mbe.prp.base.NamedPositioned
import me.mbe.prp.base.Positioned
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.NodeWithLatency
import java.io.File
import java.time.Duration
import java.util.Arrays
import kotlin.collections.HashMap

abstract class NodesGetter {
    protected abstract fun getClosestNodeInternal(p: Location): Node

    abstract val nodes: List<Node>


    private var cacheKey: Location? = null
    private var cacheValue: Node? = null


    fun getClosestNode(p: Positioned): Node {
        if (p.location != cacheKey) {
            cacheKey = p.location
            cacheValue = getClosestNodeInternal(p.location)
        }
        return cacheValue!!
    }


    // fun getClosestNode(p: Positioned): Node = getClosestNodeInternal(p)
}


class SimpleNodesGetter(
    override val nodes: List<Node>,
    private val latencyFunction: LatencyFunction = ::simpleLatencyCalculation,
) : NodesGetter() {

    override fun getClosestNodeInternal(p: Location): Node {
        return getNodesWithLatency(p).minByOrNull { it.latency }!!.node
    }

    fun getKClosestNodes(p: Location, k: Int): List<NodeWithLatency> {
        return getNodesWithLatency(p).sortedBy { it.latency }.take(k)
    }

    private fun getNodesWithLatency(p: Location): List<NodeWithLatency> {
        return nodes.map { node -> NodeWithLatency(node, latencyFunction(node.location.distance(p))) }
    }
}

class Grid<T : NamedPositioned>(
    private val nwCorner: Location,
    private val seCorner: Location,
    private val noCentresWE: Int,
    private val noCentresNS: Int,
    centreCreator: (String, Location) -> T
) {
    val centres: List<T>

    private val centresMap: Map<Pair<Int, Int>, T>
    private val gridLinesSN: DoubleArray // latitudes
    private val gridLinesWE: DoubleArray // longitudes

    private val snStepSize: Double
    private val weStepSize: Double

    init {
        val snDistance = seCorner.latitudeDeg - nwCorner.latitudeDeg
        val weDistance = nwCorner.longitudeDeg - seCorner.longitudeDeg
        snStepSize = snDistance / (noCentresNS) // negative in Beijing
        weStepSize = weDistance / (noCentresWE) // negative in Beijing
        gridLinesSN = (1 until noCentresNS).map { seCorner.latitudeDeg - it * snStepSize }.toDoubleArray()
        gridLinesWE = (1 until noCentresWE).map { nwCorner.longitudeDeg - it * weStepSize }.toDoubleArray()
        centresMap = HashMap()
        for (i in 0 until noCentresNS) {
            for (j in 0 until noCentresWE) {
                val name = "$i-$j"
                centresMap[Pair(i, j)] = centreCreator(
                    name,
                    Location(
                        seCorner.latitudeDeg - (i + 0.5) * snStepSize,
                        nwCorner.longitudeDeg - (j + 0.5) * weStepSize
                    ),
                )
            }
        }
        centres = centresMap.values.toList()
        assert(centres.size == noCentresNS * noCentresWE)
    }

    //todo: dateline wrap not considered
    private fun getClosestCentreIdx(p: Location): Pair<Int, Int> {
        //SN ; latitude
        val i = getIndex(gridLinesSN, p.latitudeDeg)
        //WE ; longitude
        val j = getIndex(gridLinesWE, p.longitudeDeg)

        return Pair(i, j)
    }

    fun getClosestCentre(p: Location): T {
        return centresMap[getClosestCentreIdx(p)]!!
    }

    private fun getIndex(gridLines: DoubleArray, deg: Double): Int {
        val j = Arrays.binarySearch(gridLines, deg)
        if (j >= 0) {
            //auf der linie
            return j
        } else {
            return (j + 1) * (-1)
        }
    }

    fun getBoundaries(n: T): Pair<Location /* nwCorner */, Location /* seCorner */> {
        val nwCorner = Location(n.location.latitudeDeg - snStepSize, n.location.longitudeDeg + weStepSize)
        val seCorner = Location(n.location.latitudeDeg + snStepSize, n.location.longitudeDeg - weStepSize)
        return Pair(nwCorner, seCorner)
    }

    fun getKMLGrid(): String {
        val sb = StringBuilder()

        sb.append(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <kml xmlns="http://www.opengis.net/kml/2.2" xmlns:gx="http://www.google.com/kml/ext/2.2" xmlns:kml="http://www.opengis.net/kml/2.2" xmlns:atom="http://www.w3.org/2005/Atom">
            <Document>
                <name>Grid</name>
        """.trimIndent()
        )

        gridLinesSN.forEach {
            sb.append(
                """
                <Placemark>
                    <name>P</name>
                    <styleUrl>#m_ylw-pushpin</styleUrl>
                    <LineString>
                        <tessellate>1</tessellate>
                        <coordinates>
                             ${nwCorner.longitudeDeg},$it,0  ${seCorner.longitudeDeg},$it,0 
                        </coordinates>
                    </LineString>
                </Placemark>  
            """.trimIndent()
            )
        }

        gridLinesWE.forEach {
            sb.append(
                """
                <Placemark>
                    <name>P</name>
                    <styleUrl>#m_ylw-pushpin</styleUrl>
                    <LineString>
                        <tessellate>1</tessellate>
                        <coordinates>
                            $it,${nwCorner.latitudeDeg},0 $it,${seCorner.latitudeDeg},0 
                        </coordinates>
                    </LineString>
                </Placemark>  
            """.trimIndent()
            )
        }

        centres.forEach {
            sb.append(
                """
                <Placemark>
                    <name>${it.name}</name>
                    <styleUrl>#m_ylw-pushpin</styleUrl>
                    <Point>
                        <gx:drawOrder>1</gx:drawOrder>
                        <coordinates>${it.location.longitudeDeg},${it.location.latitudeDeg},0</coordinates>
                    </Point>
                </Placemark>
            """.trimIndent()
            )
        }

        sb.append(
            """
             </Document>
            </kml>  
        """.trimIndent()
        )


        return sb.toString()
    }

    fun neighbors(closestNode: T): List<T> {
        val n = ArrayList<T>()

        val cNIdx = getClosestCentreIdx(closestNode.location)

        listOf(-1, 0, 1).forEach { i ->
            listOf(-1, 0, 1).forEach { j ->
                val p = Pair(cNIdx.first + i, cNIdx.second + j)
                if (p != cNIdx && p.first in 0 until noCentresNS && p.second in 0 until noCentresWE) {
                    n.add(centresMap[p]!!)
                }
            }
        }

        return n
    }

}


class GridNodeGetter(nwCorner: Location, seCorner: Location, noNodesWE: Int, noNodesNS: Int) : NodesGetter() {

    val grid = Grid(nwCorner, seCorner, noNodesWE, noNodesNS) { name, loc -> Node(name, loc, TERRA_BYTE) }

    init {
        File("./stats-out/nodes.kml").writeText(grid.getKMLGrid())
    }

    override fun getClosestNodeInternal(p: Location): Node = grid.getClosestCentre(p)

    override val nodes: List<Node>
        get() = grid.centres
}

class ShanghaiNodesGetter(
    override val nodes: List<Node>,
) : NodesGetter() {

    private val nodeMap: Map<Location, Node> = nodes.associateBy { it.location }

    override fun getClosestNodeInternal(p: Location): Node {
        return nodeMap[p]!!
    }
}


class LighthouseNodesGetter(
    override val nodes: List<Node>,
    lighthouseMaxDistance: Duration = Duration.ofNanos(50 * 1000),
    latencyFunction: LatencyFunction = ::simpleLatencyCalculation,
) : NodesGetter() {

    private val lighthouse = Lighthouse(
        nodes,
        { x1, x2 -> latencyFunction(x1.distance(x2)) },
        { Pair(it - lighthouseMaxDistance, it + lighthouseMaxDistance) },
    )

    override fun getClosestNodeInternal(p: Location): Node {
        val r = lighthouse.getClosest(p)
        return r.first
    }
}