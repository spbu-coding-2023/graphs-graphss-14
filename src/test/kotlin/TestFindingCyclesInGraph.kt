import algos.Graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestFindingCyclesInGraph {

    @Test
    fun `test findCyclesFromNode with a simple cycle`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 4)
        graph.addEdge(4, 2) // Создаем цикл 2 -> 3 -> 4 -> 2

        val cycles = graph.findCyclesFromNode(2)
        assertEquals(3, cycles.size)
        assertEquals(listOf(2, 3, 4), cycles.sortedBy { -it.size }[0])
    }

    @Test
    fun `test findCyclesFromNode with multiple cycles`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 1) // Создаем цикл 1 -> 2 -> 3 -> 1
        graph.addEdge(3, 4)
        graph.addEdge(4, 2) // Создаем цикл 2 -> 3 -> 4 -> 2

        val cycles = graph.findCyclesFromNode(1)
        assertEquals(2, cycles.size)
        assertEquals(listOf(1, 2, 3), cycles.sortedBy { -it.size }[0])
    }

    @Test
    fun `test findCyclesFromNode with no cycles`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 4)

        val cycles = graph.findCyclesFromNode(1)
        assertEquals(1, cycles.filter { it[0] == 1 }.size)
    }

    @Test
    fun `test findCyclesFromNode with a single node`() {
        val graph = Graph()
        graph.addNode(1)

        val cycles = graph.findCyclesFromNode(1)
        assertEquals(0, cycles.size)
    }

    @Test
    fun `test findCyclesFromNode with a disconnected graph`() {
        val graph = Graph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2)
        graph.addEdge(3, 4)

        val cycles = graph.findCyclesFromNode(1)
        assertEquals(1, cycles.size)
    }
}