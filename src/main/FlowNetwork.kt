/**
 * @Authors: Adaro Maximilano
 *           Biale Brian
 *           Iannuzzi Joaquin
 */

import java.util.*
import kotlin.math.min

/************************************************************************************************
a.	Especificar una red de transporte ++++++++
b.	Realizar y probar la implementación de una red de transporte +++++++++++
c.	Para probar el buen funcionamiento de la clase anterior:
i.	Hacer un método que permita cargar la red (este método debe permita cargar los valores
de los nodos y las aristas).
ii.	Hacer un método que muestre por pantalla la red (puede ser un listado con los valores
de los nodos y otro con las aristas o bien el dibujo del grafo).
d.	Implementar el algoritmo de Ford Fulkerson. +++++++++++

La entrega es el 12 de noviembre. Ese día se efectuará la defensa del trabajo.
Como este trabajo equivale al segundo parcial debe quedar la evidencia del mismo en la
facultad por lo tanto deben entregar impreso:
•	caratula que incluya el nombre de la materia, cuatrimestre y año y los integrantes del grupo
•	el enunciado
•	la especificación del grafo
•	análisis de la complejidad de cada método.
•	las especificaciones de los algoritmos pedidos
•	NO entregar los códigos

 *************************************************************************************************/

class FlowNetwork<T>(capacity: Int = 10) : FlowNetworkInterface<T> {

    data class Edge(var capacity: Int, var flow: Int = 0)

    private val inf = Int.MAX_VALUE

    private var order = 0
    private var alpha = 0
    private var vertexes: Array<T?> = arrayOfNulls<Any>(capacity) as Array<T?>
    private var matrix = Array(capacity) { Array(capacity) { Edge(inf) } }

    constructor(vararg vertexes: T) : this(vertexes.size) { // 2º constructor, just for test
        this.vertexes = vertexes as Array<T?>
        this.order = vertexes.size
    }

    override fun order() = order

    override fun alpha() = alpha

    override fun addVertex(v: T) {
        vertexes[order++] = v
    }

    override fun addEdge(v: Int, w: Int, capacity: Int) {
        if (!existsEdge(v, w)) {
            matrix[v][w].capacity = capacity
            alpha++
        }
    }

    operator fun set(v: Int, w: Int, capacity: Int) = addEdge(v, w, capacity)

    override fun existsEdge(v: Int, w: Int) = matrix[v][w].capacity != inf

    override fun notSaturated(v: Int, w: Int) = remainingFlow(v, w) > 0

    override fun remainingFlow(v: Int, w: Int) = matrix[v][w].capacity - matrix[v][w].flow

    override fun getVertex(v: Int) = vertexes[v]

    fun getEdge(v: Int, w: Int) = matrix[v][w]

    operator fun get(v: Int, w: Int) = getEdge(v, w)

    fun clear() {
        val capacity = vertexes.size
        order = 0
        alpha = 0
        vertexes = arrayOfNulls<Any>(capacity) as Array<T?>
        matrix = Array(capacity) { Array(capacity) { Edge(inf) } }
    }

    private fun lookForPath(source: Int, end: Int, tags: Array<Tag>): Boolean {
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

    /**
     *@param source index of vertex from where to start the calculations
     */
    override fun calculateMaxFlow(source: Int, sink: Int): Int {
        // create array of length order with all values nil tags
        val tags = Array(order) { Tag(-1, inf) }
        var maxFlow = 0

        while (lookForPath(source, sink, tags)) {

            val pathFlow = tags[sink].flow

            var s = sink
            while (s != source) {
                val u = tags[s].parent
                matrix[u][s].flow += pathFlow
                s = tags[s].parent
            }
            maxFlow += pathFlow
        }
        return maxFlow
    }

}
