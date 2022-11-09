package edu.utap.hex

data class Graph<T>(
    val vertices: Set<T>,
    val edges: Map<T, Set<T>>,
)
// Adapted from
// https://www.atomiccommits.io/dijkstras-algorithm-in-kotlin
class GraphUtil {
    companion object {
        fun <T> dijkstra(graph: Graph<T>, start: T): Map<T, T?> {
            val set: MutableSet<T> = mutableSetOf() // a subset of vertices, for which we know the true distance

            val delta = graph.vertices.associateWith { Int.MAX_VALUE }.toMutableMap()
            delta[start] = 0

            val previous: MutableMap<T, T?> = graph.vertices.associateWith { null }.toMutableMap()

            while (set != graph.vertices) {
                val v: T = delta
                    .filter { !set.contains(it.key) }
                    .minBy { it.value }
                    .key

                graph.edges.getValue(v).minus(set).forEach { neighbor ->
                    val newPath = delta.getValue(v)

                    if (newPath < delta.getValue(neighbor)) {
                        delta[neighbor] = newPath
                        previous[neighbor] = v
                    }
                }

                set.add(v)
            }

            return previous.toMap()
        }

        fun <T> shortestPath(shortestPathTree: Map<T, T?>, start: T, end: T): List<T> {
            fun pathTo(start: T, end: T): List<T> {
                if (shortestPathTree[end] == null) return listOf(end)
                return listOf(pathTo(start, shortestPathTree[end]!!), listOf(end)).flatten()
            }
            return pathTo(start, end)
        }
        fun <T> isPath(shortestPathTree: Map<T, T?>, start: T, end: T): Boolean {
            return start in shortestPath(shortestPathTree, start, end)
        }
    }
}