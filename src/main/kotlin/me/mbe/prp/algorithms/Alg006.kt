package me.mbe.prp.algorithms


import me.mbe.prp.base.Algorithm
import me.mbe.prp.base.Location
import me.mbe.prp.base.NamedPositioned
import me.mbe.prp.base.Positioned
import me.mbe.prp.simulation.helpers.Grid
import me.mbe.prp.simulation.helpers.GridNodeGetter
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

class InnerGridCell(override val name: String, override var location: Location) : NamedPositioned

class Alg006(user: User) : Algorithm(user) {

    val m = HashMap<Node, Grid<InnerGridCell>>()

    val transitionTable = TransitionTable<String /*InnerGridCell name*/, Node>()

    var lastNode: Node? = null
    var lastInnerGridCell: String? = null

    override fun doWork(state: WorldState): Instant {
        val correctMembers = LinkedList<Node>()
        val kg = getKeyGroup(state)
        val closestNode = state.getClosestNode(user)

        correctMembers.add(closestNode)

        val nodeGetter = state.nodes as GridNodeGetter // this algorithm works only with GridNodeGetter

        val grid = m.getOrPut(closestNode, {
            val b = nodeGetter.grid.getBoundaries(closestNode)
            Grid(b.first, b.second, 4, 4) { n, l -> InnerGridCell(closestNode.name + ":" + n, l) }
        })

        if (lastNode != null && lastNode!! != closestNode) {
            transitionTable.addTransition(lastInnerGridCell!!, closestNode)
        }

        val innerGridCell = grid.getClosestCentre(user.location)

        val nextNode = transitionTable.getNext(innerGridCell.name)
        if (nextNode != null) {
            correctMembers.addAll(nextNode)
        }

        lastNode = closestNode
        lastInnerGridCell = innerGridCell.name
        state.setKeygroupMembers(kg, correctMembers)
        return state.time.plus(SECOND)
    }

}

