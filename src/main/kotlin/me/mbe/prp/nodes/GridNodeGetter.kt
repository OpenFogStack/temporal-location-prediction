package me.mbe.prp.nodes

import me.mbe.prp.core.*
import java.io.File
import java.util.*


class Grid<T : NamedPositioned>(
    private val nwCorner: Location,
    private val seCorner: Location,
    val noCentresWE: Int,
    val noCentresNS: Int,
    centreCreator: (String, Location) -> T
) {
    val centres: List<T>

    val centresMap: Map<Pair<Int, Int>, T>
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

    fun nextInDirection(last: Pair<T, T>): T {
        val idx1 = getClosestCentreIdx(last.first.location)
        val idx2 = getClosestCentreIdx(last.second.location)

        val idxDiff = Pair(idx2.first - idx1.first, idx2.second - idx1.second)

        var idx3First = idx2.first + idxDiff.first
        var idx3Second = idx2.second + idxDiff.second

        if (idx3First < 0) {
            idx3First = 0
        } else if (idx3First >= noCentresNS) {
            idx3First = noCentresNS - 1
        }

        if (idx3Second < 0) {
            idx3Second = 0
        } else if (idx3Second >= noCentresWE) {
            idx3Second = noCentresWE - 1
        }

        val idx3 = Pair(idx3First, idx3Second)
        return centresMap[idx3]!!
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
