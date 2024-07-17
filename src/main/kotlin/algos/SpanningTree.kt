package algos

fun getSpanningTree(g: WGraph): WGraph {
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
    for (i in res) {
        ans.addEdge(i.second.first, i.second.second, i.first)
    }
    return ans
}