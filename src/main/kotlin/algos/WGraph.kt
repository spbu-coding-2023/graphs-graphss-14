package algos

import java.util.*

open class WGraph : Graph() {
    val weights = mutableMapOf<Pair<Int, Int>, Int>()

    open fun addEdge(n: Int, m: Int, weight: Int) {
        super.addEdge(n, m)
        weights[Pair(n, m)] = weight
        weights[Pair(m, n)] = weight // Если граф ненаправленный
    }

    fun shortestPathBF(source: Int, target: Int): List<Int>? {
        val distance = mutableMapOf<Int, Int>()
        val predecessor = mutableMapOf<Int, Int?>()

        for (node in adjacencyList.keys) {
            distance[node] = Int.MAX_VALUE
            predecessor[node] = null
        }
        distance[source] = 0

        for (i in 1 until adjacencyList.size) {
            for ((u, neighbors) in adjacencyList) {
                for (v in neighbors) {
                    val edgeWeight = weights[Pair(u, v)] ?: Int.MAX_VALUE
                    if (distance[u] != Int.MAX_VALUE && distance[u]!! + edgeWeight < distance[v]!!) {
                        distance[v] = distance[u]!! + edgeWeight
                        predecessor[v] = u
                    }
                }
            }
        }

        // Проверка на наличие отрицательных циклов
        for ((u, neighbors) in adjacencyList) {
            for (v in neighbors) {
                val edgeWeight = weights[Pair(u, v)] ?: Int.MAX_VALUE
                if (distance[u] != Int.MAX_VALUE && distance[u]!! + edgeWeight < distance[v]!!) {
                    throw IllegalArgumentException("Граф содержит отрицательный цикл")
                }
            }
        }

        // Восстановление пути
        val path = mutableListOf<Int>()
        var currentNode: Int? = target
        while (currentNode != null) {
            path.add(currentNode)
            currentNode = predecessor[currentNode]
        }
        return if (path.last() == source) path.asReversed() else null
    }
    fun shortestPathD(source: Int, target: Int): List<Int>? {
        val distance = mutableMapOf<Int, Int>()
        val predecessor = mutableMapOf<Int, Int?>()
        val queue = PriorityQueue<Int>(compareBy { distance[it] })

        for (node in adjacencyList.keys) {
            distance[node] = Int.MAX_VALUE
            predecessor[node] = null
            queue.add(node)
        }
        distance[source] = 0

        while (queue.isNotEmpty()) {
            val u = queue.poll()
            for (v in adjacencyList[u] ?: emptyList()) {
                val edgeWeight = weights[Pair(u, v)] ?: Int.MAX_VALUE
                if (distance[u] != Int.MAX_VALUE && distance[u]!! + edgeWeight < distance[v]!!) {
                    distance[v] = distance[u]!! + edgeWeight
                    predecessor[v] = u
                    queue.remove(v) // Delete and restore back to update the order in the queue
                    queue.add(v)
                }
            }
        }

        // Restore path
        val path = mutableListOf<Int>()
        var currentNode: Int? = target
        while (currentNode != null) {
            path.add(currentNode)
            currentNode = predecessor[currentNode]
        }
        return if (path.last() == source) path.asReversed() else null
    }

    fun getSpanningTree(): WGraph {
        val g = this
        val edges: MutableList<Pair<Int, Pair<Int, Int>>> = mutableListOf()
        val res: MutableList<Pair<Int, Pair<Int, Int>>> = mutableListOf()
        for (i in g.weights) {
            edges.add(Pair(i.value, Pair(i.key.first, i.key.second)));
        }
        edges.sortBy { it.first }
        val treeId: MutableMap<Int, Int> = mutableMapOf()
        for (i in g.nodes) {
            treeId[i] = i
        }
        for (i in edges) {
            val a = i.second.first
            val b = i.second.second
            val l = i.first
            if (treeId[a] != treeId[b]) {
                res.add(Pair(l, Pair(a, b)))
                val oldId = treeId[b]
                val newId = treeId[a]
                for (j in treeId.keys) {
                    if (treeId[j] == oldId) {
                        treeId[j] = newId!!
                    }
                }
            }
        }
        val ans: WGraph = WGraph()
        for (i in g.nodes) {
            ans.addNode(i)
        }
        for (i in res) {
            ans.addEdge(i.second.first, i.second.second, i.first)
        }
        return ans
    }

