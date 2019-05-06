import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import javax.vecmath.Vector3d

class Food(origin: Vector3d, angle: Double): Entity(origin, angle) {
    var eaten = false
    init {
        width = 20.0
        height = 20.0
        calculateBounds()
    }
    fun render(g: Graphics?) {
        if (g == null || eaten) {
            return
        }
        val g2 = g as Graphics2D
        val savedTransform = g2.transform
        val shape = Ellipse2D.Double(origin.x - width/2, origin.y - height/2, width, height)
        g2.rotate(angle, origin.x, origin.y)
        g.color = Color(0x106010)
        g2.fill(shape)
        g2.transform = savedTransform
    }

}