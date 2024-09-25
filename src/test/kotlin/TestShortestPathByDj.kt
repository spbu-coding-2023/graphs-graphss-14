import algos.WGraph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

class DjTest {

    @Test
    fun `test Shortest Path By Dj`() {
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
    fun `test Shortest Path By Dj With Negative Cycle`() {
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
    fun `test Shortest Path By Dj With Disconnected Graph`() {
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

    @Test
    fun `test shortest path from 0 to 5 with multiple paths`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(1, 3, 5)
        graph.addEdge(2, 3, 1)
        graph.addEdge(2, 4, 3)
        graph.addEdge(3, 4, 1)
        graph.addEdge(3, 5, 2)
        graph.addEdge(4, 5, 1)

        val result = graph.shortestPathD(0, 5)
        assertEquals(listOf(0, 1, 2, 3, 5), result)
    }

    @Test
    fun `test shortest path from 0 to 4 with different weights`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(1, 3, 5)
        graph.addEdge(2, 3, 1)
        graph.addEdge(2, 4, 3)
        graph.addEdge(3, 4, 1)

        val result = graph.shortestPathD(0, 4)
        assertEquals(listOf(0, 1, 2, 3, 4), result)
    }

    @Test
    fun `test no path from 5 to 0`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(1, 3, 5)
        graph.addEdge(2, 3, 1)
        graph.addEdge(4, 5, 1)

        val result = graph.shortestPathD(5, 0)
        assertNull(result)
    }

    @Test
    fun `test shortest path from 2 to 5 with cycle`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(1, 3, 5)
        graph.addEdge(2, 3, 1)
        graph.addEdge(2, 4, 3)
        graph.addEdge(3, 4, 1)
        graph.addEdge(3, 5, 2)
        graph.addEdge(4, 5, 1)

        val result = graph.shortestPathD(2, 5)
        assertEquals(listOf(2, 3, 5), result)
    }

    @Test
    fun `test shortest path from 1 to 5 with different paths`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(1, 3, 5)
        graph.addEdge(2, 3, 1)
        graph.addEdge(2, 4, 3)
        graph.addEdge(3, 4, 1)
        graph.addEdge(3, 5, 2)
        graph.addEdge(4, 5, 1)

        val result = graph.shortestPathD(1, 5)
        assertEquals(listOf(1, 2, 3, 5), result)
    }

    @Test
    fun `test shortest path from 0 to 3 with multiple edges`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(1, 3, 5)
        graph.addEdge(2, 3, 1)
        graph.addEdge(2, 4, 3)
        graph.addEdge(3, 4, 1)

        val result = graph.shortestPathD(0, 3)
        assertEquals(listOf(0, 1, 2, 3), result)
    }

    @Test
    fun `test shortest path from 3 to 5 with direct edge`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(1, 3, 5)
        graph.addEdge(2, 3, 1)
        graph.addEdge(2, 4, 3)
        graph.addEdge(3, 4, 1)
        graph.addEdge(3, 5, 2)
        graph.addEdge(4, 5, 1)

        val result = graph.shortestPathD(3, 5)
        assertEquals(listOf(3, 5), result)
    }

    @Test
    fun `test no path from 4 to 0`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(3, 4, 1)
        graph.addEdge(3, 5, 2)
        graph.addEdge(4, 5, 1)

        val result = graph.shortestPathD(4, 0)
        assertNull(result)
    }

    @Test
    fun `test shortest path from 0 to 2 with direct edge`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(1, 3, 5)
        graph.addEdge(2, 3, 1)
        graph.addEdge(2, 4, 3)
        graph.addEdge(3, 4, 1)

        val result = graph.shortestPathD(0, 2)
        assertEquals(listOf(0, 1, 2), result)
    }

    @Test
    fun `test shortest path from 2 to 4 with direct edge`() {
        val graph = WGraph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)

        graph.addEdge(0, 1, 1)
        graph.addEdge(0, 2, 4)
        graph.addEdge(1, 2, 2)
        graph.addEdge(1, 3, 5)
        graph.addEdge(2, 3, 1)
        graph.addEdge(2, 4, 3)
        graph.addEdge(3, 4, 1)

        val result = graph.shortestPathD(2, 4)
        assertEquals(listOf(2, 3, 4), result)
    }
}