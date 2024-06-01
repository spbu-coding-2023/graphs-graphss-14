package algos

open class DGraph() : Graph() {
    override fun addEdge(n: Int, m: Int) {
        if (Pair(n, m) !in edges) {
            edges.add(Pair(n, m))
            adjacencyList[n]?.add(m)
        }
    }
}