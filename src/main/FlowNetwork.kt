package main

import java.util.*
import kotlin.math.min

class FlowNetwork<T>(capacity: Int = 10) {

    data class Edge(var capacity: Int, var flow: Int = 0)

    var order = 0
        private set
    var alpha = 0
        private set
    private var vertexes = arrayOfNulls<Any>(capacity) as Array<T?>
    private var matrix = Array(capacity) { Array(capacity) { Edge(-1) } }

    constructor(vararg vertexes: T) : this(vertexes.size) { // 2º constructor
        this.vertexes = vertexes as Array<T?>
        this.order = vertexes.size
    }

    fun addVertex(v: T) {
        vertexes[order++] = v
    }

    fun addEdge(v: T, w: T, capacity: Int) = addEdge(vertexes.indexOf(v), vertexes.indexOf(w), capacity)

    private fun addEdge(v: Int, w: Int, capacity: Int) {
        if (!existsEdge(v, w)) {
            matrix[v][w].capacity = capacity
            alpha++
        }
    }

    operator fun set(v: T, w: T, capacity: Int) = addEdge(v, w, capacity)

    fun existsEdge(v: T, w: T) = existsEdge(vertexes.indexOf(v), vertexes.indexOf(w))

    private fun existsEdge(v: Int, w: Int) = matrix[v][w].capacity > 0

    fun getVertex(v: Int) = vertexes[v]

    private fun getEdge(v: Int, w: Int) = matrix[v][w]

    private fun getEdge(v: T, w: T) = matrix[vertexes.indexOf(v)][vertexes.indexOf(w)]

    operator fun get(v: Int, w: Int) = getEdge(v, w)

    fun getMatrix() = matrix

    fun getAdj(v: Int): MutableList<Int> {
        if (v > order) {
            throw IndexOutOfBoundsException()
        }
        val lst = mutableListOf<Int>()
        for (w in 0 until order)
            if (existsEdge(v, w))
                lst.add(w)
        return lst
    }

    private fun bfs(source: Int, end: Int, parent: IntArray): Boolean {
        val visited = BooleanArray(order)
        val queue = LinkedList<Int>()
        queue.push(source)
        visited[source] = true

        while (!queue.isEmpty()) {
            val u = queue.pop()

            for (vertex in 0 until order) {
                if (!visited[vertex] && existsEdge(u, vertex)) {
                    queue.push(vertex)
                    parent[vertex] = u
                    visited[vertex] = true
                }
            }
        }
        return visited[end]
    }

    fun fordFulkerson(source: T, sink: T): Int {
        val sourceIndex = vertexes.indexOf(source)
        val sinkIndex = vertexes.indexOf(sink)

        var s = 0
        var u = 0
        val parent = IntArray(order) { -1 } // create array of length order with all values '-1'
        var maxFlow = 0

        while (bfs(sourceIndex, sinkIndex, parent)) {

            var pathFlow = Int.MAX_VALUE // capacity of origin
            s = sinkIndex

            while (s != sourceIndex) {
                u = parent[s]
                pathFlow = min(pathFlow, matrix[u][s].capacity)
                s = parent[s]
            }

            s = sinkIndex
            while (s != sourceIndex) {
                u = parent[s]
                matrix[u][s].capacity -= pathFlow
                matrix[s][u].capacity += pathFlow
                s = parent[s]
            }
            maxFlow += pathFlow
        }
        return maxFlow
    }


}
