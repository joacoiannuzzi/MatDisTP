import java.awt.*
import java.awt.Color.*
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.geom.*
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
    private var connecting = false
    private var selectedForConnect: Vertex? = null

    private val flowNetwork = FlowNetwork<String>()

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

    fun connect() {
        getSelected()
        val node1 = selectedForConnect!!
        val node2 = selected[0]
        if (node1 != node2) {
            val capacity = JOptionPane.showInputDialog("Enter capacity:").toInt()
            edges.add(Edge(node1, node2))
            flowNetwork[vertexes.indexOf(node1), vertexes.indexOf(node2)] = capacity
        }
        connecting = false
        selectedForConnect = null
        repaint()
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
                connecting -> {
                    if (selectOne(mousePt)) {
                        connect()
                    }
                }
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
            this.add(JButton(clearAll))
            this.add(JButton(calculateFlow))
            this.add(JButton(connectEdge))

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
                val maxFlow = flowNetwork.calculateMaxFlow(0, flowNetwork.order() - 1)
                JOptionPane.showMessageDialog(null, "Max flow: $maxFlow")
                repaint()
            }
        }
    }

    private inner class NewVertexAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            selectNone()
            val p = mousePt.location
            val text = JOptionPane.showInputDialog("Enter text:")
            val n = Vertex(p, text) // create node
            flowNetwork.addVertex(text)
            n.isSelected = true
            vertexes.add(n)
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
                connecting = true
            }

        }
    }

    /**
     * An Edge is a pair of Nodes.
     */
    private inner class Edge(private val n1: Vertex, private val n2: Vertex) {

        fun draw(g: Graphics) {
            val p1 = n1.location
            val p2 = n2.location
            g.color = darkGray
            val edge = flowNetwork[vertexes.indexOf(n1), vertexes.indexOf(n2)]
            g.drawString("" + edge.capacity + ", " + edge.flow, p1.x / 2, p1.x * 2)
            drawArrow(g, p1, p2)


        }

        private fun drawArrow(g: Graphics, circle1: Point2D, circle2: Point2D) {

            val g2d = g as Graphics2D
            val from = angleBetween(circle1, circle2)
            val to = angleBetween(circle2, circle1)

            val pointFrom = getPointOnCircle(circle1, from)
            val pointTo = getPointOnCircle(circle2, to)

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

        inner class ArrowHead : Path2D.Double() {
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
        /**
         * Return true if this node is selected.
         */
        /**
         * Mark this node as selected.
         */
        var isSelected = false
        val b = Rectangle()

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
            g.color = red
            g.drawOval(b.x, b.y, b.width, b.height)

            g.drawString(text, b.x + b.width / 2 - text.length / 2, b.y + b.height / 2)

            if (isSelected) {
                g.color = black
                g.drawOval(b.x, b.y, b.width, b.height)
            }
        }

        /**
         * Return true if this node contains p.
         */
        operator fun contains(p: Point): Boolean {
            return p in b
        }

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
            }
        }
    }
}