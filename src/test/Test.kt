//import main.FlowNetwork
//import org.junit.Before
//import org.junit.Test
//import kotlin.test.assertEquals
//
//class Test {
//
//    private lateinit var t: FlowNetwork<Int>
//
//    @Before // this fun is called before every @Test
//    fun createFlowNetwork() {
//        t = FlowNetwork(0, 1, 2, 3, 4, 5)
//
//        t[0, 1] = 5; t[0, 3] = 7
//        t[1, 2] = 4; t[1, 4] = 6
//        t[2, 5] = 5
//        t[3, 4] = 5; t[3, 1] = 5
//        t[4, 2] = 2; t[4, 5] = 6
//
//    }
//
//    @Test
//    fun `The order should be 6`() {
//        assertEquals(6, t.order())
//    }
//
//    @Test
//    fun `The alpha should be 9`() {
//        assertEquals(9, t.alpha())
//    }
//
//    @Test
//    fun `The max flow should be 11`() {
//        val flow = t.calculateMaxFlow(0, 5)
//        assertEquals(11, flow)
//    }
//}