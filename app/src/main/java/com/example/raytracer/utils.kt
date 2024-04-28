import com.example.raytracer.PI
import com.example.raytracer.float2
import kotlin.math.acos
import kotlin.math.atan2
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

fun Direction2PolarCoords(D: float3): float2
{
    // TODO: Can switch to INVPI!
    val a = 1 + atan2(D.x, -D.z) / Math.PI
    val b = acos(D.y) / Math.PI
//    return (float2)(1 + atan2(D.x, -D.z) / PI, acos(D.y) / PI)
    return float2(a.toFloat(), b.toFloat())
}

fun RGB32FtoRGB8(c: float3): Int
{
    val r: Int = (min(c.x, 1.0f) * 255).toInt()
    val g: Int = (min(c.y, 1.0f) * 255).toInt()
    val b : Int = (min(c.z, 1.0f) * 255).toInt()

    return (r shl 16) + (g shl 8) + b;
}