import java.awt.*
import java.awt.Color.*
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.geom.AffineTransform
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.util.*
import javax.swing.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class GraphPanel : JComponent() {

    private val control = ControlPanel()
    private val vertexes = ArrayList<Vertex>()
    private val selected = ArrayList<Vertex>()
    private val edges = ArrayList<Edge>()
    private var mousePt = Point(WIDE / 2, HIGH / 2)
    private val mouseRect = Rectangle()
    private var selecting = false
    private var selectedForConnect: Vertex? = null
    private var source = -1
    private var sink = -1
    private var currentState = State.None

    private val flowNetwork = FlowNetwork<String>()

    enum class State {
        ChoosingSource,
        ChoosingSink,
        Connecting,
        None
    }

    init {
        this.isOpaque = true
        this.addMouseListener(MouseHandler())
        this.addMouseMotionListener(MouseMotionHandler())
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(WIDE, HIGH)
    }

    override fun paintComponent(g: Graphics) {
        g.color = Color(0x00f0f0f0)
        g.fillRect(0, 0, width, height)
        for (e in edges) {
            e.draw(g)
        }
        for (n in vertexes) {
            n.draw(g)
        }
        if (selecting) {
            g.color = darkGray
            g.drawRect(
                mouseRect.x, mouseRect.y,
                mouseRect.width, mouseRect.height
            )
        }
    }

    private fun connect() {
        getSelected()
        val node1 = selectedForConnect!!
        val node2 = selected[0]
        if (node1 != node2) {
            val capacity = JOptionPane.showInputDialog("Enter capacity:").toInt()
            addEdge(node1, node2, capacity)
        }
        currentState = State.None
        selectedForConnect = null
        repaint()
    }

    private fun chooseSource() {
        getSelected()
        val vertex = selected[0]
        vertex.isSource = true
        source = vertexes.indexOf(vertex)
        currentState = State.ChoosingSink
        selectNone()
        repaint()
    }

    private fun chooseSink() {
        getSelected()
        val vertex = selected[0]
        sink = vertexes.indexOf(vertex)
        if (source == sink) return
        vertex.isSink = true
        currentState = State.None
        calculateMaxFlow()

    }

    private fun calculateMaxFlow() {
        val maxFlow = flowNetwork.calculateMaxFlow(source, sink)
        selectNone()
        repaint()
        JOptionPane.showMessageDialog(null, "Max flow: $maxFlow")
    }

    private fun addEdge(node1: Vertex, node2: Vertex, capacity: Int) {
        edges.add(Edge(node1, node2))
        flowNetwork.addEdge(vertexes.indexOf(node1), vertexes.indexOf(node2), capacity)
    }

    private fun addVertex(p: Point, text: String): Vertex {
        val n = Vertex(p, text) // create node
        flowNetwork.addVertex(text)
        vertexes.add(n)
        return n
    }

    /**
     * Collected all the selected nodes in vertexes.
     */
    fun getSelected() {
        selected.clear()
        for (n in vertexes) {
            if (n.isSelected) {
                selected.add(n)
            }
        }
    }

    /**
     * Select no nodes.
     */
    fun selectNone() {
        for (n in vertexes) {
            n.isSelected = false
        }
    }

    /**
     * Select a single node; return true if not already selected.
     */
    fun selectOne(point: Point): Boolean {
        for (node in vertexes) {
            if (point in node) {
                if (!node.isSelected) {
                    selectNone()
                    node.isSelected = true
                }
                return true
            }
        }
        return false
    }

    /**
     * Select each node in mouseRect.
     */
    fun selectRect() {
        for (n in vertexes) {
            n.isSelected = mouseRect.contains(n.location)
        }
    }

    /**
     * Toggle selected state of each node containing p.
     */
    fun selectToggle(p: Point) {
        for (n in vertexes) {
            if (p in n) {
                n.isSelected = !n.isSelected
            }
        }
    }

    /**
     * Update each node's position by d (delta).
     */
    fun updatePosition(d: Point) {
        for (n in vertexes) {
            if (n.isSelected) {
                n.location.x += d.x
                n.location.y += d.y
                n.setBoundary(n.b)
            }
        }
    }

    private inner class MouseHandler : MouseAdapter() {

        override fun mouseReleased(e: MouseEvent) {
            selecting = false
            mouseRect.setBounds(0, 0, 0, 0)
            e.component.repaint()
            e.component.repaint()
        }

        override fun mousePressed(e: MouseEvent) {
            mousePt = e.point
            when {
                currentState == State.Connecting -> if (selectOne(mousePt)) connect()
                currentState == State.ChoosingSource -> if (selectOne(mousePt)) chooseSource()
                currentState == State.ChoosingSink -> if (selectOne(mousePt)) chooseSink()
                e.isShiftDown -> selectToggle(mousePt)
                selectOne(mousePt) -> selecting = false
                else -> {
                    selectNone()
                    selecting = true
                }
            }
            e.component.repaint()
        }
    }

    private inner class MouseMotionHandler : MouseMotionAdapter() {

        internal var delta = Point()

        override fun mouseDragged(e: MouseEvent) {
            if (selecting) {
                mouseRect.run {
                    setBounds(
                        mousePt.x.coerceAtMost(e.x),
                        mousePt.y.coerceAtMost(e.y),
                        abs(mousePt.x - e.x),
                        abs(mousePt.y - e.y)
                    )
                }
                selectRect()
            } else {
                delta.setLocation(
                    e.x - mousePt.x,
                    e.y - mousePt.y
                )
                updatePosition(delta)
                mousePt = e.point
            }
            e.component.repaint()
        }
    }

    private inner class ControlPanel internal constructor() : JToolBar() {

        private val newNode = NewVertexAction("Add Vertex")
        private val connectEdge = ConnectEdgeAction("Add Edge")
        private val clearAll = ClearAction("Clear")
        private val calculateFlow = CalculateFlowAction("Calculate Flow")
        val defaultButton = JButton(newNode)

        init {
            this.layout = FlowLayout(FlowLayout.LEFT)
            this.background = lightGray

            this.add(defaultButton)
            this.add(JButton(connectEdge))
            this.add(JButton(calculateFlow))
            this.add(JButton(clearAll))

        }
    }

    private inner class CalculateFlowAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            if (vertexes.size < 2) {
                JOptionPane.showMessageDialog(
                    null,
                    "No enough vertexes",
                    "WARNING_MESSAGE",
                    JOptionPane.WARNING_MESSAGE
                )
            } else {
                source = -1
                sink = -1
                vertexes.forEach {
                    it.isSource = false
                    it.isSink = false
                }
                flowNetwork.resetFlow()
                currentState = State.ChoosingSource
            }
        }
    }

    private inner class NewVertexAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            selectNone()
            val p = mousePt.location
            val text = JOptionPane.showInputDialog("Enter text:")
            addVertex(p, text)
            repaint()
        }
    }

    private inner class ClearAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            vertexes.clear()
            edges.clear()
            flowNetwork.clear()
            repaint()
        }
    }

    private inner class ConnectEdgeAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            getSelected()

            if (selected.size == 1) {
                selectedForConnect = selected[0]
                currentState = State.Connecting
            }

        }
    }

    /**
     * An Edge is a pair of Nodes.
     */
    private inner class Edge(private val n1: Vertex, private val n2: Vertex) {

        var edge = flowNetwork.getEdge(vertexes.indexOf(n1), vertexes.indexOf(n2))

        fun draw(g: Graphics) {
            val p1 = n1.location
            val p2 = n2.location
            g.color = darkGray

            val g2d = g as Graphics2D
            val from = angleBetween(p1, p2)
            val to = angleBetween(p2, p1)

            val pointFrom = getPointOnCircle(p1, from)
            val pointTo = getPointOnCircle(p2, to)

            val xValue = ((pointFrom.x + pointTo.x) / 2).toFloat()
            val yValue = ((pointFrom.y + pointTo.y) / 2).toFloat()

            g.font = Font("Arial", Font.PLAIN, 17)
            g.drawString("" + edge.capacity + ", " + edge.flow, xValue, yValue) //capacity + flow

            val line = Line2D.Double(pointFrom, pointTo)
            g2d.draw(line)

            g2d.color = MAGENTA
            val arrowHead = ArrowHead()
            val at = AffineTransform.getTranslateInstance(
                pointTo.x - arrowHead.bounds2D.width / 2.0,
                pointTo.y
            )
            at.rotate(from, arrowHead.bounds2D.centerX, 0.0)
            arrowHead.transform(at)
            g2d.draw(arrowHead)
        }

        /**
         * Draw an arrow line between two points.
         * @param g the graphics component.
         * @param x1 x-position of first point.
         * @param y1 y-position of first point.
         * @param x2 x-position of second point.
         * @param y2 y-position of second point.
         * @param d  the width of the arrow.
         * @param h  the height of the arrow.
         */

        private fun angleBetween(from: Point2D, to: Point2D): Double {
            val x = from.x
            val y = from.y

            // This is the difference between the anchor point
            // and the mouse.  Its important that this is done
            // within the local coordinate space of the component,
            // this means either the MouseMotionListener needs to
            // be registered to the component itself (preferably)
            // or the mouse coordinates need to be converted into
            // local coordinate space
            val deltaX = to.x - x
            val deltaY = to.y - y

            // Calculate the angle...
            // This is our "0" or start angle..
            var rotation = -atan2(deltaX, deltaY)
            rotation = Math.toRadians(Math.toDegrees(rotation) + 180)

            return rotation
        }

        private fun getPointOnCircle(center: Point2D, angle: Double): Point2D {
            var radians = angle

            val x = center.x
            val y = center.y
            radians -= Math.toRadians(90.0) // 0 becomes th?e top
            // Calculate the outter point of the line
            val xPosy = (x + cos(radians) * RADIUS).toFloat().toDouble()
            val yPosy = (y + sin(radians) * RADIUS).toFloat().toDouble()

            return Point2D.Double(xPosy, yPosy)

        }

        private inner class ArrowHead : Path2D.Double() {
            init {
                val size = 10
                moveTo(0.0, size.toDouble())
                lineTo((size / 2).toDouble(), 0.0)
                lineTo(size.toDouble(), size.toDouble())
            }

        }
    }

    /**
     * A Node represents a node in a graph.
     */
    private inner class Vertex(val location: Point, private var text: String) {

        var isSelected = false
        val b = Rectangle()
        var isSource = false
        var isSink = false
        var font = Font("Arial", Font.PLAIN, 25)

        init {
            setBoundary(b)
        }

        /**
         * Calculate this node's rectangular boundary.
         */
        fun setBoundary(b: Rectangle) {
            b.setBounds(location.x - RADIUS, location.y - RADIUS, 2 * RADIUS, 2 * RADIUS)
        }

        /**
         * Draw this node.
         */
        fun draw(g: Graphics) {
            when {
                isSelected -> g.color = black
                isSource -> g.color = blue
                isSink -> g.color = lightGray
                else -> g.color = red
            }
            g.drawOval(b.x, b.y, b.width, b.height)
            g.font = font
            var fm = g.fontMetrics
            var textWidth = fm.getStringBounds(text, g).width
            while (textWidth > RADIUS * 2 - 1) {
                font = Font(font.fontName, font.style, font.size - 1)
                g.font = font
                fm = g.fontMetrics
                textWidth = fm.getStringBounds(text, g).width
            }
            val xText = b.x + b.width / 2.0 - textWidth / 2.0
            val yText = b.y + b.height / 2.0 + fm.maxAscent / 2.0
            g.drawString(text, xText.toInt(), yText.toInt())
        }

        /**
         * Return true if this node contains p.
         */
        operator fun contains(p: Point): Boolean {
            return p in b
        }

    }

    private fun testWithGraph() {
        val a = addVertex(Point(50, 250), "A")
        val b = addVertex(Point(200, 125), "B")
        val d = addVertex(Point(400, 125), "D")
        val g = addVertex(Point(200, 425), "G")
        val h = addVertex(Point(400, 425), "H")
        val z = addVertex(Point(550, 250), "Z")
        addEdge(a, b, 5)
        addEdge(a, g, 7)
        addEdge(b, h, 6)
        addEdge(b, d, 4)
        addEdge(g, h, 5)
        addEdge(h, d, 2)
        addEdge(d, z, 5)
        addEdge(h, z, 6)
        addEdge(g, b, 5)
    }

    companion object {

        private const val WIDE = 640
        private const val HIGH = 480
        private const val RADIUS = 40

        @JvmStatic
        fun main(args: Array<String>) {
            EventQueue.invokeLater {
                val f = JFrame("GraphPanel")
                f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                val gp = GraphPanel()
                f.add(gp.control, BorderLayout.NORTH)
                f.add(JScrollPane(gp), BorderLayout.CENTER)
                f.rootPane.defaultButton = gp.control.defaultButton
                f.pack()
                f.isLocationByPlatform = true
                f.isVisible = true
                gp.testWithGraph()
            }
        }
    }
}