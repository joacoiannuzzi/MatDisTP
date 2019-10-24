package main

import java.util.*
import kotlin.math.min

class FlowNetwork<T>(capacity: Int = 10): FlowNetworkInterface<T> {

    data class Edge(var capacity: Int, var flow: Int = 0)

    private var order = 0
    private var alpha = 0
    private var vertexes: Array<T?> = arrayOfNulls<Any>(capacity) as Array<T?>
        private set

    private var matrix = Array(capacity) { Array(capacity) { Edge(Int.MAX_VALUE) } }
        private set

    constructor(vararg vertexes: T) : this(vertexes.size) { // 2º constructor
        this.vertexes = vertexes as Array<T?>
        this.order = vertexes.size
    }

    override fun order() = order

    override fun alpha()  = alpha

    override fun addVertex(v: T) {
        vertexes[order++] = v
    }

    override fun addEdge(v: Int, w: Int, capacity: Int) {
        if (!existsEdge(v, w)) {
            matrix[v][w].capacity = capacity
            alpha++
        }
    }

    override fun removeVertex(v: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeEdge(v: Int, w: Int) {
        matrix[v][w] = Edge(Int.MAX_VALUE)
    }


    operator fun set(v: Int, w: Int, capacity: Int) = addEdge(v, w, capacity)

    override fun existsEdge(v: Int, w: Int) = matrix[v][w].capacity != Int.MAX_VALUE

    override fun notSaturated(v: Int, w: Int) = remainingFlow(v, w) > 0

    override fun remainingFlow(v: Int, w: Int) = matrix[v][w].capacity - matrix[v][w].flow

    override fun getVertex(v: Int) = vertexes[v]

    fun getEdge(v: Int, w: Int) = matrix[v][w]

    operator fun get(v: Int, w: Int) = getEdge(v, w)

    override fun getAdj(v: Int): MutableList<Int> {
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

    override fun calculateMaxFlow(source: Int, sink: Int): Int {
        var s: Int
        var u: Int
        val tags = Array(order) { Tag(-1, Int.MAX_VALUE) } // create array of length order with all values nil tags
        var maxFlow = 0

        while (bfs(source, sink, tags)) {

            val pathFlow = tags[sink].flow

            s = sink
            while (s != source) {
                u = tags[s].parent
                matrix[u][s].flow += pathFlow
                s = tags[s].parent
            }
            maxFlow += pathFlow
        }
        return maxFlow
    }


}

