import java.awt.*
import java.awt.Font.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane

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

    fun run() {
        prepareGui()
        while (true) {
            simViewPanel.repaint()
            repaint()
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
        simViewPanel.preferredSize = Dimension(1280, 720)
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
                g.color = Color(0xC0C0C0)
                val font = Font("Monospaced", BOLD, 20)
                g.font = font
                g.drawString(String.format("FPS=%3.2f", fps), 0, height -insets.top - 24)
            }
        }
    }
}


