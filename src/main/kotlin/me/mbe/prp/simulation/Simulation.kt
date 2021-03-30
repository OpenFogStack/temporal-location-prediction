package me.mbe.prp.simulation

import me.mbe.prp.base.Algorithm
import me.mbe.prp.base.AlgorithmConstructor
import me.mbe.prp.base.SpaceTimeLocation
import me.mbe.prp.simulation.helpers.MEGA_BYTE
import me.mbe.prp.simulation.helpers.NodesGetter
import me.mbe.prp.simulation.state.Keygroup
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import me.mbe.prp.simulation.stats.StatsCollector
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.LinkedHashMap


class Simulation(val stats: StatsCollector, nodes: NodesGetter) {

    val currentState: WorldState = WorldState(nodes)

    val userLocations: MutableMap<User, Iterator<SpaceTimeLocation>> = LinkedHashMap()

    val nextUserLocations: PriorityQueue<Pair<User, SpaceTimeLocation>> =
        PriorityQueue { o1, o2 -> o1.second.time.compareTo(o2.second.time) }

    val algorithms: MutableMap<User, Algorithm> = LinkedHashMap()
    val algorithmsNextCall: MutableMap<User, Instant> = LinkedHashMap()

    fun nextSimTimeUser(): Pair<User, SpaceTimeLocation>? {
        val (user, nextLoc) = nextUserLocations.poll() ?: return null

        if (userLocations[user]!!.hasNext()) {
            nextUserLocations.offer(Pair(user, userLocations[user]!!.next()))
        }

        return Pair(user, nextLoc)
    }
}


fun runSimulation(
    algorithmConstructor: AlgorithmConstructor,
    statsCollector: StatsCollector,
    nodes: NodesGetter,
    users: Map<String /*userName*/, Iterator<SpaceTimeLocation>>,
    simName: String,
) {

    val s = Simulation(statsCollector, nodes)

    for ((userName, locIterator) in users) {
        val user = User(userName)
        s.currentState.users[user.name] = user
        s.userLocations[user] = locIterator
        s.currentState.keyGroups[user.name] =
            Keygroup(user.name, 100 * MEGA_BYTE, Duration.of(1, ChronoUnit.SECONDS)) //todo: config

        val uNext = s.userLocations[user]!!.next()
        s.nextUserLocations.offer(Pair(user, uNext))
        s.algorithmsNextCall[user] = uNext.time

        s.algorithms[user] = algorithmConstructor(user)
    }

    while (true) {
        val (user, nextLoc) = s.nextSimTimeUser() ?: break

        s.currentState.time = nextLoc.time
        s.currentState.users[user.name]!!.location = nextLoc.location
        s.currentState.users[user.name]!!.lastUpdated = s.currentState.time

        val nextCall = s.algorithmsNextCall[user]!!
        if (s.currentState.time.isAfter(nextCall) || s.currentState.time.equals(nextCall)) {
            s.algorithmsNextCall[user] = s.algorithms[user]!!.doWork(s.currentState)
        }

        s.stats.collect(user, s.currentState)
    }

    for ((u, a) in s.algorithms) {
        println(u.name)
        a.printState()
    }

    s.stats.printStats(simName)

}

