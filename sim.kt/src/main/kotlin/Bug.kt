import java.awt.*
import java.awt.geom.Ellipse2D
import javax.vecmath.Vector3d

class Bug(origin: Vector3d, angle: Double): Entity(origin, angle) {
    val vars = mutableListOf<Double>()
    init {
        width = 20.0
        height = 40.0
        for (i in 0..9) {
            vars.add(Math.random())
        }
//        vars[0] = 1.0
//        vars[1] = 0.0
//        vars[0] = 0.6
//        vars[1] = 0.4
//        vars[2] = 0.75
//        vars[3] = 0.5
//        vars[4] = 0.5
    }
    val healthDec = 0.001
    val healthInc = 0.5
    var foodAvg = 0.0
    var foodDelta = 0.0
    var leftSensor = Vector3d(origin)
    var rightSensor = Vector3d(origin)

    val mutationChance = 0.1

    fun spawnChild(): Bug {
        val child = Bug(Vector3d(origin), 0.0)
        child.generation = generation+1
        child.mutations = mutations
        for (i in 0 until 5) {
            child.vars[i] = vars[i]
            if ((Math.random() < mutationChance)) {
                child.vars[i] += (Math.random() - 0.5) / 1
                child.mutations++
            }
        }
        return child
    }

    fun update(world: World) {
        calculateBounds()
        // Check for food
        var ate = false
        for (foodItem in world.food) {
            if (!foodItem.eaten && foodItem.bounds.intersects(bounds)) {
                ate = true
                foodItem.eaten = true
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
//            val foodHere = world.getChemGridAt(origin.x, origin.y)
//            var foodMult = 100 * (foodHere - foodAvg)
//            foodDelta = foodMult
//            if (foodMult <= 0.01) {
//                foodMult = vars[0]
//            } else if (foodMult < 0.5) {
//                foodMult = vars[1]
//            } else {
//                foodMult = vars[2]
//            }
//            foodAvg = foodAvg * 3 /4 + foodHere / 4
//            angle += foodMult * (Math.random() - 0.5) / 1

            // Calculate food to the left and right
            leftSensor.x = origin.x + Math.sin(angle - Math.PI/2 * vars[4]) * 100 * vars[2]
            leftSensor.y = origin.y - Math.cos(angle - Math.PI/2 * vars[4]) * 100 * vars[2]
            rightSensor.x = origin.x + Math.sin(angle + Math.PI/2 * vars[4]) * 100 * vars[2]
            rightSensor.y = origin.y - Math.cos(angle + Math.PI/2 * vars[4]) * 100 * vars[2]
            val leftFood = world.getChemGridAt(leftSensor.x, leftSensor.y)
            val rightFood = world.getChemGridAt(rightSensor.x, rightSensor.y)
            angle += (vars[0] - 0.5) * rightFood + (vars[1] - 0.5) * leftFood + (Math.random() - 0.5) / (vars[4]*10)
            origin.x += Math.sin(angle) * vars[3] * 5
            origin.y -= Math.cos(angle) * vars[3] * 5
            if (origin.x <= 0 || origin.x >= world.bounds.width.toDouble() ||
                origin.y <= 0 || origin.y >= world.bounds.height.toDouble()) {
                angle += Math.PI
            }
//            origin.x = clip(origin.x, 0.0, world.bounds.width.toDouble())
//            origin.y = clip(origin.y, 0.0, world.bounds.height.toDouble())
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
        if (g == null || health == 0.0) {
            return
        }
        val g2 = g as Graphics2D
        val savedTransform = g2.transform
        val shape = Ellipse2D.Double(origin.x - width/2, origin.y - height/2, width, height)
        g2.rotate(angle, origin.x, origin.y)
        g2.paint = Color(1.0.toFloat(), health.toFloat(), health.toFloat())
        g2.fill(shape)
        g2.transform = savedTransform
        val shapeLeft = Ellipse2D.Double(leftSensor.x, leftSensor.y, 5.0, 5.0)
        val shapeRight = Ellipse2D.Double(rightSensor.x, rightSensor.y, 5.0, 5.0)
        g2.paint = Color.WHITE
        g2.fill(shapeLeft)
        g2.fill(shapeRight)
        g.color = Color.WHITE
        val font = Font("Monospaced", Font.BOLD, 20)
        g.font = font
        g.drawString("G $generation M $mutations", origin.x.toFloat() - 20, origin.y.toFloat() + height.toFloat() + 20)
    }

}