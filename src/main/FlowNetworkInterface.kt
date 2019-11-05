interface FlowNetworkInterface<T> {

    fun order(): Int

    fun alpha(): Int

    fun addVertex(v: T)

    fun addEdge(v: Int, w: Int, capacity: Int)

    fun removeVertex(v: Int)

    fun removeEdge(v: Int, w: Int)

    fun existsEdge(v: Int, w: Int): Boolean

    fun notSaturated(v: Int, w: Int): Boolean

    fun remainingFlow(v: Int, w: Int): Int

    fun calculateMaxFlow(source: Int, sink: Int): Int

    fun getVertex(v: Int): T?
}