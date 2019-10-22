class TransportNet<Element>(capacity: Int = 10) {

    class Vertex<Element>(var key: Element)
    class Edge(var maxFlux: Int, var currentFlux: Int) {
        fun print() {
            println("maxFlux = $maxFlux, currentFlux = $currentFlux")
        }
    }

    private var n = 0
    private var alpha = 0
    private val vertexes = arrayOfNulls<Vertex<Element>>(capacity)
    private var matrix = Array(capacity) { arrayOfNulls<Edge>(capacity)}
}
