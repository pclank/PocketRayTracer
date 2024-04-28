import kotlin.math.min

fun RandomFloatRange(min: Float, max: Float): Float
{
    return min + (max - min) * Math.random().toFloat()
}

fun insideBacksideCheck(n: float3, rayD: float3): float3
{
    var new_n = n
    if (n.dot(rayD) > 0)
        new_n = -n; // hit backside / inside

    return new_n
}

fun RGB32FtoRGB8(c: float3): Int
{
    val r: Int = (min(c.x, 1.0f) * 255).toInt()
    val g: Int = (min(c.y, 1.0f) * 255).toInt()
    val b : Int = (min(c.z, 1.0f) * 255).toInt()

    return (r shl 16) + (g shl 8) + b;
}