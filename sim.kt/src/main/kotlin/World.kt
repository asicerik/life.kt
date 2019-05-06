import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.lang.Exception
import javax.vecmath.Vector3d

class World {

    var interpolate = false

    // Bugs
    val bugs = mutableListOf<Bug>()
    val numBugs = 50

    // Food
    val food = mutableListOf<Food>()
    val numFood = 50
    val regrowthChance = 0.005

    // Chemical grid
    val chemGridSize = 25
    var chemGridUpdateCount = 0
    var chemGrid = arrayOf(arrayOf(0.0))

    var bounds = Rectangle(0,0)

    fun create(bounds: Rectangle) {
        this.bounds = bounds
        createChemGrid()
        for (i in 0 until numBugs) {
            val x = bounds.width * Math.random()
            val y = bounds.height * Math.random()
            val theta = Math.PI * Math.random()
            bugs.add(0, Bug(Vector3d(x, y, 0.0), theta))
        }
        for (i in 0 until numFood) {
            val x = (bounds.width - 100) * Math.random()
            val y = (bounds.height - 100) * Math.random()
            val theta = 0.0
            food.add(0, Food(Vector3d(x, y, 0.0), theta))
        }
    }

    fun update(bounds: Rectangle) {
        if (this.bounds != bounds) {
            this.bounds = bounds
            createChemGrid()
        }
        updateChemGrid()
        val newChildren = mutableListOf<Bug>()
        for (bug in bugs) {
            bug.update(this)
            // Children
            if (bug.health > 0.95) {
                if ((System.currentTimeMillis() - bug.lastBirthTime) > 10000) {
                    newChildren.add(bug.spawnChild())
                    bug.health = 0.5
                    bug.lastBirthTime = System.currentTimeMillis()
                }
            }
        }
        bugs.addAll(newChildren)
        for (foodItem in food) {
            if (foodItem.eaten) {
                if (Math.random() < regrowthChance) {
                    foodItem.eaten = false
                }
            }
        }

    }

    fun render(g: Graphics) {
        renderChemGrid(g)
        for (foodItem in food) {
            foodItem.render(g)
        }
        for (bug in bugs) {
            bug.render(g)
        }
    }

    fun getChemGridAt(xIn: Double, yIn: Double): Double {
        // Find the closest, lower grid location
        var xG = Math.floor(xIn/chemGridSize).toInt()
        var yG = Math.floor(yIn/chemGridSize).toInt()
        if (interpolate) {
            if (xG > (chemGrid.size - 1)) {
                return 0.0
            }
            if (yG > (chemGrid[0].size - 1)) {
                return 0.0
            }
            var res = 0.0
            res += chemGrid[xG][yG] * (((xG + 1) * chemGridSize - xIn) / chemGridSize) *
                    (((yG + 1) * chemGridSize - yIn) / chemGridSize)
            res += chemGrid[xG + 1][yG] * ((xIn - xG * chemGridSize) / chemGridSize) *
                    (((yG + 1) * chemGridSize - yIn) / chemGridSize)
            res += chemGrid[xG][yG + 1] * (((xG + 1) * chemGridSize - xIn) / chemGridSize) *
                    ((yIn - yG * chemGridSize) / chemGridSize)
            res += chemGrid[xG + 1][yG + 1] * ((xIn - xG * chemGridSize) / chemGridSize) *
                    ((yIn - yG * chemGridSize) / chemGridSize)
            return res / 4
        } else {
            if (xG > (chemGrid.size - 1)) {
                xG--
            }
            if (yG > (chemGrid[0].size - 1)) {
                yG--
            }
            try {
                return chemGrid[xG][yG]
            } catch (e: Exception) {
                println(e)
            }
        }
        return 0.0
    }

    private fun renderChemGrid(g: Graphics) {
        for (xG in 0 until chemGrid.size) {
            for (yG in 0 until chemGrid[0].size) {
                val x = xG * chemGridSize
                val y = yG * chemGridSize
                if (interpolate) {
                    for (x2 in 0..9) {
                        for (y2 in 0..9) {
                            var intens = getChemGridAt(x.toDouble() + x2*chemGridSize/10, y.toDouble() + y2*chemGridSize/10)
                            if (intens > 1) {
                                intens = 1.0
                            }
                            g.color = Color(0.0f, 0.0f, intens.toFloat())
                            g.fillRect(x+x2*chemGridSize/10,y+y2*chemGridSize/10,chemGridSize/10, chemGridSize/10)
                        }
                    }
                } else {
                    var intens = chemGrid[xG][yG]
                    if (intens > 1) {
                        intens = 1.0
                    }
                    g.color = Color(0.0f, 0.0f, intens.toFloat())
                    g.fillRect(x, y, chemGridSize, chemGridSize)
                }
            }
        }
    }

    private fun createChemGrid() {
        val gridX = Math.ceil(bounds.width.toDouble() / chemGridSize).toInt()
        val gridY = Math.ceil(bounds.height.toDouble() / chemGridSize).toInt()
        chemGrid = Array(gridX) {
            Array(gridY) {
                0.0
            }
        }
    }

    private fun updateChemGrid() {
        if (chemGrid.isEmpty())
            return
        val xG = chemGridUpdateCount
        for (yG in 0 until chemGrid[0].size) {
            chemGrid[xG][yG] = 0.0
            val x = xG * chemGridSize + chemGridSize * 0.5
            val y = yG * chemGridSize + chemGridSize * 0.5
            for (foodItem in food) {
                if (foodItem.eaten)
                    continue
                var dist = Math.sqrt((foodItem.origin.x - x) * (foodItem.origin.x - x) + (foodItem.origin.y - y) * (foodItem.origin.y - y))
                dist = Math.pow(dist, 1.5)
                chemGrid[xG][yG] += 50/dist
            }
        }
        chemGridUpdateCount++
        if (chemGridUpdateCount >= chemGrid.size) {
            chemGridUpdateCount = 0
        }
    }
}