package algos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestStronglyConnectedComponents{

    @Test
    fun `test Find SCCs`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 1)
        graph.addEdge(3, 4)
        graph.addEdge(4, 4)

        val sccs = graph.findSCCs()
        assertEquals(2, sccs.size)
        assertEquals(true, sccs.any { it.containsAll(listOf(1, 2, 3)) })
        assertEquals(true, sccs.any { it.containsAll(listOf(4)) })
    }

    @Test
    fun `test Find SCCs Empty Graph`() {
        val graph = DiGraph()
        val sccs = graph.findSCCs()
        assertEquals(0, sccs.size)
    }

    @Test
    fun `test Find SCCs Single Node`() {
        val graph = DiGraph()
        graph.addNode(1)
        val sccs = graph.findSCCs()
        assertEquals(1, sccs.size)
        assertEquals(true, sccs.any { it.contains(1) })
    }

    @Test
    fun `test Find SCCs Disconnected Graph`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addEdge(1, 2)
        graph.addEdge(2, 1)
        graph.addEdge(3, 3)

        val sccs = graph.findSCCs()
        assertEquals(2, sccs.size)
        assertEquals(true, sccs.any { it.containsAll(listOf(1, 2)) })
        assertEquals(true, sccs.any { it.containsAll(listOf(3)) })
    }

    @Test
    fun `test Find SCCs With Multiple SCCs`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)
        graph.addNode(6)
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 1)
        graph.addEdge(4, 5)
        graph.addEdge(5, 6)
        graph.addEdge(6, 4)

        val sccs = graph.findSCCs()
        assertEquals(2, sccs.size)
        assertEquals(true, sccs.any { it.containsAll(listOf(1, 2, 3)) })
        assertEquals(true, sccs.any { it.containsAll(listOf(4, 5, 6)) })
    }

    @Test
    fun `test Find SCCs With Cycle`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 1)

        val sccs = graph.findSCCs()
        assertEquals(1, sccs.size)
        assertEquals(true, sccs.any { it.containsAll(listOf(1, 2, 3)) })
    }

    @Test
    fun `test Find SCCs With No Edges`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)

        val sccs = graph.findSCCs()
        assertEquals(3, sccs.size)
        assertEquals(true, sccs.any { it.contains(1) })
        assertEquals(true, sccs.any { it.contains(2) })
        assertEquals(true, sccs.any { it.contains(3) })
    }

    @Test
    fun `test Find SCCs With Self Loops`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addEdge(1, 1)
        graph.addEdge(2, 2)
        graph.addEdge(3, 3)

        val sccs = graph.findSCCs()
        assertEquals(3, sccs.size)
        assertEquals(true, sccs.any { it.contains(1) })
        assertEquals(true, sccs.any { it.contains(2) })
        assertEquals(true, sccs.any { it.contains(3) })
    }

    @Test
    fun `test Find SCCs With Large Graph`() {
        val graph = DiGraph()
        val nodes = (1..100).toList()
        nodes.forEach { graph.addNode(it) }
        for (i in 0 until nodes.size - 1) {
            graph.addEdge(nodes[i], nodes[i + 1])
        }
        graph.addEdge(nodes.last(), nodes.first())

        val sccs = graph.findSCCs()
        assertEquals(1, sccs.size)
        assertEquals(true, sccs.any { it.containsAll(nodes) })
    }
}