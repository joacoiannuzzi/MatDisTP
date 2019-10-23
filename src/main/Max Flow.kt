package main

// Java program for implementation of Ford Fulkerson algorithm
import java.util.LinkedList
import kotlin.math.min

class MaxFlow {

    val V = 6

    /* Returns true if there is a path from source 's' to sink
	't' in residual graph. Also fills parent[] to store the
	path */
    fun bfs(rGraph: Array<IntArray>, s: Int, t: Int, parent: IntArray): Boolean {
        // Create a visited array and mark all vertices as not
        // visited
        val visited = BooleanArray(V)
        for (i in 0 until V)
            visited[i] = false

        // Create a queue, enqueue source vertex and mark
        // source vertex as visited
        val queue = LinkedList<Int>()
        queue.add(s)
        visited[s] = true
        parent[s] = -1

        // Standard BFS Loop
        while (queue.size != 0) {
            val u = queue.poll()

            for (v in 0 until V) {
                if (!visited[v] && rGraph[u][v] > 0) {
                    queue.add(v)
                    parent[v] = u
                    visited[v] = true
                }
            }
        }

        // If we reached sink in BFS starting from source, then
        // return true, else false
        return visited[t] == true
    }

    // Returns tne maximum flow from s to t in the given graph
    fun fordFulkerson(graph: Array<IntArray>, s: Int, t: Int): Int {
        var u: Int
        var v: Int

        // Create a residual graph and fill the residual graph
        // with given capacities in the original graph as
        // residual capacities in residual graph

        // Residual graph where rGraph[i][j] indicates
        // residual capacity of edge from i to j (if there
        // is an edge. If rGraph[i][j] is 0, then there is
        // not)
        val rGraph = graph.copyOf()

        // This array is filled by BFS and to store path
        val parent = IntArray(V)

        var max_flow = 0 // There is no flow initially

        // Augment the flow while there is path from source
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


