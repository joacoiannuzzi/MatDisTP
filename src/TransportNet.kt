import java.util.*

class TransportNet<T>(capacity: Int = 10) {


    class Vertex<T>(val vertex: T)
    class Edge(var capacity: Int, var Flow: Int = 0) {
        fun print() {
            println("Capacity = $capacity, Flux = $Flow")
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

    fun existsEdge(v: Int, w: Int): Boolean {
        if (v > n || w > n) {
            throw IndexOutOfBoundsException()
        }
        return matrix[v][w] != nilEdge
    }

    fun order(): Int {
        return n
    }

    fun quantityOfEdges(): Int {
        return alpha
    }

    fun getVertex(v: Int): T? {
        if (v > n) {
            throw IndexOutOfBoundsException()
        }
        return vertexes[v]
    }

    fun getMatrix(): Array<Array<Edge>> {
        return matrix
    }

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

    fun <T> bfs(v: Int) {
        var fr: Int
        val visited = BooleanArray(order())
        val c = LinkedList<Int>()
        var lst: List<Int>
        c.add(v)
        visited[v] = true
        while (!c.isEmpty()) {
            fr = c.peek()
            c.remove()
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
