package edu.utap.hex

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun shouldCalculateCorrectShortestPaths() {
        val vertices = setOf("A", "B", "C", "D", "E")
        val edges = mapOf(
            "A" to setOf("B", "C", "D"),
            "B" to setOf("C"),
            "C" to setOf("E"),
            "D" to setOf("E"),
            "E" to setOf(),
        )

        val graph = Graph(vertices, edges)
        var start = "A"
        var shortestPathTree = GraphUtil.dijkstra(graph, start)

        assertEquals(listOf(start, "C"),
            GraphUtil.shortestPath(shortestPathTree, start, "C")
        )
        assertEquals(listOf(start, "C", "E"),
            GraphUtil.shortestPath(shortestPathTree, start, "E")
        )
        assertEquals(listOf(start, "D"), GraphUtil.shortestPath(shortestPathTree, start, "D"))
        assert(GraphUtil.isPath(shortestPathTree, start, "E"))
        assert(GraphUtil.isPath(shortestPathTree, start, "D"))
        assert(GraphUtil.isPath(shortestPathTree, start, "C"))
        assert(GraphUtil.isPath(shortestPathTree, start, "B"))
        assert(GraphUtil.isPath(shortestPathTree, start, "A"))

        start = "E"
        shortestPathTree = GraphUtil.dijkstra(graph, start)
        assertEquals(listOf("E"), GraphUtil.shortestPath(shortestPathTree, start, "E"))
        assertEquals(listOf("D"), GraphUtil.shortestPath(shortestPathTree, start, "D"))
        assertEquals(listOf("C"), GraphUtil.shortestPath(shortestPathTree, start, "C"))
        assertEquals(listOf("B"), GraphUtil.shortestPath(shortestPathTree, start, "B"))
        assertEquals(listOf("A"), GraphUtil.shortestPath(shortestPathTree, start, "A"))
        assert(GraphUtil.isPath(shortestPathTree, start, "E"))
        assert(!GraphUtil.isPath(shortestPathTree, start, "D"))
        assert(!GraphUtil.isPath(shortestPathTree, start, "C"))
        assert(!GraphUtil.isPath(shortestPathTree, start, "B"))
        assert(!GraphUtil.isPath(shortestPathTree, start, "A"))
    }
}