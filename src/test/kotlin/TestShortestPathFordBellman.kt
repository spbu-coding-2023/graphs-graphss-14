import algos.WGraph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
class WGraphTest {

    @Test
    fun testShortestPathBF() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(1, 2, 1)
        graph.addEdge(1, 3, 2)
        graph.addEdge(2, 3, 3)
        graph.addEdge(3, 4, 4)
        graph.addEdge(4, 5, 5)

        val shortestPath = graph.shortestPathBF(1, 5)
        assertEquals(listOf(1, 3, 4, 5), shortestPath)
    }

    @Test
    fun testShortestPathBFWithNegativeCycle() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)

        graph.addEdge(1, 2, -1)
        graph.addEdge(2, 3, -2)
        graph.addEdge(3, 1, -3)

        try {
            graph.shortestPathBF(1, 3)
        } catch (e: IllegalArgumentException) {
            assertEquals("Граф содержит отрицательный цикл", e.message)
        }
    }

    @Test
    fun testShortestPathBFWithDisconnectedGraph() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(1, 2, 1)
        graph.addEdge(2, 3, 2)
        graph.addEdge(4, 5, 3)

        val shortestPath = graph.shortestPathBF(1, 5)
        assertEquals(null, shortestPath)
    }
}