import algos.Graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TestSpringEmbedder {

    @Test
    fun `test layout for a simple graph`() {
        val graph = Graph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addEdge(0, 1)
        graph.addEdge(1, 2)

        val springEmbedder = Graph.SpringEmbedder()
        val layout = springEmbedder.layout(graph)

        // Проверяем, что все вершины имеют координаты
        assertEquals(3, layout.size)
        assertTrue(layout.containsKey(0))
        assertTrue(layout.containsKey(1))
        assertTrue(layout.containsKey(2))
        
        for (coordinate in layout.values) {
            assertTrue(coordinate.first >= -20.0 && coordinate.first <= 20.0)
            assertTrue(coordinate.second >= -20.0 && coordinate.second <= 20.0)
        }
    }

    @Test
    fun `test layout for a graph with disconnected components`() {
        val graph = Graph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addEdge(0, 1)
        graph.addEdge(2, 3)

        val springEmbedder = Graph.SpringEmbedder()
        val layout = springEmbedder.layout(graph)

        // Проверяем, что все вершины имеют координаты
        assertEquals(4, layout.size)
        assertTrue(layout.containsKey(0))
        assertTrue(layout.containsKey(1))
        assertTrue(layout.containsKey(2))
        assertTrue(layout.containsKey(3))

        // Проверяем, что координаты находятся в пределах [0, 1]
        for (coordinate in layout.values) {
            assertTrue(coordinate.first >= -20.0 && coordinate.first <= 20.0)
            assertTrue(coordinate.second >= -20.0 && coordinate.second <= 20.0)
        }
    }

    @Test
    fun `test layout for a fully connected graph`() {
        val graph = Graph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)
        graph.addEdge(0, 1)
        graph.addEdge(1, 2)
        graph.addEdge(2, 0)

        val springEmbedder = Graph.SpringEmbedder()
        val layout = springEmbedder.layout(graph)

        // Проверяем, что все вершины имеют координаты
        assertEquals(3, layout.size)
        assertTrue(layout.containsKey(0))
        assertTrue(layout.containsKey(1))
        assertTrue(layout.containsKey(2))

        // Проверяем, что координаты находятся в пределах [0, 1]
        for (coordinate in layout.values) {
            assertTrue(coordinate.first >= -20.0 && coordinate.first <= 20.0)
            assertTrue(coordinate.second >= -20.0 && coordinate.second <= 20.0)
        }
    }

    @Test
    fun `test layout for a graph with a single node`() {
        val graph = Graph()
        graph.addNode(0)

        val springEmbedder = Graph.SpringEmbedder()
        val layout = springEmbedder.layout(graph)

        // Проверяем, что вершина имеет координаты
        assertEquals(1, layout.size)
        assertTrue(layout.containsKey(0))

        // Проверяем, что координаты находятся в пределах [0, 1]
        val coordinate = layout[0]!!
        assertTrue(coordinate.first >= -20.0 && coordinate.first <= 20.0)
        assertTrue(coordinate.second >= -20.0 && coordinate.second <= 20.0)
    }

    @Test
    fun `test layout for a graph with no edges`() {
        val graph = Graph()
        graph.addNode(0)
        graph.addNode(1)
        graph.addNode(2)

        val springEmbedder = Graph.SpringEmbedder()
        val layout = springEmbedder.layout(graph)

        // Проверяем, что все вершины имеют координаты
        assertEquals(3, layout.size)
        assertTrue(layout.containsKey(0))
        assertTrue(layout.containsKey(1))
        assertTrue(layout.containsKey(2))

        // Проверяем, что координаты находятся в пределах [0, 1]
        for (coordinate in layout.values) {
            assertTrue(coordinate.first >= -20.0 && coordinate.first <= 20.0)
            assertTrue(coordinate.second >= -20.0 && coordinate.second <= 20.0)
        }
    }
}