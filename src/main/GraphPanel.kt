package main

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
    private val nodes = ArrayList<Node>()
    private val selected = ArrayList<Node>()
    private val edges = ArrayList<Edge>()
    private var mousePt = Point(WIDE / 2, HIGH / 2)
    private val mouseRect = Rectangle()
    private var selecting = false

    val flow = FlowNetwork<String>()

    init {
        this.isOpaque = true
        this.addMouseListener(MouseHandler())
        this.addMouseMotionListener(MouseMotionHandler())
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(WIDE, HIGH)
    }

    public override fun paintComponent(g: Graphics) {
        g.color = Color(0x00f0f0f0)
        g.fillRect(0, 0, width, height)
        for (e in edges) {
            e.draw(g)
        }
        for (n in nodes) {
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

    private inner class MouseHandler : MouseAdapter() {

        override fun mouseReleased(e: MouseEvent) {
            selecting = false
            mouseRect.setBounds(0, 0, 0, 0)
            if (e.isPopupTrigger) {
                showPopup(e)
            }
            e.component.repaint()
        }

        override fun mousePressed(e: MouseEvent) {
            mousePt = e.point
            when {
                //e.isShiftDown -> Node.selectToggle(nodes, mousePt)
//                e.isPopupTrigger -> {
//                    Node.selectOne(nodes, mousePt)
//                    showPopup(e)
//                }
                Node.selectOne(nodes, mousePt) -> selecting = false
                else -> {
                    Node.selectNone(nodes)
                    selecting = true
                }
            }
            e.component.repaint()
        }

        private fun showPopup(e: MouseEvent) {
            control.popup.show(e.component, e.x, e.y)
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
                Node.selectRect(nodes, mouseRect)
            } else {
                delta.setLocation(
                    e.x - mousePt.x,
                    e.y - mousePt.y
                )
                Node.updatePosition(nodes, delta)
                mousePt = e.point
            }
            e.component.repaint()
        }
    }

    private inner class ControlPanel internal constructor() : JToolBar() {

        private val newNode = NewVertexAction("NewVertex")
        private val connect = ConnectAction("Connect")
        private val connectEdge = ConnectEdgeAction("ConnectEdge")
        private val clearAll = ClearAction("Clear")
        private val delete = DeleteAction("Delete")
        private val calculateFlow = CalculateFlowAction("CalculateFlow")
        val defaultButton = JButton(newNode)
        val popup = JPopupMenu()

        init {
            this.layout = FlowLayout(FlowLayout.LEFT)
            this.background = lightGray

            this.add(defaultButton)
            this.add(JButton(clearAll))
            this.add(JButton(calculateFlow))
            this.add(JButton(connectEdge))

            popup.add(JMenuItem(newNode))
            popup.add(JMenuItem(connect))
            popup.add(JMenuItem(delete))
        }
    }

    private inner class CalculateFlowAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            if (nodes.size < 2) {
                JOptionPane.showMessageDialog(
                    null,
                    "No enough vertexes",
                    "WARNING_MESSAGE",
                    JOptionPane.WARNING_MESSAGE
                );
            }
            val maxFlow = flow.calculateMaxFlow(0, flow.order() - 1)
            JOptionPane.showMessageDialog(null, "Max flow: " + maxFlow);
            repaint()
        }
    }


    private inner class NewVertexAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            Node.selectNone(nodes)
            val p = mousePt.location
            val text = JOptionPane.showInputDialog("Enter text:")
            val n = Node(p, text) // create node
            flow.addVertex(text)
            n.isSelected = true
            nodes.add(n)
            repaint()
        }
    }

    private inner class ClearAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            nodes.clear()
            edges.clear()
            repaint()
        }
    }

    private inner class ConnectEdgeAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            Node.getSelected(nodes, selected)
            val node1: Node
            val node2: Node
            if (selected.size == 1) {
                node1 = selected[1]
            }


        }
    }

    private inner class ConnectAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            Node.getSelected(nodes, selected)
            if (selected.size > 1) {
                for (i in 0 until selected.size - 1) {
                    val n1 = selected[i]
                    val n2 = selected[i + 1]
                    val capacity = JOptionPane.showInputDialog("Enter capacity:").toInt()
                    edges.add(Edge(n1, n2))
                    flow[i, i + 1] = capacity
                }
            }
            repaint()
        }
    }

    private inner class DeleteAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            val iter = nodes.listIterator()
            while (iter.hasNext()) {
                val n = iter.next()
                if (n.isSelected) {
                    deleteEdges(n)
                    iter.remove()
                }
            }
            repaint()
        }

        private fun deleteEdges(n: Node) {
            val iter = edges.listIterator()
            while (iter.hasNext()) {
                val e = iter.next()
                if (e.n1 === n || e.n2 === n) {
                    iter.remove()
                }
            }
        }
    }


    /**
     * An Edge is a pair of Nodes.
     */
    private open class Edge(val n1: Node, val n2: Node) {

        fun draw(g: Graphics) {
            val p1 = n1.location
            val p2 = n2.location
            g.color = darkGray
            g.drawString("", p1.x / 2, p1.x * 2)
            drawArrow(g, p1, p2)

        }

        private fun drawArrow(g: Graphics, circle1: Point2D, circle2: Point2D) {

            val g2d = g as Graphics2D
            val from = angleBetween(circle1, circle2)
            val to = angleBetween(circle2, circle1)


            val pointFrom = getPointOnCircle(circle1, from, RADIUS.toDouble())
            val pointTo = getPointOnCircle(circle2, to, RADIUS.toDouble())

            val line = Line2D.Double(pointFrom, pointTo)
            g2d.draw(line)
            g2d.color = Color.MAGENTA
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

        protected fun center(bounds: Rectangle2D): Point2D {
            return Point2D.Double(bounds.centerX, bounds.centerY)
        }

        protected fun getPointOnCircle(center: Point2D, radians: Double, radius: Double): Point2D {
            var radians = radians

            val x = center.x
            val y = center.y
            radians -= Math.toRadians(90.0) // 0 becomes th?e top
            // Calculate the outter point of the line
            val xPosy = (x + cos(radians) * radius).toFloat().toDouble()
            val yPosy = (y + sin(radians) * radius).toFloat().toDouble()

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
    private class Node
    /**
     * Construct a new node.
     */(
        /**
         * Return this node's location.
         */
        val location: Point,
        var text: String
    ) {
        /**
         * Return true if this node is selected.
         */
        /**
         * Mark this node as selected.
         */
        var isSelected = false
        private val b = Rectangle()

        init {
            setBoundary(b)
        }

        /**
         * Calculate this node's rectangular boundary.
         */
        private fun setBoundary(b: Rectangle) {
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

        companion object {

            /**
             * Collected all the selected nodes in list.
             */
            fun getSelected(list: List<Node>, selected: MutableList<Node>) {
                selected.clear()
                for (n in list) {
                    if (n.isSelected) {
                        selected.add(n)
                    }
                }
            }

            /**
             * Select no nodes.
             */
            fun selectNone(list: List<Node>) {
                for (n in list) {
                    n.isSelected = false
                }
            }

            /**
             * Select a single node; return true if not already selected.
             */
            fun selectOne(list: List<Node>, p: Point): Boolean {
                for (n in list) {
                    if (p in n) {
                        if (!n.isSelected) {
                            selectNone(list)
                            n.isSelected = true
                        }
                        return true
                    }
                }
                return false
            }

            /**
             * Select each node in r.
             */
            fun selectRect(list: List<Node>, r: Rectangle) {
                for (n in list) {
                    n.isSelected = r.contains(n.location)
                }
            }

            /**
             * Toggle selected state of each node containing p.
             */
            fun selectToggle(list: List<Node>, p: Point) {
                for (n in list) {
                    if (p in n) {
                        n.isSelected = !n.isSelected
                    }
                }
            }

            /**
             * Update each node's position by d (delta).
             */
            fun updatePosition(list: List<Node>, d: Point) {
                for (n in list) {
                    if (n.isSelected) {
                        n.location.x += d.x
                        n.location.y += d.y
                        n.setBoundary(n.b)
                    }
                }
            }
        }
    }

    companion object {

        private const val WIDE = 640
        private const val HIGH = 480
        private const val RADIUS = 40

        @JvmStatic
        fun main(args: Array<String>) {
            EventQueue.invokeLater {
                val f = JFrame("main.GraphPanel")
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