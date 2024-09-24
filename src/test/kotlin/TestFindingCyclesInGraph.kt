import algos.DiGraph
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
        assertEquals(1, cycles.size)
        assertEquals(listOf(2, 3, 4), cycles.sortedBy { -it.size }[0])
    }

    @Test
    fun `test findCyclesFromNode with multiple cycles`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2, 1) // Забыл добавить веса для ребер. Бывает)
        graph.addEdge(2, 3, 1)
        graph.addEdge(3, 1, 1) // Создаем цикл 1 -> 2 -> 3 -> 1
        graph.addEdge(3, 4, 1)
        graph.addEdge(4, 2, 1) // Создаем цикл 2 -> 3 -> 4 -> 2

        val cycles = graph.findCyclesFromNode(2)
        assertEquals(2, cycles.size)
        assertEquals(listOf(2, 3, 1), cycles.sortedBy { it.size }[0])
        assertEquals(listOf(2, 3, 4), cycles.sortedBy { it.size }[1])
    }

    @Test
    fun `test findCyclesFromNode with no cycles`() {
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2, 1)
        graph.addEdge(2, 3, 1)
        graph.addEdge(3, 4, 1)

        val cycles = graph.findCyclesFromNode(1)
        assertEquals(0, cycles.filter { it[0] == 1 }.size)
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
        val graph = DiGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addEdge(1, 2, 1)
        graph.addEdge(3, 4, 1)

        val cycles = graph.findCyclesFromNode(1)
        assertEquals(0, cycles.size)
    }
}