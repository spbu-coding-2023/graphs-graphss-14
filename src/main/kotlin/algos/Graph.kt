package algos
import kotlin.math.sqrt

open class Graph {
    val nodes = mutableListOf<Int>()
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

    class SpringEmbedder {
        private val smoothingFactor = 0.1
        private val iterations = 500
        private val temperature = 0.9

        fun layout(graph: Graph): Map<Int, Pair<Double, Double>> {
            val layout = mutableMapOf<Int, Pair<Double, Double>>()
            val forces = mutableMapOf<Int, Pair<Double, Double>>()
            val temperature = mutableMapOf<Int, Double>()

            // Initialize the location of vertices with random coordinates
            for (node in graph.nodes) {
                layout[node] = Pair(Math.random(), Math.random())
                forces[node] = Pair(0.0, 0.0)
                temperature[node] = 1.0
            }

            // Calculate the forces acting on the vertices
            for (i in 0 until iterations) {
                for (node in graph.nodes) {
                    forces[node] = Pair(0.0, 0.0)
                    for (neighbor in graph.adjacencyList[node] ?: emptyList()) {
                        val deltaX = layout[node]!!.first - layout[neighbor]!!.first
                        val deltaY = layout[node]!!.second - layout[neighbor]!!.second
                        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
                        val repulsion = smoothingFactor * smoothingFactor / distance // Сила отталкивания
                        forces[node] = Pair(forces[node]!!.first + repulsion * deltaX, forces[node]!!.second + repulsion * deltaY)
                    }
                }

                // Update vertex locations
                for (node in graph.nodes) {
                    val forceX = forces[node]!!.first
                    val forceY = forces[node]!!.second
                    val length = sqrt(forceX * forceX + forceY * forceY)
                    if (length > 0) {
                        val deltaX = forceX * temperature[node]!! / length
                        val deltaY = forceY * temperature[node]!! / length
                        layout[node] = Pair(layout[node]!!.first + deltaX, layout[node]!!.second + deltaY)
                    }
                    // Decrease temperature
                    temperature[node] = temperature[node]!! * this.temperature
                }
            }

            return layout
        }
    }
}

fun main() {
    val graph = Graph()
// Adding vertices and edges to the graph
graph.addNode(1)
graph.addNode(2)
graph.addNode(3)
graph.addNode(4)
graph.addNode(5)
graph.addNode(6)
graph.addNode(7)
graph.addEdge(1,1,)
graph.addEdge(2,1,)
graph.addEdge(3,1,)
graph.addEdge(4,3,)
graph.addEdge(5,2,)
graph.addEdge(1,5,)
    val layout = Graph.SpringEmbedder().layout(graph)
    for ((node, position) in layout) {
        println("Node $node is at position $position")
    }
}