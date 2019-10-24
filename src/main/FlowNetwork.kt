package main

import java.util.*
import kotlin.math.min

class FlowNetwork<T>(capacity: Int = 10) {

    data class Edge(var capacity: Int, var flow: Int = 0)

    var order = 0
        private set
    var alpha = 0
        private set
    private var vertexes: Array<T?> = arrayOfNulls<Any>(capacity) as Array<T?>
    private var matrix = Array(capacity) { Array(capacity) { Edge(Int.MAX_VALUE) } }

    constructor(vararg vertexes: T) : this(vertexes.size) { // 2º constructor
        this.vertexes = vertexes as Array<T?>
        this.order = vertexes.size
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

    private fun existsEdge(v: Int, w: Int) = matrix[v][w].capacity != Int.MAX_VALUE

    fun notSaturated(v: Int, w: Int) = remainingFlow(v, w) > 0

    fun remainingFlow(v: Int, w: Int) = matrix[v][w].capacity - matrix[v][w].flow

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

    private fun bfs(source: Int, end: Int, tags: Array<Tag>): Boolean {
        val visited = BooleanArray(order)
        val queue = LinkedList<Int>()
        queue.push(source)
        visited[source] = true

        while (!queue.isEmpty()) {
            val u = queue.pop()

            for (vertex in 0 until order) {
                if (!visited[vertex] && existsEdge(u, vertex) && notSaturated(u, vertex)) {
                    queue.push(vertex)
                    tags[vertex] = Tag(u, min(remainingFlow(u, vertex), tags[u].flow))
                    visited[vertex] = true
                }
            }
        }
        return visited[end]
    }

    private data class Tag(val parent: Int, val flow: Int)

    fun calculateMaxFlow(source: T, sink: T): Int {
        val sourceIndex = vertexes.indexOf(source)
        val sinkIndex = vertexes.indexOf(sink)

        var s: Int
        var u: Int
        val tags = Array(order) { Tag(-1, Int.MAX_VALUE) } // create array of length order with all values nil tags
        var maxFlow = 0

        while (bfs(sourceIndex, sinkIndex, tags)) {

            val pathFlow = tags[sinkIndex].flow

            s = sinkIndex
            while (s != sourceIndex) {
                u = tags[s].parent
                matrix[u][s].flow += pathFlow
                s = tags[s].parent
            }
            maxFlow += pathFlow
        }
        return maxFlow
    }


}

