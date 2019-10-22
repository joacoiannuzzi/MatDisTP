import main.MaxFlow

fun main() {
    val V = 6 //Number of vertices in graph

    // Driver program to test above functions
    // Let us create a graph shown in the above example
    val graph = arrayOf(
        intArrayOf(0, 16, 13, 0, 0, 0),
        intArrayOf(0, 0, 10, 12, 0, 0),
        intArrayOf(0, 4, 0, 0, 14, 0),
        intArrayOf(0, 0, 9, 0, 0, 20),
        intArrayOf(0, 0, 0, 7, 0, 4),
        intArrayOf(0, 0, 0, 0, 0, 0)
    )
    val m = MaxFlow()

    System.out.println(
        "The maximum possible flow is " +
                m.fordFulkerson(graph, 0, 5)
    )


}