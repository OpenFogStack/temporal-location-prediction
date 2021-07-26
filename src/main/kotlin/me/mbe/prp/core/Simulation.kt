package me.mbe.prp.core

import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.collections.LinkedHashMap


data class AnnotatedSpaceTimeLocation(
    val location: SpaceTimeLocation,
    val beginOfTrip: Boolean,
    val endOfTrip: Boolean,
    val endOfSim: Boolean = false
)

data class NextSimTimeUserResult(val user: User, val aSTL: AnnotatedSpaceTimeLocation)

class Simulation(val stats: MetricsCollector, network: Network) {

    val currentState: WorldState = WorldState(network)

    val userLocations: MutableMap<User, Iterator<AnnotatedSpaceTimeLocation>> = LinkedHashMap()

    val nextUserLocations: PriorityQueue<NextSimTimeUserResult> =
        PriorityQueue { o1, o2 -> o1.aSTL.location.time.compareTo(o2.aSTL.location.time) }

    val algorithms: MutableMap<User, Algorithm> = LinkedHashMap()

    val timeCallbacks: PriorityQueue<Triple<Algorithm, Instant, String>> =
        PriorityQueue { o1, o2 -> o1.second.compareTo(o2.second) }

    fun nextSimTimeUser(): NextSimTimeUserResult? {
        val res = nextUserLocations.poll() ?: return null

        if (userLocations[res.user]!!.hasNext()) {
            nextUserLocations.offer(NextSimTimeUserResult(res.user, userLocations[res.user]!!.next()))
        }

        return res
    }

    fun registerTimeCallback(at: Instant, alg: Algorithm, value: String) {
        if (at < currentState.time) throw IllegalArgumentException()
        timeCallbacks.offer(Triple(alg, at, value))
    }

    fun cancelCallbacks(alg: Algorithm, prefix: String) {
        timeCallbacks.removeIf { it.first == alg && it.third.startsWith(prefix) }
    }
}


fun runSimulation(
    algorithmConstructor: AlgorithmConstructor,
    statsCollector: MetricsCollector,
    network: Network,
    users: Map<String, Sequence<AnnotatedSpaceTimeLocation>>,
    simName: String,
    printUserStats: Boolean,
) {

    //to make sure everything is in order
    val users2 = users.mapValues { q ->
        var onlineCheck = false
        q.value.onEach {
            if (it.beginOfTrip) {
                if (onlineCheck) throw RuntimeException(q.key)
                onlineCheck = true
            }

            if (!onlineCheck) throw RuntimeException(q.key)

            if (it.endOfTrip) {
                if (!onlineCheck) throw RuntimeException(q.key)
                onlineCheck = false
            }
        }.iterator()
    }

    val s = Simulation(statsCollector, network)

    for ((userName, locIterator) in users2) {
        val user = User(userName)
        s.currentState.users[user.name] = user
        s.userLocations[user] = locIterator

        network.addKeygroup(user.name)

        s.nextUserLocations.offer(NextSimTimeUserResult(user, s.userLocations[user]!!.next()))

        s.algorithms[user] = algorithmConstructor(AlgorithmParams(user, s::registerTimeCallback, s::cancelCallbacks))
    }

    while (true) {
        val (user, nextLoc) = s.nextSimTimeUser() ?: break

        while (true) {
            val tC = s.timeCallbacks.peek() ?: break
            if (tC.second.isAfter(nextLoc.location.time)) break

            network.advanceTimeBy(Duration.between(s.currentState.time, tC.second))

            s.currentState.time = tC.second
            tC.first.onTime(s.currentState, tC.third)
            s.stats.onTime(tC.first.p.user, s.currentState)

            s.timeCallbacks.remove()
        }

        network.advanceTimeBy(Duration.between(s.currentState.time, nextLoc.location.time))

        s.currentState.time = nextLoc.location.time
        val userAlg = s.algorithms[user]!!

        s.currentState.users[user.name]!!.location = nextLoc.location.location
        s.currentState.users[user.name]!!.lastUpdated = s.currentState.time

        if (nextLoc.beginOfTrip) {
            userAlg.onStartTrip(s.currentState)
            s.stats.onStartTrip(user, s.currentState)
        }

        userAlg.onNewPosition(s.currentState)
        s.stats.onNewPosition(user, s.currentState)

        if (nextLoc.endOfTrip) {
            userAlg.onEndTrip(s.currentState)
            s.stats.onEndTrip(user, s.currentState)
            if (nextLoc.endOfSim) {
                s.cancelCallbacks(userAlg, "")
                s.currentState.setKeygroupMembers(network.getKeygroup(user.name), listOf())
                s.stats.onEndSim(user, s.currentState, userAlg)
            }
        }
    }

    if (printUserStats) {
        for ((u, a) in s.algorithms) {
            println(u.name)
            a.printState()
        }
    }
    s.stats.printMetrics(simName, printUserStats)

}

