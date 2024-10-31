import algos.WGraph
import org.junit.jupiter.api.Test

class TestSpanningTree {
    @Test
    fun easyTest() {
        val graph = WGraph()
        for (i in 1..4) {
            graph.addNode(i)
        }
        graph.addEdge(1, 2, 1)
        graph.addEdge(2, 3, 1)
        graph.addEdge(3, 4, 1)
        graph.addEdge(1, 4, 1)
        var good = true
        val t = graph.getSpanningTree().edges
        val right: MutableList<Pair<Int, Int>> = mutableListOf(Pair(1, 2), Pair(2, 3), Pair(3, 4))
        for (i in right) {
            if (Pair(i.first, i.second) in t || Pair(i.second, i.first) in t) {
                continue
            }
            good = false
        }
        assert(good)
    }

    @Test
    fun weightedTest() {
        val graph = WGraph()
        for (i in 1..3) {
            graph.addNode(i)
        }
        graph.addEdge(1, 2, 1)
        graph.addEdge(2, 3, 2)
        graph.addEdge(3, 1, 3)
        var good = true
        val t = graph.getSpanningTree().edges
        val right: MutableList<Pair<Int, Int>> = mutableListOf(Pair(1, 2), Pair(2, 3))
        for (i in right) {
            if (Pair(i.first, i.second) in t || Pair(i.second, i.first) in t) {
                continue
            }
            good = false
        }
        assert(good)
    }

    @Test
    fun weightedDisjointedTest() {
        val graph = WGraph()
        for (i in 1..6) {
            graph.addNode(i)
        }
        graph.addEdge(1, 2, 1)
        graph.addEdge(2, 3, 2)
        graph.addEdge(3, 1, 3)
        graph.addEdge(4, 5, 1)
        graph.addEdge(5, 6, 1)
        var good = true
        val t = graph.getSpanningTree().edges
        val right: MutableList<Pair<Int, Int>> = mutableListOf(Pair(1, 2), Pair(2, 3), Pair(4, 5), Pair(5, 6))
        for (i in right) {
            if (Pair(i.first, i.second) in t || Pair(i.second, i.first) in t) {
                continue
            }
            good = false
        }
        assert(good)
    }

    @Test
    fun oneNodeTest() {
        val graph = WGraph()
        graph.addNode(1)
        var good = true
        val t = graph.getSpanningTree()
        if (!(1 in t.nodes && t.nodes.size == 1)) {
            good = false
        }
        assert(good)
    }

    @Test
    fun negativeEdgesTest() {
        val graph = WGraph()
        for (i in 1..4) {
            graph.addNode(i)
        }
        graph.addEdge(1, 2, -10)
        graph.addEdge(2, 4, 2)
        graph.addEdge(1, 4, 5)
        graph.addEdge(1, 3, 4)
        graph.addEdge(3, 4, 3)
        var good = true
        val t = graph.getSpanningTree().edges
        val right: MutableList<Pair<Int, Int>> = mutableListOf(Pair(1, 2), Pair(2, 4), Pair(4, 3))
        for (i in right) {
            if (Pair(i.first, i.second) in t || Pair(i.second, i.first) in t) {
                continue
            }
            good = false
        }
        assert(good)
    }
}