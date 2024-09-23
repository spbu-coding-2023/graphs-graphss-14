import algos.Graph
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestFindBridges {

    @Test
    fun `test findBridges with no bridges`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 1)

        val bridges = graph.findBridges()
        assertTrue(bridges.isEmpty())
    }

    @Test
    fun `test findBridges with a single bridge`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 4)
        graph.addEdge(4, 2)

        val bridges = graph.findBridges()
        assertEquals(1, bridges.size)
        assertTrue(bridges.contains(Pair(1, 2)))
    }

    @Test
    fun `test findBridges with multiple bridges`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 4)
        graph.addEdge(4, 5)
        graph.addEdge(2, 4)

        val bridges = graph.findBridges()
        assertEquals(2, bridges.size)
        assertTrue(bridges.contains(Pair(1, 2)))
        assertTrue(bridges.contains(Pair(4, 5)))
    }

    @Test
    fun `test findBridges with isolated nodes`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addEdge(1, 2)

        val bridges = graph.findBridges()
        assertEquals(1, bridges.size)
        assertTrue(bridges.contains(Pair(1, 2)))
    }

    @Test
    fun `test findBridges with self-loop`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addEdge(1, 1)

        val bridges = graph.findBridges()
        assertTrue(bridges.isEmpty())
    }

    @Test
    fun `test findBridges with disconnected graph`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2)
        graph.addEdge(3, 4)

        val bridges = graph.findBridges()
        assertEquals(2, bridges.size)
        assertTrue(bridges.contains(Pair(1, 2)))
        assertTrue(bridges.contains(Pair(3, 4)))
    }
}