import java.util.*
import java.util.ArrayList
import kotlin.collections.ArrayList

class TransportNet<T>(capacity: Int = 10) {


    class Edge(var capacity: Int, var Flow: Int = 0) {
        fun print() {
            println("Capacity = $capacity, Flux = $Flow")
        }
    }

    private var n = 0
    private var alpha = 0
    private val vertexes= arrayOfNulls<Any>(capacity) as Array<T?>
    private var matrix = Array(capacity) { arrayOfNulls<Edge>(capacity)}

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
        return matrix[v][w] != null
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

    fun getMatrix(): Array<Array<Edge?>> {
        return matrix
    }

    fun getAdj(v: Int): List<Int> {
        if (v > n) {
            throw IndexOutOfBoundsException()
        }
        val lst = arrayListOf<Int>()
        for (w in 0 until n)
            if (matrix[v][w] != null)
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
            for (integer in lst) {
                if (!visited[integer]) {
                    visited[integer] = true
                    c.add(integer)
                }
            }
        }
    }

    private fun process(t: Int) {
        println(t)
    }


}
