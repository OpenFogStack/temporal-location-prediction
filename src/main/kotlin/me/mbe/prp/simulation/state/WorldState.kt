package me.mbe.prp.simulation.state

import me.mbe.prp.simulation.helpers.CapacityError
import me.mbe.prp.simulation.helpers.NodesGetter
import java.time.Instant

class WorldState(val nodes: NodesGetter) {
    var time: Instant = Instant.MIN // is set by the simulation

    val keyGroups: MutableMap<String, Keygroup> = LinkedHashMap() // are added by the creating simulation
    val users: MutableMap<String, User> = LinkedHashMap() // are added by the creating simulation

    //for backwards compatibility
    fun getClosestNode(user: User): Node = nodes.getClosestNode(user)

    // adds nodes; removes everything else
    fun setKeygroupMembers(kg: Keygroup, nodes: List<Node?>) {
        val n = nodes.filterNotNull()
        n.forEach { addKeygroupMember(kg, it) }
        kg.members.values.filter { it.node !in n }.forEach { deleteKeygroupMember(kg, it.node) }
    }

    private fun addKeygroupMember(kg: Keygroup, node: Node) {
        if (kg.members.contains(node.name)) return
        if (kg.size > node.freeCapacity) throw CapacityError()

        node.freeCapacity -= kg.size

        kg.members[node.name] = KeygroupMember(node, time.plus(kg.transferTime))
    }

    private fun deleteKeygroupMember(kg: Keygroup, node: Node) {
        kg.members.remove(node.name) ?: return
        node.freeCapacity += kg.size
    }

    fun isKeygroupMember(kg: Keygroup, node: Node): Boolean {
        val m = kg.members[node.name] ?: return false
        return !time.isBefore(m.availableFrom)
    }
}