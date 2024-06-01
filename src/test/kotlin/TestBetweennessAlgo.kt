import algos.Graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GraphTest {

    @Test
    fun testBetweennessCentrality() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(1, 2)
        graph.addEdge(1, 3)
        graph.addEdge(2, 3)
        graph.addEdge(3, 4)
        graph.addEdge(4, 5)

        val betweenness = graph.betweennessCentrality()

        assertEquals(0.0f, betweenness[1])
        assertEquals(0.0f, betweenness[2])
        assertEquals(1 + 1/3f, betweenness[3])
        assertEquals(1f, betweenness[4])
        assertEquals(0.0f, betweenness[5])
    }

    @Test
    fun testBetweennessCentralityWithCycle() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)

        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 4)
        graph.addEdge(4, 1)

        val betweenness = graph.betweennessCentrality()
        assertEquals(1f/3.0f, betweenness[1])
        assertEquals(1f/3.0f, betweenness[2])
        assertEquals(1f/3.0f, betweenness[3])
        assertEquals(1f/3.0f, betweenness[4])
    }

    @Test
    fun testBetweennessCentralityWithDisconnectedGraph() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(4, 5)

        val betweenness = graph.betweennessCentrality()

        assertEquals(0.0f, betweenness[1])
        assertEquals(1f/3.0f, betweenness[2])
        assertEquals(0.0f, betweenness[3])
        assertEquals(0.0f, betweenness[4])
        assertEquals(0.0f, betweenness[5])
    }
}