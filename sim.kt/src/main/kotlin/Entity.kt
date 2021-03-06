import java.awt.geom.Rectangle2D
import javax.vecmath.Vector3d

open class Entity(var origin: Vector3d, var angle: Double) {
    var width = 0.0
    var height = 0.0
    var health = 1.0
    var generation = 1
    var mutations = 0
    var creationTime = System.currentTimeMillis()
    var lastBirthTime = System.currentTimeMillis()

    var bounds = Rectangle2D.Double(0.0, 0.0, 0.0, 0.0)
    fun calculateBounds() {
        bounds = Rectangle2D.Double(origin.x - width/2, origin.y - height/2, width, height)
    }
}