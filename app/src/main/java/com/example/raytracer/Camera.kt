import Ray
import float3

class Camera(var camPos: float3, var camDir: float3, val SCRWIDTH: Int, val SCRHEIGHT: Int) {
    val aspect: Float = SCRWIDTH.toFloat() / SCRHEIGHT.toFloat()
    var topLeft: float3 = float3(-aspect, 1.0f, 0.0f)
    var topRight: float3 = float3(aspect, 1.0f, 0.0f)
    var bottomLeft: float3 = float3(-aspect, -1.0f, 0.0f)

    fun GeneratePrimaryRay(x: Float, y: Float): Ray
    {
        // Pixel position on screen plane
        val u: Float = x * (1.0f / SCRWIDTH)
        val v: Float = y * (1.0f / SCRHEIGHT)

        val P = topLeft + (topRight - topLeft) * u + (bottomLeft - topLeft) * v
        val p_c = (P - camPos)
        p_c.normalize()
        return Ray(camPos, p_c)
    }
}