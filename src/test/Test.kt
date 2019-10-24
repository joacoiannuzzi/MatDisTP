import main.FlowNetwork
import org.junit.Before
import org.junit.Test
import kotlin.test.*

class Test {


    private lateinit var t: FlowNetwork<String>

    @Before
    fun createFlowNetwork() {
        t = FlowNetwork("a", "b", "d", "g", "h", "z")

        t["a", "b"] = 5; t["a", "g"] = 7
        t["b", "d"] = 4; t["b", "h"] = 6
        t["d", "z"] = 5
        t["g", "h"] = 5; t["g", "b"] = 5
        t["h", "d"] = 2; t["h", "z"] = 6
    }

    @Test
    fun `The order should be 6`() {
        assertEquals(6, t.order)
    }

    @Test
    fun `The alpha should be 9`() {
        assertEquals(9, t.alpha)
    }

    @Test
    fun `The max flow should be 11`() {
        val flow = t.calculateMaxFlow("a", "z")
        assertEquals(11, flow)
    }
}