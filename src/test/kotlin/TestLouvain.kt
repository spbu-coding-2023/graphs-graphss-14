import algos.WGraph
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestLouvain {

    private fun getSetOfCommunities(mp: MutableMap<Int, Int>): MutableSet<MutableSet<Int>> {
        val t: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
        for (i in mp.keys) {
            if (!t.containsKey(mp[i])) {
                t[mp[i]!!] = mutableSetOf()
            }
            t[mp[i]!!]?.add(i)
        }
        val tt: MutableSet<MutableSet<Int>> = mutableSetOf()
        for (i in t.keys) {
            val s: MutableSet<Int> = mutableSetOf()
            for (j in t[i]!!) {
                s.add(j)
            }
            tt.add(s)
        }
        return tt
    }

    @Test
    fun testWithDisjointCommunities() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)
        graph.addNode(6)
        graph.addNode(7)
        graph.addNode(8)
        graph.addEdge(1, 2, 1)
        graph.addEdge(2, 3, 1)
        graph.addEdge(3, 4, 1)
        graph.addEdge(4, 5, 1)
        graph.addEdge(5, 1, 1)
        graph.addEdge(8, 7, 1)
        graph.addEdge(7, 6, 1)
        graph.addEdge(6, 8, 1)

        val communities = graph.LouvainAlgorithm()
        val t = getSetOfCommunities(communities)
        val right: MutableSet<MutableSet<Int>> = mutableSetOf(mutableSetOf(1, 2, 3, 4, 5), mutableSetOf(6, 7, 8))
        assertEquals(t, right)
    }

    @Test
    fun testWithJoinedCommunities() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)
        graph.addNode(6)
        graph.addEdge(1, 2, 1)
        graph.addEdge(1, 3, 1)
        graph.addEdge(2, 3, 1)
        graph.addEdge(4, 5, 1)
        graph.addEdge(5, 6, 1)
        graph.addEdge(4, 6, 1)
        graph.addEdge(2, 5, 1)
        val s = getSetOfCommunities(graph.LouvainAlgorithm())
        val right: MutableSet<MutableSet<Int>> = mutableSetOf(mutableSetOf(1, 2, 3), mutableSetOf(4, 5, 6))
        assertEquals(s, right)
    }

    @Test
    fun testWithOneNode() {
        val graph = WGraph()
        graph.addNode(1)
        val s = getSetOfCommunities(graph.LouvainAlgorithm())
        val right = mutableSetOf(mutableSetOf(1))
        assertEquals(s, right)
    }

    @Test
    fun testWithWeightedEdges() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addNode(3)
        graph.addNode(4)
        graph.addNode(5)
        graph.addEdge(1, 2, 6)
        graph.addEdge(1, 3, 1)
        graph.addEdge(2, 3, 1)
        graph.addEdge(3, 5, 1)
        graph.addEdge(3, 4, 1)
        graph.addEdge(5, 4, 1)
        val s = getSetOfCommunities(graph.LouvainAlgorithm())
        val right = mutableSetOf(mutableSetOf(1, 2), mutableSetOf(3, 4, 5))
        assertEquals(s, right)
    }

    @Test
    fun testWithTwoNodesWithoutEdges() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        val s = getSetOfCommunities(graph.LouvainAlgorithm())
        val right = mutableSetOf(mutableSetOf(1), mutableSetOf(2))
        assertEquals(s, right)
    }

    @Test
    fun testWithSelfLoop() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addEdge(1, 1, 1)
        val s = getSetOfCommunities(graph.LouvainAlgorithm())
        val right = mutableSetOf(mutableSetOf(1))
        assertEquals(s, right)
    }

    @Test
    fun testWithSelfLoop1() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addEdge(1, 1, 1)
        graph.addEdge(1, 2, 1)
        val s = getSetOfCommunities(graph.LouvainAlgorithm())
        val right = mutableSetOf(mutableSetOf(1, 2))
        assertEquals(s, right)
    }

    @Test
    fun testWithSelfLoop2() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addEdge(1, 1, 9)
        graph.addEdge(1, 2, 1)
        val s = getSetOfCommunities(graph.LouvainAlgorithm())
        val right = mutableSetOf(mutableSetOf(1, 2))
        assertEquals(s, right)
    }

    @Test
    fun testWithZeroEdge() {
        val graph = WGraph()
        graph.addNode(1)
        graph.addNode(2)
        graph.addEdge(1, 2, 0)
        val s = getSetOfCommunities(graph.LouvainAlgorithm())
        val right = mutableSetOf(mutableSetOf(1), mutableSetOf(2))
        assertEquals(s, right)
    }
}