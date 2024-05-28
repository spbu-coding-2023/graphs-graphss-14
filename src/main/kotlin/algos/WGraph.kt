package algos

class WGraph : Graph() {
    private val weights = mutableMapOf<Pair<Int, Int>, Int>()

    fun addEdge(n: Int, m: Int, weight: Int) {
        super.addEdge(n, m)
        weights[Pair(n, m)] = weight
        weights[Pair(m, n)] = weight // Если граф ненаправленный
    }

    fun shortestPath(source: Int, target: Int): List<Int>? {
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
}
