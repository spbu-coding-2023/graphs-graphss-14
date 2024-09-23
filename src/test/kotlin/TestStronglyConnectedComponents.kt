package algos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestStronglyConnectedComponents{

    @Test
    fun `test Add Node`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        assertEquals(2, graph.nodes.size)
        assertEquals(true, graph.nodes.contains(1))
        assertEquals(true, graph.nodes.contains(2))
    }

    @Test
    fun `test Add Edge`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addEdge(1, 2)
        assertEquals(1, graph.edges.size)
        assertEquals(true, graph.edges.contains(Pair(1, 2)))
        assertEquals(1, graph.adjacencyList[1]?.size)
        assertEquals(true, graph.adjacencyList[1]?.contains(2))
    }

    @Test
    fun `test Transpose`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addEdge(1, 2)
        val transposedGraph = graph.transpose()
        assertEquals(1, transposedGraph.edges.size)
        assertEquals(true, transposedGraph.edges.contains(Pair(2, 1)))
        assertEquals(1, transposedGraph.adjacencyList[2]?.size)
        assertEquals(true, transposedGraph.adjacencyList[2]?.contains(1))
    }

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

        val sccs = findSCCs(graph)
        assertEquals(2, sccs.size)
        assertEquals(true, sccs.any { it.containsAll(listOf(1, 2, 3)) })
        assertEquals(true, sccs.any { it.containsAll(listOf(4)) })
    }

    @Test
    fun `test Find SCCs Empty Graph`() {
        val graph = DiGraph()
        val sccs = findSCCs(graph)
        assertEquals(0, sccs.size)
    }

    @Test
    fun `test Find SCCs Single Node`() {
        val graph = DiGraph()
        graph.addNode(1)
        val sccs = findSCCs(graph)
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

        val sccs = findSCCs(graph)
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

        val sccs = findSCCs(graph)
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

        val sccs = findSCCs(graph)
        assertEquals(1, sccs.size)
        assertEquals(true, sccs.any { it.containsAll(listOf(1, 2, 3)) })
    }

    @Test
    fun `test Find SCCs With No Edges`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)

        val sccs = findSCCs(graph)
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

        val sccs = findSCCs(graph)
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

        val sccs = findSCCs(graph)
        assertEquals(1, sccs.size)
        assertEquals(true, sccs.any { it.containsAll(nodes) })
    }
}