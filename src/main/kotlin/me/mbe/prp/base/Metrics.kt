package me.mbe.prp.base


object Metrics {
    private val values = LinkedHashMap<String, Int>()

    fun increment(s: String) = increment(s, 1)

    fun increment(s: String, n: Int) {
        values.merge(s, n, Int::plus)
    }

    fun print(vararg fns: (Map<String, Int>) -> Pair<String, Any>) {
        print()
        fns.forEach { fn ->
            val (n, v) = fn(values)
            println("$n: $v")
        }
    }

    fun print() {
        println("Metrics:")
        values.forEach { (k, v) ->
            println("$k: $v")
        }
    }

    fun reset() {
        values.clear()
    }
}