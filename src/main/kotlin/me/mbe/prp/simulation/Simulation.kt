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


data class AnnotatedSpaceTimeLocation(
    val location: SpaceTimeLocation,
    val beginOfTrip: Boolean,
    val endOfTrip: Boolean
)

data class NextSimTimeUserResult(val user: User, val aSTL: AnnotatedSpaceTimeLocation)

class Simulation(val stats: StatsCollector, nodes: NodesGetter) {

    val currentState: WorldState = WorldState(nodes)

    val userLocations: MutableMap<User, Iterator<AnnotatedSpaceTimeLocation>> = LinkedHashMap()

    val nextUserLocations: PriorityQueue<NextSimTimeUserResult> =
        PriorityQueue { o1, o2 -> o1.aSTL.location.time.compareTo(o2.aSTL.location.time) }

    val algorithms: MutableMap<User, Algorithm> = LinkedHashMap()

    val timeCallbacks: PriorityQueue<Pair<Algorithm, Instant>> =
        PriorityQueue { o1, o2 -> o1.second.compareTo(o2.second) }

    fun nextSimTimeUser(): NextSimTimeUserResult? {
        val res = nextUserLocations.poll() ?: return null

        if (userLocations[res.user]!!.hasNext()) {
            nextUserLocations.offer(NextSimTimeUserResult(res.user, userLocations[res.user]!!.next()))
        }

        return res
    }

    fun registerTimeCallback(at: Instant, alg: Algorithm) {
        timeCallbacks.offer(Pair(alg, at))
    }
}


fun runSimulation(
    algorithmConstructor: AlgorithmConstructor,
    statsCollector: StatsCollector,
    nodes: NodesGetter,
    users: Map<String /*userName*/, Iterator<AnnotatedSpaceTimeLocation>>,
    simName: String,
) {

    val s = Simulation(statsCollector, nodes)

    for ((userName, locIterator) in users) {
        val user = User(userName)
        s.currentState.users[user.name] = user
        s.userLocations[user] = locIterator
        s.currentState.keyGroups[user.name] =
            Keygroup(user.name, 100 * MEGA_BYTE, Duration.of(1, ChronoUnit.SECONDS)) //todo: config

        s.nextUserLocations.offer(NextSimTimeUserResult(user, s.userLocations[user]!!.next()))

        s.algorithms[user] = algorithmConstructor(user, s)
    }

    while (true) {
        val (user, nextLoc) = s.nextSimTimeUser() ?: break

        while (true) {
            val tC = s.timeCallbacks.peek() ?: break
            if (tC.second.isAfter(nextLoc.location.time)) break

            s.currentState.time = tC.second
            tC.first.onTime(s.currentState)
            s.stats.onTime(tC.first.user, s.currentState)

            s.timeCallbacks.remove()
        }

        s.currentState.time = nextLoc.location.time
        val userAlg = s.algorithms[user]!!

        if (nextLoc.beginOfTrip) {
            userAlg.onStartTrip(s.currentState)
            s.stats.onStartTrip(user, s.currentState)
        }

        s.currentState.users[user.name]!!.location = nextLoc.location.location
        s.currentState.users[user.name]!!.lastUpdated = s.currentState.time

        userAlg.onNewPosition(s.currentState)
        s.stats.onNewPosition(user, s.currentState)

        if (nextLoc.endOfTrip) {
            userAlg.onEndTrip(s.currentState)
            s.stats.onEndTrip(user, s.currentState)
        }
    }

    for ((u, a) in s.algorithms) {
        println(u.name)
        a.printState()
    }

    s.stats.printStats(simName)

}