    fun LouvainAlgorithm(): MutableMap<Int, Int> {
        val communities = mutableMapOf<Int, Int>()
        fun execute(g: WGraph) {
            for (i in g.nodes) {
                communities[i] = i
            }
            fun getCommunityMembers(community: Int): MutableList<Int> {
                val members = mutableListOf<Int>()
                for (i in g.nodes) {
                    if (communities[i] == community) {
                        members.add(i)
                    }
                }
                return members
            }
            fun deltaQ(node: Int, community: Int): Double {
                var m = 0
                for (i in g.weights.values) {
                    m += i
                }
                val curId = communities[node]
                val newId = community
                val currentCommunity = getCommunityMembers(communities[node]!!)
                val newCommunity = getCommunityMembers(community)
                var sigmaIn1 = 0
                var sigmaTot1 = 0
                for (i in newCommunity) {
                    for (j in g.adjacencyList[i]!!) {
                        sigmaTot1 += g.weights[Pair(i, j)]!!
                        if (communities[j] == newId) {
                            sigmaIn1 += g.weights[Pair(i, j)]!!
                        }
                    }
                }
                var kiin1 = 0
                var ki1 = 0
                for (i in g.adjacencyList[node]!!) {
                    ki1 += g.weights[Pair(node, i)]!!
                    if (communities[i] == newId) {
                        kiin1 += g.weights[Pair(node, i)]!!
                    }
                }
                kiin1 *= 2
                val qBef1: Double = (sigmaIn1.toDouble() / (2 * m).toDouble()) - Math.pow(sigmaTot1.toDouble() / (2 * m).toDouble(), 2.0) - Math.pow(ki1.toDouble() / (2 * m).toDouble(), 2.0)
                val qAft1: Double = ((sigmaIn1 + kiin1).toDouble() / (2 * m).toDouble()) - Math.pow(((sigmaTot1 + ki1).toDouble() / (2 * m).toDouble()).toDouble(), 2.0)
                val dQNew = qAft1 - qBef1
                var sigmaIn2 = 0
                var sigmaTot2 = 0
                for (i in currentCommunity) {
                    if (i == node) {
                        continue
                    }
                    for (j in g.adjacencyList[i]!!) {
                        sigmaTot2 += g.weights[Pair(i, j)]!!
                        if (communities[j] == curId && j != node) {
                            sigmaIn2 += g.weights[Pair(i, j)]!!
                        }
                    }

                }
                var kiin2 = 0
                var ki2 = 0
                for (i in g.adjacencyList[node]!!) {
                    ki2 += g.weights[Pair(node, i)]!!
                    if (i == node) {
                        continue
                    }
                    if (communities[i] == curId) {
                        kiin2 += g.weights[Pair(node, i)]!!
                    }
                }
                kiin2 *= 2
                val qAft2: Double = (sigmaIn2.toDouble() / (2 * m).toDouble()) - Math.pow(sigmaTot2.toDouble() / (2 * m).toDouble(), 2.0) - Math.pow(ki2.toDouble() / (2 * m).toDouble(), 2.0)
                val qBef2: Double = ((sigmaIn2 + kiin2).toDouble() / (2 * m).toDouble()) - Math.pow(((sigmaTot2 + ki2).toDouble() / (2 * m).toDouble()).toDouble(), 2.0)
                val dQCur = qAft2 - qBef2
                return dQCur + dQNew
            }
            fun findBest(node: Int): Int {
                var bestCommunity = communities[node]
                var bestDQ: Double = 0.0
                for (adjacentNode in g.adjacencyList[node]!!) {
                    if (communities[adjacentNode] != communities[node]) {
                        var dQ = deltaQ(node, communities[adjacentNode]!!)
                        if (dQ > bestDQ) {
                            bestCommunity = communities[adjacentNode]
                            bestDQ =dQ
                        }
                    }
                }
                return bestCommunity!!
            }
            fun moveToCommunity(node: Int, community: Int) {
                for (i in communities.keys) {
                    if (communities[i] == node) {
                        communities[i] = community
                    }
                }
            }
            var good = true
            var totalGood = false
            while (good) {
                good = false
                for (node in g.nodes) {
                    val bestCommunity = findBest(node);
                    if (bestCommunity != communities[node]) {
                        good = true
                        totalGood = true
                        moveToCommunity(node, bestCommunity)
                    }
                }
            }
            fun reduceGraph(graph: WGraph): WGraph {
                val newG = WGraph()
                val mp = mutableMapOf<Int, MutableSet<Int>>()
                for (i in communities.keys) {
                    if (mp.containsKey(communities[i])) {
                        mp[communities[i]!!]?.add(i)
                    } else {
                        mp[communities[i]!!] = mutableSetOf();
                        mp[communities[i]!!]?.add(i)
                    }
                }
                for (i in mp.keys) {
                    newG.addNode(i)
                }
                for (community in mp.keys) {
                    var s = 0
                    for (i in mp[community]!!) {
                        if (!graph.nodes.contains(i)) {
                            continue
                        }
                        for (j in graph.adjacencyList[i]!!) {
                            if (communities[j] == community) {
                                s += graph.weights[Pair(i, j)]!!
                            }
                        }
                    }
                    newG.addEdge(community, community, s)
                }
                for (community in mp.keys) {
                    for (community1 in mp.keys) {
                        if (community != community1) {
                            var s = 0
                            var bad = true
                            for (i in mp[community]!!) {
                                if (!graph.nodes.contains(i)) {
                                    continue
                                }
                                for (j in graph.adjacencyList[i]!!) {
                                    if (communities[j] == community1) {
                                        s += graph.weights[Pair(i, j)]!!
                                        bad = false
                                    }
                                }
                            }
                            if (!bad && !newG.weights.containsKey(Pair(community, community1))) {
                                newG.addEdge(community, community1, s)
                            }
                        }
                    }
                }
                return newG
            }
            val newG = reduceGraph(g)
            if (totalGood) {
                execute(newG)
            }
        }
        execute(this)
        return communities
    }
}