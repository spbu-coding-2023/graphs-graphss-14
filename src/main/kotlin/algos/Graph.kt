package algos
import androidx.compose.ui.graphics.Color
import java.util.*
import kotlin.collections.ArrayDeque
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

    open class SpringEmbedder {
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

    fun betweennessCentrality(): Map<Int, Float> {
        val betweenness = mutableMapOf<Int, Float>()
        val stack = mutableListOf<Int>()
        val predecessors = mutableMapOf<Int, MutableList<Int>>()
        val sigma = mutableMapOf<Int, Int>()
        val delta = mutableMapOf<Int, Double>()

        fun shortestPaths(source: Int) {
            val dist = mutableMapOf<Int, Int>()
            val queue = ArrayDeque<Int>()
            dist[source] = 0
            sigma[source] = 1
            queue.add(source)

            while (queue.isNotEmpty()) {
                val v = queue.removeFirst()
                stack.add(v)
                for (w in adjacencyList[v] ?: emptyList()) {
                    if (dist.getOrDefault(w, Int.MAX_VALUE) == Int.MAX_VALUE) {
                        queue.add(w)
                        dist[w] = dist.getOrDefault(v, 0) + 1
                    }
                    if (dist[w] == dist.getOrDefault(v, 0) + 1) {
                        sigma[w] = sigma.getOrDefault(w, 0) + sigma.getOrDefault(v, 0)
                        predecessors.getOrPut(w) { mutableListOf() }.add(v)
                    }
                }
            }
        }

        fun accumulateBetweenness(source: Int) {
            val dependency = mutableMapOf<Int, Float>()
            while (stack.isNotEmpty()) {
                val w = stack.removeLast()
                for (v in predecessors[w] ?: emptyList()) {
                    dependency[v] = dependency.getOrDefault(v, 0.0F) + (sigma.getOrDefault(v, 0) / sigma.getOrDefault(w, 1).toFloat()) * (1 + dependency.getOrDefault(w, 0.0F))
                }
                if (w != source) {
                    betweenness[w] = betweenness.getOrDefault(w, 0.0F) + dependency.getOrDefault(w, 0.0F)
                }
            }
        }

        for (source in nodes) {
            shortestPaths(source)
            accumulateBetweenness(source)
            sigma.clear()
            predecessors.clear()
        }

        val n = nodes.size
        for ((node, bc) in betweenness) {
            betweenness[node] = (bc / ((n - 1) * (n - 2) / 2.0)).toFloat()
        }

        return betweenness
    }
    fun findCyclesFromNode(startNode: Int): List<List<Int>> {
        val cycles = mutableListOf<List<Int>>()
        val visited = mutableSetOf<Int>()
        val path = mutableListOf<Int>()

        fun dfs(node: Int) {
            visited.add(node)
            path.add(node)

            for (neighbor in adjacencyList[node] ?: emptyList()) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor)
                } else if (path.contains(neighbor)) {
                    // Cycle detected
                    val cycle = path.slice(path.indexOf(neighbor) until path.size)
                    cycles.add(cycle)
                }
            }

            path.removeAt(path.size - 1)
        }

        dfs(startNode)

        return cycles
    }
}
fun main(){
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

    println(graph.findCyclesFromNode(1))
}