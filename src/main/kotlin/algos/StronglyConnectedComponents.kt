package algos

import java.util.*

class DiGraph {
    val nodes = mutableListOf<Int>()
    val edges = mutableListOf<Pair<Int, Int>>()
    val adjacencyList = mutableMapOf<Int, MutableList<Int>>()

    fun addNode(n: Int) {
        nodes.add(n)
        adjacencyList[n] = mutableListOf()
    }

    fun addEdge(n: Int, m: Int) {
        if (Pair(n, m) !in edges) {
            edges.add(Pair(n, m))
            adjacencyList[n]?.add(m)
        }
    }

    fun transpose(): DiGraph {
        val transposedGraph = DiGraph()
        for (node in nodes) {
            transposedGraph.addNode(node)
        }
        for ((u, v) in edges) {
            transposedGraph.addEdge(v, u)
        }
        return transposedGraph
    }
}

fun findSCCs(graph: DiGraph): List<List<Int>> {
    val visited = mutableSetOf<Int>()
    val stack = Stack<Int>()

    // Первый проход DFS
    fun fillOrder(v: Int) {
        visited.add(v)
        for (neighbor in graph.adjacencyList[v] ?: emptyList()) {
            if (!visited.contains(neighbor)) {
                fillOrder(neighbor)
            }
        }
        stack.push(v)
    }

    for (node in graph.nodes) {
        if (!visited.contains(node)) {
            fillOrder(node)
        }
    }

    // Транспонирование графа
    val transposedGraph = graph.transpose()

    // Сброс посещенных вершин для второго прохода
    visited.clear()

    // Второй проход DFS
    val sccs = mutableListOf<List<Int>>()
    fun dfs(v: Int, scc: MutableList<Int>) {
        visited.add(v)
        scc.add(v)
        for (neighbor in transposedGraph.adjacencyList[v] ?: emptyList()) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, scc)
            }
        }
    }

    while (!stack.isEmpty()) {
        val v = stack.pop()
        if (!visited.contains(v)) {
            val scc = mutableListOf<Int>()
            dfs(v, scc)
            sccs.add(scc)
        }
    }

    return sccs
}