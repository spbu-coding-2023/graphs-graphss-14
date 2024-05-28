package algos

open class Graph {
    private val nodes = mutableListOf<Int>()
    val edges = mutableListOf<Pair<Int, Int>>()
    val adjacencyList = mutableMapOf<Int, MutableList<Int>>()

    fun addNode(n: Int) {
        nodes.add(n)
        adjacencyList[n] = mutableListOf()
    }

    open fun addEdge(n: Int, m: Int) {
        if (Pair(n, m) !in edges) {
            edges.add(Pair(n, m))
            adjacencyList[n]?.add(m)
            adjacencyList[m]?.add(n)
        }
    }
    fun removeNode(n: Int) {
        nodes.remove(n)
        adjacencyList.remove(n)
        edges.removeAll { it.first == n || it.second == n }
        for ((_, neighbors) in adjacencyList) {
            neighbors.remove(n)
        }
    }
    fun removeEdge(n:Int, m:Int){
        edges.remove(Pair(n,m))
        edges.remove(Pair(m,n))
        adjacencyList[n]?.remove(m)
        adjacencyList[m]?.remove(n)
    }
    fun findBridges(): List<Pair<Int, Int>> {
        val bridges = mutableListOf<Pair<Int, Int>>()
        val visited = mutableSetOf<Int>()
        val discoveryTime = mutableMapOf<Int, Int>()
        val low = mutableMapOf<Int, Int>()
        var time = 0

        fun dfs(node: Int, parent: Int) {
            visited.add(node)
            discoveryTime[node] = time
            low[node] = time
            time++

            for (neighbor in adjacencyList[node] ?: emptyList()) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, node)
                    low[node] = minOf(low[node]!!, low[neighbor]!!)
                    if (low[neighbor] == discoveryTime[neighbor]) {
                        bridges.add(Pair(node, neighbor))
                    }
                } else if (neighbor != parent) {
                    low[node] = minOf(low[node]!!, discoveryTime[neighbor]!!)
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
    fun printAll(){
        println(nodes)
        println(edges)
    }
}