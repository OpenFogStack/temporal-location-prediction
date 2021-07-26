package me.mbe.prp.core

import java.time.Instant

class WorldState(val network: Network) {
    var time: Instant = Instant.MIN // is set by the simulation
        set(value) {
            network.time = value
            field = value
        }

    // val keyGroups: MutableMap<String, Keygroup> = LinkedHashMap() // are added by the creating simulation
    val users: MutableMap<String, User> = LinkedHashMap() // are added by the creating simulation

    //for backwards compatibility
    fun getClosestNode(user: User): Node = network.nodes.getClosestNode(user)

    // adds nodes; removes everything else
    fun setKeygroupMembers(kg: Keygroup, nodes: List<Node>) {
        nodes.forEach { network.addKeygroupMember(kg, it) }
        kg.members.values.filter { it.node !in nodes }.forEach { network.deleteKeygroupMember(kg, it.node) }
    }

    //for backwards compatibility
    fun isKeygroupMember(kg: Keygroup, node: Node): Boolean = network.isKeygroupMember(kg, node)

    //for backwards compatibility
    fun getKeygroup(kgName: String): Keygroup = network.getKeygroup(kgName)

    //for backwards compatibility
    val nodes: NodesGetter
        get() = network.nodes

}