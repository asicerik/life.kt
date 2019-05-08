import java.awt.*
import java.awt.Font.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.vecmath.Vector3d

fun main(args: Array<String>) {
    val view = MainView()
    view.run()
}

class MainView: JFrame() {
    var lastFpsUpdate = 0L
    var fpsCount = 0
    var fps = 0.0

    // Panels
    var simViewPanel = SimViewPanel()

    // World
    val world = World()

    fun run() {
        prepareGui()
        world.create(bounds)
        while (true) {
            val innerBounds = Rectangle(bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom)
            world.update(innerBounds)
            repaint()
            Thread.sleep(16)
        }
    }

    fun prepareGui() {

        setLocation(0, 0)
        title = "Life.kt"

        val mainPanel = JPanel()
        mainPanel.layout = GridBagLayout()
        val c = GridBagConstraints()
        c.fill = GridBagConstraints.BOTH
        c.weighty = 1.0
        c.gridx = 0
        c.gridy = 0
        simViewPanel.preferredSize = Dimension(2600, 1400)
        mainPanel.add(simViewPanel, c)

        add(mainPanel)
        pack()

        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        isVisible = true
        contentPane.revalidate()
        contentPane.repaint()
    }

    // Create a panel to hold the Simulation View
    inner class SimViewPanel: JPanel() {

        override fun paintComponent(g: Graphics?) {
            if (g != null) {
                fpsCount++
                val currTime = System.currentTimeMillis()
                if ((currTime - lastFpsUpdate) > 1000) {
                    fps = 1000.0 * fpsCount.toDouble() / (currTime - lastFpsUpdate).toDouble()
                    lastFpsUpdate = currTime
                    fpsCount = 0
                }
                g.color = Color(0x408040)
                g.fillRect(0,0, bounds.width, bounds.height)
                world.render(g)
                g.color = Color(0xC0C0C0)
                val font = Font("Monospaced", BOLD, 20)
                g.font = font
                g.drawString(String.format("FPS=%3.2f. foodAvg=%3.2f, delta=%3.2f", fps, world.bugs[0].foodAvg, world.bugs[0].foodDelta), 0, height -insets.top - 24)
            }
        }
    }
}


