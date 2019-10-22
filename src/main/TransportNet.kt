package main

import java.util.*

class TransportNet<T>(capacity: Int = 10) {


    class Vertex<T>(val vertex: T)
    class Edge(var capacity: Int, var flow: Int = 0) {
        fun print() {
            println("Capacity = $capacity, Flow = $flow")
        }
    }

    private val nilEdge = Edge(-1, -1)

    private var n = 0
    private var alpha = 0
    private val vertexes = arrayOfNulls<Any>(capacity) as Array<T?>
    private var matrix = Array(capacity) { Array(capacity) { nilEdge } }

    fun addVertex(v: T) {
        if (n > vertexes.size)
            throw IndexOutOfBoundsException("")
        vertexes[n++] = v
    }

    fun addEdge(v: Int, w: Int, capacity: Int) {
        if (v > n || w > n || v < 0 || w < 0) {
            throw IndexOutOfBoundsException()
        }
        if (!existsEdge(v, w)) {
            matrix[w][v] = Edge(capacity)
            alpha++
        }
    }

    operator fun set(v: Int, w: Int, capacity: Int) = addEdge(v, w, capacity)

    fun existsEdge(v: Int, w: Int): Boolean {
        if (v > n || w > n) {
            throw IndexOutOfBoundsException()
        }
        return matrix[v][w] != nilEdge
    }

    fun order() = n


    fun quantityOfEdges() = alpha


    fun getVertex(v: Int): T? {
        if (v > n) {
            throw IndexOutOfBoundsException()
        }
        return vertexes[v]
    }

    fun getEdge(v: Int, w: Int) = matrix[v][w]

    operator fun get(v: Int, w: Int) = getEdge(v, w)


    fun getMatrix() = matrix

    fun getAdj(v: Int): List<Int> {
        if (v > n) {
            throw IndexOutOfBoundsException()
        }
        val lst = mutableListOf<Int>()
        for (w in 0 until n)
            if (matrix[v][w] != nilEdge)
                lst.add(w)
        return lst
    }

    fun <T> bfs(v: Int = 0) {
        var fr: Int
        val visited = BooleanArray(order())
        val c = LinkedList<Int>()
        var lst: List<Int>
        c.add(v)
        visited[v] = true
        while (!c.isEmpty()) {
            fr = c.remove()
            process(fr)
            lst = getAdj(fr)
            for (i in lst) {
                if (!visited[i]) {
                    visited[i] = true
                    c.add(i)
                }
            }
        }
    }

    private fun process(t: Int) {
        println(t)
    }


}
