/**
 * @Authors: Adaro Maximilano
 *           Biale Brian
 *           Iannuzzi Joaquin
 */

import java.util.*
import kotlin.math.min

/**
 *    a.	Especificar una red de transporte ++++++++
 *
 *    b.	Realizar y probar la implementación de una red de transporte +++++++++++
 *
 *    c.	Para probar el buen funcionamiento de la clase anterior: +++++++++++
 *
 *        i.	Hacer un método que permita cargar la red (este método debe permita cargar los valores
 *        de los nodos y las aristas). ++++++++++
 *
 *        ii.	Hacer un método que muestre por pantalla la red (puede ser un listado con los valores
 *        de los nodos y otro con las aristas o bien el dibujo del grafo). ++++++++++++
 *
 *    d.	Implementar el algoritmo de Ford Fulkerson. +++++++++++
 *
 *    La entrega es el 12 de noviembre. Ese día se efectuará la defensa del trabajo.
 *    Como este trabajo equivale al segundo parcial debe quedar la evidencia del mismo en la
 *    facultad por lo tanto deben entregar impreso:
 *
 *        • caratula que incluya el nombre de la materia, cuatrimestre y año y los integrantes del grupo
 *        • el enunciado
 *        • la especificación del grafo
 *        • análisis de la complejidad de cada método.
 *        • las especificaciones de los algoritmos pedidos
 *        • NO entregar los códigos
 *
 */

class FlowNetwork<T>(capacity: Int = 10) {

    data class Edge(var capacity: Int, var flow: Int = 0)

    private val inf = Int.MAX_VALUE

    private var order = 0
    private var alpha = 0
    private var vertexes: Array<T?> = arrayOfNulls<Any>(capacity) as Array<T?>
    private var matrix = Array(capacity) { Array(capacity) { Edge(inf) } }


    /**
     * Returns the order of the FlowNetwork
     *
     *  O(1)
     */
    fun order() = order

    /**
     * Returns the quantity of edges (alpha) of the FlowNetwork
     *
     *  O(1)
     */
    fun alpha() = alpha

    /**
     * Add the vertex 'v' to FlowNetwork
     *
     *  O(1)
     */
    fun addVertex(v: T) {
        vertexes[order++] = v
    }

    /**
     * Adds an edge between 'v' and 'w' with a given capacity
     *
     *  O(1)
     */
    fun addEdge(v: Int, w: Int, capacity: Int) {
        if (!existsEdge(v, w)) {
            matrix[v][w].capacity = capacity
            alpha++
        }
    }

    /**
     * Returns true if there is an edge between 'v' and 'w'
     * or false otherwise
     *
     *  O(1)
     */
    fun existsEdge(v: Int, w: Int) = matrix[v][w].capacity != inf

    /**
     * Returns the vertex in the position 'i' of the array
     *
     *  O(1)
     */
    fun getVertex(i: Int) = vertexes[i]

    /**
     * Returns the edge between 'v' and 'w'
     *
     *  O(1)
     */
    fun getEdge(v: Int, w: Int) = matrix[v][w]

    /**
     * Resets the flow of each edge to 0
     *
     *  O(n2)
     */
    fun resetFlow() {
        matrix.forEach { array ->
            array.forEach { edge ->
                edge.flow = 0
            }
        }
    }

    /**
     * Restores the FlowNetwork to default values,
     * removes every vertex and edge
     *
     *  O(1)
     */
    fun clear() {
        val capacity = vertexes.size
        order = 0
        alpha = 0
        vertexes = arrayOfNulls<Any>(capacity) as Array<T?>
        matrix = Array(capacity) { Array(capacity) { Edge(inf) } }
    }

    /**
     * Returns the remaining flow of the edge between 'v' and 'w'
     *
     *  O(1)
     */
    fun remainingFlow(v: Int, w: Int) = matrix[v][w].capacity - matrix[v][w].flow


    /**
     * Returns true if the edge is not saturated
     *
     *  O(1)
     */
    fun notSaturated(v: Int, w: Int) = remainingFlow(v, w) > 0


    /**
     * Returns true if there is a path with not saturated edges
     * between the source and the sink
     * and fills the 'tags' array with the parent of each vertex
     * and the remaining flow
     *
     *  O(order + alpha)
     */
    private fun lookForPath(source: Int, end: Int, tags: Array<Tag>): Boolean {
        // creates an array with all values false,
        // if the vertex is visited the value will change to true
        val visited = BooleanArray(order)
        val queue = LinkedList<Int>()
        queue.push(source)
        visited[source] = true

        while (!queue.isEmpty()) {
            val u = queue.pop()

            for (vertex in 0 until order) {

                // checks that the vertex was not visited,
                // there is an edge between u and vertex
                // and the edge is not saturated
                if (!visited[vertex] && existsEdge(u, vertex) && notSaturated(u, vertex)) {
                    queue.push(vertex)

                    // the max flow that current edge can transport
                    val flow = min(remainingFlow(u, vertex), tags[u].flow)
                    tags[vertex] = Tag(u, flow)
                    visited[vertex] = true
                }
            }
        }
        return visited[end]
    }

    private data class Tag(val parent: Int, val flow: Int)


    /**
     * @param source index of vertex from where to start the flow
     * @param sink index of vertex from where to finish the flow
     *
     * Calculates the flow in each edge
     * and returns the maximum total flow
     * of the FlowNetwork
     *
     *  O(alpha * maxFlow)
     */
    fun calculateMaxFlow(source: Int, sink: Int): Int {

        // create array of length 'order' with all values nil tags
        val tags = Array(order) { Tag(-1, inf) }

        // set initial max flow to 0
        var maxFlow = 0

        // checks if there is a path
        while (lookForPath(source, sink, tags)) {

            // the flow of the current path
            val pathFlow = tags[sink].flow

            var s = sink

            //increases the flow in each edge of the path
            while (s != source) {
                val u = tags[s].parent
                matrix[u][s].flow += pathFlow
                s = tags[s].parent
            }
            // increases total max flow
            maxFlow += pathFlow
        }
        return maxFlow
    }

}
