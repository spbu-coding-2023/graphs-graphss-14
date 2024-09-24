package algos

import java.util.*

open class DiGraph() : WGraph(){

    override fun addEdge(n: Int, m: Int, weight: Int) {
        if (Pair(n, m) !in edges) {
            edges.add(Pair(n, m))
            weights[Pair(n, m)] = weight
            adjacencyList.getOrPut(n) { mutableListOf() }.add(m)
        }
    }

    override fun findCyclesFromNode(node: Int): List<List<Int>> {
        val cycles = mutableListOf<List<Int>>()
        val visited = mutableSetOf<Int>()
        val path = LinkedHashSet<Int>()

        fun dfs(currentNode: Int) {
            if (path.contains(currentNode)) {
                // Cycle detected
                val cycle = path.toList().subList(path.indexOf(currentNode), path.size)
                cycles.add(cycle)
                return
            }

            if (visited.contains(currentNode)) {
                return
            }

            visited.add(currentNode)
            path.add(currentNode)

            for (neighbor in adjacencyList[currentNode] ?: emptyList()) {
                dfs(neighbor)
            }

            path.remove(currentNode)
        }

        dfs(node)
        return cycles
    }


override fun findBridges(): List<Pair<Int, Int>> {
        val bridges = mutableListOf<Pair<Int, Int>>()
        val visited = mutableSetOf<Int>()
        val discoveryTime = mutableMapOf<Int, Int>()
        val low = mutableMapOf<Int, Int>()
        val stack = Stack<Int>()
        var time = 0

        fun dfs(node: Int, parent: Int) {
            visited.add(node)
            discoveryTime[node] = time
            low[node] = time
            time++
            stack.push(node)

            for (neighbor in adjacencyList[node] ?: emptyList()) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, node)
                    low[node] = minOf(low[node]!!, low[neighbor]!!)
                } else if (stack.contains(neighbor)) {
                    low[node] = minOf(low[node]!!, discoveryTime[neighbor]!!)
                }
            }

            if (low[node] == discoveryTime[node]) {
                val scc = mutableListOf<Int>()
                while (true) {
                    val v = stack.pop()
                    scc.add(v)
                    if (v == node) break
                }
                if (scc.size == 1) {
                    bridges.add(Pair(parent, node))
                }
            }
        }

        for (node in nodes) {
            if (!visited.contains(node)) {
                dfs(node, -1)
            }
        }

        return bridges
    }

// Выделение компонент сильной связности

    fun transpose(): DiGraph {
        val transposedGraph = DiGraph()
        for (node in nodes) {
            transposedGraph.addNode(node)
        }
        for ((from, neighbors) in adjacencyList) {
            for (to in neighbors) {
                transposedGraph.addEdge(to, from, 1)
            }
        }
        return transposedGraph
    }
    fun findSCCs(): List<List<Int>> {
        val visited = mutableSetOf<Int>()
        val stack = Stack<Int>()

        // Первый проход DFS
        fun fillOrder(v: Int) {
            visited.add(v)
            for (neighbor in adjacencyList[v] ?: emptyList()) {
                if (!visited.contains(neighbor)) {
                    fillOrder(neighbor)
                }
            }
            stack.push(v)
        }

        for (node in nodes) {
            if (!visited.contains(node)) {
                fillOrder(node)
            }
        }

        // Транспонирование графа
        val transposedGraph = transpose()

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
}
