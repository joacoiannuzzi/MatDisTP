import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class Test {

    private lateinit var t: FlowNetwork<Int>

    @Before // this fun is called before every @Test
    fun createFlowNetwork() {
        t = FlowNetwork(6)
        for (i in 0..5)
            t.addVertex(i)

        t.addEdge(0, 1, 5)
        t.addEdge(0, 3, 7)
        t.addEdge(1, 2, 4)
        t.addEdge(1, 4, 6)
        t.addEdge(2, 5, 5)
        t.addEdge(3, 4, 5)
        t.addEdge(3, 1, 5)
        t.addEdge(4, 2, 2)
        t.addEdge(4, 5, 6)

    }

    @Test
    fun `The order should be 6`() {
        assertEquals(6, t.order())
    }

    @Test
    fun `The alpha should be 9`() {
        assertEquals(9, t.amountOfEdges())
    }

    @Test
    fun `The max flow should be 11`() {
        val flow = t.calculateMaxFlow(0, 5)
        assertEquals(11, flow)
    }
}