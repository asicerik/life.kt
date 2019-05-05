import java.awt.Rectangle
import javax.vecmath.Vector3d

class World {
    // Bugs
    val bugs = mutableListOf<Bug>()
    val numBugs = 10

    // Food
    val food = mutableListOf<Food>()
    val numFood = 1000

    var bounds = Rectangle(0,0)

    fun create(bounds: Rectangle) {
        this.bounds = bounds
        for (i in 0 until numBugs) {
            val x = bounds.width * Math.random()
            val y = bounds.height * Math.random()
            val theta = Math.PI * Math.random()
            bugs.add(0, Bug(Vector3d(x, y, 0.0), theta))
        }
        for (i in 0 until numFood) {
            val x = bounds.width * Math.random()
            val y = bounds.height * Math.random()
            val theta = 0.0
            food.add(0, Food(Vector3d(x, y, 0.0), theta))
        }
    }

    fun update(bounds: Rectangle) {
        this.bounds = bounds
        for (bug in bugs) {
            bug.update(this)
        }
//        for (foodItem in food) {
//            foodItem.update(bounds)
//        }
    }
}