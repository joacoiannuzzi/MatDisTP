package main

import kotlin.math.min

class TransportNetApp {

    fun <T> fordFulkerson(graph: TransportNet<T>, s: Int, t: Int): Int {
        var u: Int
        var v: Int
        val order = graph.order()

        val rGraph = graph.getMatrix()

        // This array is filled by BFS and to store path
        val parent = IntArray(order)

        var max_flow = 0 // There is no flow initially

        // Augment the flow while tere is path from source
        // to sink
        while (bfs(rGraph, s, t, parent)) {
            // Find minimum residual capacity of the edhes
            // along the path filled by BFS. Or we can say
            // find the maximum flow through the path found.
            var path_flow = Integer.MAX_VALUE
            v = t
            while (v != s) {
                u = parent[v]
                path_flow = min(path_flow, rGraph[u][v])
                v = parent[v]
            }

            // update residual capacities of the edges and
            // reverse edges along the path
            v = t
            while (v != s) {
                u = parent[v]
                rGraph[u][v] -= path_flow
                rGraph[v][u] += path_flow
                v = parent[v]
            }

            // Add path flow to overall flow
            max_flow += path_flow
        }

        // Return the overall flow
        return max_flow
    }
}