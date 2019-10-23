import main.FlowNetwork
import org.junit.Test

fun main() {

    val t = FlowNetwork<String>(6)
    t.addVertex("a")
    t.addVertex("b")
    t.addVertex("d")
    t.addVertex("g")
    t.addVertex("h")
    t.addVertex("z")

    t["a", "b"] = 5; t["a", "g"] = 7
    t["b", "d"] = 4; t["b", "h"] = 6
    t["d", "z"] = 5
    t["g", "h"] = 5
    t["h", "d"] = 2; t["h", "z"] = 6

    println(t.fordFulkerson("a", "z"))
}


@Test
fun test() {

}