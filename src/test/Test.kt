import main.FlowNetwork
import org.junit.Test
import kotlin.test.*

class Test {

    @Test
    fun `The flow of this network should be 11`() {
        val t = FlowNetwork(arrayOf("a", "b", "d", "g", "h", "z"))

        t["a", "b"] = 5; t["a", "g"] = 7
        t["b", "d"] = 4; t["b", "h"] = 6
        t["d", "z"] = 5
        t["g", "h"] = 5
        t["h", "d"] = 2; t["h", "z"] = 6

        val flow = t.fordFulkerson("a", "z")

        assertEquals(11, flow)
    }
}