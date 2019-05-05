import java.awt.*
import java.awt.geom.Ellipse2D
import javax.vecmath.Vector3d

class Bug(origin: Vector3d, angle: Double): Entity(origin, angle) {
    init {
        width = 20.0
        height = 40.0
        speed = 5.1
    }
    val healthDec = 0.01
    val healthInc = 0.1

    fun update(world: World) {
        calculateBounds()
        // Check for food
        var ate = false
        for (foodItem in world.food) {
            if (foodItem.bounds.intersects(bounds)) {
                ate = true
                foodItem.width = 0.0
                foodItem.height = 0.0
                foodItem.calculateBounds()
                break
            }
        }
        if (ate) {
            health += healthInc
        } else {
            health -= healthDec
        }
        health = clip(health, 0.0, 1.0)
        if (health > 0) {
            angle += (Math.random() - 0.5) / 1
            origin.x += Math.sin(angle) * speed
            origin.y -= Math.cos(angle) * speed
            origin.x = clip(origin.x, 0.0, world.bounds.width.toDouble())
            origin.y = clip(origin.y, 0.0, world.bounds.height.toDouble())
        }
    }

    fun clip(value: Double, min: Double, max: Double): Double {
        if (value < min) {
            return min
        } else if (value >= max) {
            return max
        }
        return value
    }

    fun render(g: Graphics?) {
        if (g == null) {
            return
        }
        val g2 = g as Graphics2D
        val savedTransform = g2.transform
        val shape = Ellipse2D.Double(origin.x - width/2, origin.y - height/2, width, height)
        g2.rotate(angle, origin.x, origin.y)
        g2.paint = Color(1.0.toFloat(), health.toFloat(), health.toFloat())
        g2.fill(shape)
        g2.transform = savedTransform
    }

}