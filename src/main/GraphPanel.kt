package main

import java.awt.*
import java.awt.Color.*
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.util.*
import javax.swing.*
import kotlin.math.abs
import javax.swing.JOptionPane
import java.awt.Graphics
import java.awt.AWTEventMulticaster.getListeners
import kotlin.math.sqrt


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
                e.isShiftDown -> Node.selectToggle(nodes, mousePt)
                e.isPopupTrigger -> {
                    Node.selectOne(nodes, mousePt)
                    showPopup(e)
                }
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

        private val newNode = NewNodeAction("New")
        private val clearAll = ClearAction("Clear")
        private val connect = ConnectAction("Connect")
        private val delete = DeleteAction("Delete")
        val defaultButton = JButton(newNode)
        val popup = JPopupMenu()

        init {
            this.layout = FlowLayout(FlowLayout.LEFT)
            this.background = lightGray

            this.add(defaultButton)
            this.add(JButton(clearAll))
            val js = JSpinner()
            js.model = SpinnerNumberModel(RADIUS, 5, 100, 5)
//            js.addChangeListener { e ->
//                val s = e.source as JSpinner
//                radius = s.value as Int
//                this@main.GraphPanel.repaint()
//            }

            popup.add(JMenuItem(newNode))
            popup.add(JMenuItem(connect))
            popup.add(JMenuItem(delete))
        }
    }

    private inner class NewNodeAction(name: String) : AbstractAction(name) {

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

    private inner class ConnectAction(name: String) : AbstractAction(name) {

        override fun actionPerformed(e: ActionEvent) {
            Node.getSelected(nodes, selected)
            if (selected.size > 1) {
                for (i in 0 until selected.size - 1) {
                    val n1 = selected[i]
                    val n2 = selected[i + 1]
                    edges.add(Edge(n1, n2))
                    flow[i, i + 1] = 3
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
    private class Edge(val n1: Node, val n2: Node) {

        fun draw(g: Graphics) {
            val p1 = n1.location
            val p2 = n2.location
            g.color = darkGray
            g.drawString("", p1.x / 2, p1.x * 2)
//            g.drawLine(p1.x, p1.y, p2.x, p2.y)
            drawArrowLine(g, p1.x, p1.y, p2.x, p2.y)
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

        private fun drawArrowLine(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int, d: Int = 6, h: Int = 10) {
            val dx = x2 - x1
            val dy = y2 - y1
            val D = sqrt((dx * dx + dy * dy).toDouble())
            var xm = D - d
            var xn = xm
            var ym = h.toDouble()
            var yn = (-h).toDouble()
            var x: Double
            val sin = dy / D
            val cos = dx / D

            x = xm * cos - ym * sin + x1
            ym = xm * sin + ym * cos + y1.toDouble()
            xm = x

            x = xn * cos - yn * sin + x1
            yn = xn * sin + yn * cos + y1.toDouble()
            xn = x

            val xpoints = intArrayOf(x2, xm.toInt(), xn.toInt())
            val ypoints = intArrayOf(y2, ym.toInt(), yn.toInt())

            g.drawLine(x1, y1, x2, y2)
            g.fillPolygon(xpoints, ypoints, 3)
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
        private val r = 40

        init {
            setBoundary(b)
        }
        
        /**
         * Calculate this node's rectangular boundary.
         */
        private fun setBoundary(b: Rectangle) {
            b.setBounds(location.x - r, location.y - r, 2 * r, 2 * r)
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
            return b.contains(p)
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
                    if (n.contains(p)) {
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
                    if (n.contains(p)) {
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
        private const val RADIUS = 35

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