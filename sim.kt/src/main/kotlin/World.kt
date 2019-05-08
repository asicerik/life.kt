import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.lang.Exception
import java.lang.Math.sqrt
import javax.vecmath.Vector3d
import kotlinx.coroutines.*

class World {

    var interpolate = false

    // Bugs
    val bugs = mutableListOf<Bug>()
    val numBugs = 25
    val spawnChance = 0.0001

    // Food
    val food = mutableListOf<Food>()
    val numFood = 50
    val regrowthChance = 0.001

    // Chemical grid
    val chemGridSize = 20
    var chemGridUpdateCount = 0
    var chemGrid = arrayOf(arrayOf(0.0))

    var bounds = Rectangle(0,0)

    fun create(bounds: Rectangle) {
        this.bounds = bounds
        createChemGrid()
//        GlobalScope.launch  { while (true) {
//                updateChemGrid()
//                delay(10)
//            }
//        }
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
        for (i in 0 until bugs.size) {
            val bug = bugs[i]
            // Is this bug dead?
            if (bug.health == 0.0) {
                if (Math.random() < spawnChance) {
                    val x = bounds.width * Math.random()
                    val y = bounds.height * Math.random()
                    val theta = Math.PI * Math.random()
                    bugs[i] = Bug(Vector3d(x, y, 0.0), theta)
                }
            }
            bug.update(this)
            // Children
            if (bug.health > 0.95) {
                if ((System.currentTimeMillis() - bug.lastBirthTime) > 30000) {
                    newChildren.add(bug.spawnChild())
                    bug.health = 0.75
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
                xG = chemGrid.size - 1
            } else if (xG < 0) {
                xG = 0
            }
            if (yG > (chemGrid[0].size - 1)) {
                yG = chemGrid[0].size - 1
            } else if (yG < 0){
                yG = 0
            }
            try {
                return sqrt(chemGrid[xG][yG])
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
                    var gIntens = Math.sqrt(chemGrid[xG][yG])*1 + 0.1
                    var rIntens = Math.sqrt(chemGrid[xG][yG])/4
                    if (gIntens > 1) {
                        gIntens = 1.0
                    }
                    if (rIntens > 1) {
                        rIntens = 1.0
                    }
                    g.color = Color(0.0f, gIntens.toFloat(), rIntens.toFloat())
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
        val gridMinus = 0.1
        var gridPlus = 0.21
        for (xG in 1 until (chemGrid.size-1)) {
//        val xG = chemGridUpdateCount
//        if (xG > 0 && xG < (chemGrid.size-1)) {
            for (yG in 1 until (chemGrid[0].size-1)) {
                chemGrid[xG][yG] -= gridMinus
                val xMin = xG * chemGridSize
                val yMin = yG * chemGridSize
                val xMax = xMin + chemGridSize
                val yMax = yMin + chemGridSize
                var foodCount = 0
                for (foodItem in food) {
                    if (foodItem.eaten)
                        continue
                    if (foodItem.origin.x >= xMin && foodItem.origin.x <= xMax &&
                        foodItem.origin.y >= yMin && foodItem.origin.y <= yMax) {
                        foodCount++
                    }
                }
                chemGrid[xG][yG] = 0.0
                if (foodCount > 0) {
                    chemGrid[xG][yG] = foodCount.toDouble() * 1
                } else {
                    // Add the neighbors
                    for (xDelta in -1..1) {
                        for (yDelta in -1..1) {
                            if (xDelta == 0 || yDelta == 0) {
                                if (chemGrid[xG + xDelta][yG + yDelta] > chemGrid[xG][yG])
                                    chemGrid[xG][yG] += chemGrid[xG + xDelta][yG + yDelta] * gridPlus
                            } else {
                                if (chemGrid[xG + xDelta][yG + yDelta] > chemGrid[xG][yG])
                                    chemGrid[xG][yG] += chemGrid[xG + xDelta][yG + yDelta] * (gridPlus / 8)
                            }
                        }
                    }
//                    if (chemGrid[xG - 1][yG - 1] > chemGrid[xG][yG])
//                        chemGrid[xG][yG] += chemGrid[xG - 1][yG - 1] * gridPlus
//                    if (chemGrid[xG + 1][yG - 1] > chemGrid[xG][yG])
//                        chemGrid[xG][yG] += chemGrid[xG + 1][yG - 1] * gridPlus
//                    if (chemGrid[xG - 1][yG + 1] > chemGrid[xG][yG])
//                        chemGrid[xG][yG] += chemGrid[xG - 1][yG + 1] * gridPlus
//                    if (chemGrid[xG + 1][yG + 1] > chemGrid[xG][yG])
//                        chemGrid[xG][yG] += chemGrid[xG + 1][yG + 1] * gridPlus
//                    if (chemGrid[xG - 1][yG - 1] > chemGrid[xG][yG])
//                        chemGrid[xG][yG] += chemGrid[xG - 1][yG - 1] * gridPlus
//                    if (chemGrid[xG][yG - 1] > chemGrid[xG][yG])
//                        chemGrid[xG][yG] += chemGrid[xG][yG - 1] * gridPlus
//                    if (chemGrid[xG + 1][yG - 1] > chemGrid[xG][yG])
//                        chemGrid[xG][yG] += chemGrid[xG + 1][yG - 1] * gridPlus
                }

                if (chemGrid[xG][yG] < 0.0)
                    chemGrid[xG][yG] = 0.0
            }
        }
        chemGridUpdateCount++
        if (chemGridUpdateCount >= chemGrid.size) {
            chemGridUpdateCount = 0
        }
    }
    private fun updateChemGridDist() {
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