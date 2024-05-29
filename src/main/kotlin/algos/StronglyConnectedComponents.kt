package algos

class StronglyConnectedComponents {
    fun getStronglyConnectedComponents(graph: DGraph): MutableList<MutableList<Int>> {
        val g: MutableMap<Int, MutableList<Int>> = graph.adjacencyList
        val gr: MutableMap<Int, MutableList<Int>> = mutableMapOf()
        var used: MutableMap<Int, Boolean> = mutableMapOf()
        val order: MutableList<Int> = mutableListOf()
        var component: MutableList<Int> = mutableListOf()
        val n = g.size
        val ans: MutableList<MutableList<Int>> = mutableListOf()
        for (i in g.keys) {
            gr[i] = mutableListOf()
        }
        for (i in g.keys) {
            for (j in g[i]!!) {
                gr[j]!!.add(i)
            }
        }
        fun dfs1(v: Int) {
            used[v] = true
            for (i in g[v]!!) {
                if (!used.contains(i)) {
                    dfs1(i)
                }
            }
            order.add(v)
        }
        for (v in g.keys) {
            if (!used.contains(v)) {
                dfs1(v)
            }
        }
        fun dfs2(v: Int) {
            used[v] = true
            component.add(v)
            for (i in gr[v]!!) {
                if (!used.contains(i)) {
                    dfs2(i)
                }
            }
        }
        used = mutableMapOf()
        var ind = 0
        for (i in g.keys) {
            val v = order[n - 1 - ind]
            if (!used.contains(v)) {
                dfs2(v)
                ans.add(component)
                component = mutableListOf()
            }
        }
        return ans
    }
}