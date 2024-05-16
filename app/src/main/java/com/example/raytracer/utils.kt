import com.example.raytracer.PI
import com.example.raytracer.float2
import java.lang.Float.max
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

fun RandomFloatRange(min: Float, max: Float): Float
{
    return min + (max - min) * Math.random().toFloat()
}

// AABB adjustment functions
fun fminf(a: Float, b: Float): Float
{
    return if (a < b) a else b              // no ternary in kotlin
}

fun fmaxf(a: Float, b: Float): Float
{
    return if (a > b) a else b
}
fun fminf(a: float3, b: float3): float3
{
    return float3(fminf(a.x, b.x), fminf(a.y, b.y), fminf(a.z, b.z))
}

fun fmaxf(a: float3, b: float3): float3
{
    return float3(fmaxf(a.x, b.x), fmaxf(a.y, b.y), fmaxf(a.z, b.z))
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

fun Fresnel(I: float3, N: float3, n1: Float, n2: Float, cosI: Float, reflectivity: Float): Float
{
    if (reflectivity == 1.0f) return 1.0f;
    // Snell's Law
    val sinI = (n1 / n2) * sqrt(max(0.0f, 1.0f - cosI * cosI))
    // TIR
    if (sinI >= 1)
        return 1.0f

    val cosT = sqrt(max(0.0f, 1.0f - sinI * sinI))

    val abs_cosI = abs(cosI)

    val n2CosI = n2 * abs_cosI
    val n1CosT = n1 * abs_cosI
    val sPolar = (n2CosI - n1CosT) / (n2CosI + n1CosT)
    val n1CosI = n1 * cosI
    val n2CosT = n2 * cosT
    val pPolar = (n1CosI - n2CosT) / (n1CosI + n2CosT)
    val Fr = (sPolar * sPolar + pPolar * pPolar) / 2.0f
    return reflectivity + (1 - reflectivity) * Fr
}

fun RefractRay(D: float3, N: float3, n1: Float, n2: Float, cosI: Float): float3
{
    val abs_cosI = abs(cosI)
    val div = n1 / n2
    val r_d = D * div + N * (div * abs_cosI - sqrt(1.0f - div * div * (1.0f - abs_cosI * abs_cosI)))
    r_d.normalize()
    return r_d
}

fun RGB32FtoRGB8(c: float3): Int
{
    val r: Int = (min(c.x, 1.0f) * 255).toInt()
    val g: Int = (min(c.y, 1.0f) * 255).toInt()
    val b : Int = (min(c.z, 1.0f) * 255).toInt()

    return (r shl 16) + (g shl 8) + b;
}