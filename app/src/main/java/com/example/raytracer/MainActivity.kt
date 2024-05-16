package com.example.raytracer

import Direction2PolarCoords
import Fresnel
import MaterialType
import RGB32FtoRGB8
import Ray
import RefractRay
import Sphere
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.math.MathUtils.clamp
import com.example.raytracer.ui.theme.RayTracerTheme
import float3
import fmaxf
import fminf
import reflect
import java.lang.Float.max
import java.lang.Float.min
import java.lang.Math.pow
import java.util.Collections.swap
import kotlin.math.sqrt


// Constants
//    val SCRWIDTH = 256
//    val SCRHEIGHT = 256
val SCRWIDTH = 512
val SCRHEIGHT = 512
val MAX_DEPTH = 4
val AIR = 1.000293f

val PI = 3.14159265358979323846264f
val INVPI = 0.31830988618379067153777f
val PIOVER4 = PI / 4.0f
val PIOVER2 = PI / 2.0f
val E = 2.7182818284590452353602874713526625f
val EPSILON = 0.001f

// Create camera
val cam = Camera(float3(0.0f, 0.0f, -5.0f), float3(0.0f, 0.0f, -1.0f), SCRWIDTH, SCRHEIGHT)

// Add spheres
val sp0 = Sphere(1.0f, float3(0.0f, 0.0f, 0.0f), 0, MaterialType.MIRROR)
val sp1 = Sphere(1.0f, float3(-2.50f, 0.0f, 0.0f), 1)
//val sp1 = Sphere(1.0f, float3(-2.50f, 0.0f, 0.0f), 1, MaterialType.DIALECTRIC)
val sp2 = Sphere(1.0f, float3(2.5f, 0.0f, 0.0f), 2)
val sp3 = Sphere(200.0f, float3(0.0f, -15.0f, 0.0f), 3)
val sp4 = Sphere(1.0f, float3(-2.5f, 0.0f, -2.0f), 4, MaterialType.DIALECTRIC)

val prims = arrayOf(sp0, sp1, sp2, sp3, sp4)

// Add Point-light
val l_color = float3(1.0f, 1.0f, 1.0f) * 100.0f
val p_light = PointLight(float3(0.0f, 3.0f, -2.0f), l_color)

var bitmap: Bitmap = createBitmap(width=SCRWIDTH, height=SCRHEIGHT, config=Bitmap.Config.RGB_565)
var hdri_map = createBitmap(2048, 1024)
var prevTime = System.currentTimeMillis()

// BVH Stuff
const val rootNodeIdx = 0
var nodesUsed = 1
lateinit var bvh: MutableList<BVHNode>
lateinit var triIdx: MutableList<UInt>
val emptyNode: BVHNode = BVHNode(float3(0.0f), float3(0.0f), 0u, 0u)

class MainActivity : ComponentActivity() {

    lateinit var newView: ImageView
    lateinit var fwdBut: Button
    lateinit var aftBut: Button
    lateinit var leftBut: Button
    lateinit var rightBut: Button
    lateinit var upBut: Button
    lateinit var downBut: Button
    lateinit var rotLeftBut: Button
    lateinit var rotRightBut: Button
    lateinit var perfText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // Set up buttons
        fwdBut = findViewById(R.id.fwd_but)
        fwdBut.setOnClickListener {
            cam.InputHandle(InputType.FWD, (System.currentTimeMillis() - prevTime))
            prevTime = System.currentTimeMillis()
            UpdatePixels()
            UpdateMetric(perfText, prevTime)
            newView.setImageBitmap(bitmap)
        }
        aftBut = findViewById(R.id.aft_but)
        aftBut.setOnClickListener {
            cam.InputHandle(InputType.AFT, (System.currentTimeMillis() - prevTime))
            prevTime = System.currentTimeMillis()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        leftBut = findViewById(R.id.left_but)
        leftBut.setOnClickListener {
            cam.InputHandle(InputType.MOVE_LEFT, (System.currentTimeMillis() - prevTime))
            prevTime = System.currentTimeMillis()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        rightBut = findViewById(R.id.right_but)
        rightBut.setOnClickListener {
            cam.InputHandle(InputType.MOVE_RIGHT, (System.currentTimeMillis() - prevTime))
            prevTime = System.currentTimeMillis()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        upBut = findViewById(R.id.up_but)
        upBut.setOnClickListener {
            cam.InputHandle(InputType.UP, (System.currentTimeMillis() - prevTime))
            prevTime = System.currentTimeMillis()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        downBut = findViewById(R.id.down_but)
        downBut.setOnClickListener {
            cam.InputHandle(InputType.DOWN, (System.currentTimeMillis() - prevTime))
            prevTime = System.currentTimeMillis()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        rotLeftBut = findViewById(R.id.rot_left_but)
        rotLeftBut.setOnClickListener {
            cam.InputHandle(InputType.ROT_LEFT, (System.currentTimeMillis() - prevTime))
            prevTime = System.currentTimeMillis()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        rotRightBut = findViewById(R.id.rot_right_but)
        rotRightBut.setOnClickListener {
            cam.InputHandle(InputType.ROT_RIGHT, (System.currentTimeMillis() - prevTime))
            prevTime = System.currentTimeMillis()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }

        perfText = findViewById(R.id.perf_text)

        // Load HDRI
        hdri_map = BitmapFactory.decodeResource(resources, R.drawable.symmetrical_garden_02_2k1714319279)
//        hdri_map = BitmapFactory.decodeResource(resources, R.drawable.vignaioli_night_2k1714341932)

        // Custom ImageView
        newView = ImageView(this)
//        val main_layout = findViewById<LinearLayout>(R.id.main_layout)
        val main_layout = findViewById<FrameLayout>(R.id.main_frame)
        main_layout.addView(newView)

        // TODO: Improve layout!
        newView.layoutParams.width = SCRWIDTH * 2
        newView.layoutParams.height = SCRHEIGHT * 2
        newView.scaleType = ImageView.ScaleType.CENTER_CROP
        newView.x = 20.0f
        newView.y = 500f
        newView.setBackgroundColor(Color.MAGENTA)

        BuildBVH()

        UpdatePixels()
        newView.setImageBitmap(bitmap)

        // Update performance metric
        val curTime = System.currentTimeMillis()
        perfText.text = (curTime - prevTime).toString()+"ms"
        prevTime = curTime
    }
}

fun UpdateMetric(perfText: TextView, startTime: Long)
{
    // Update performance metric
    val curTime = System.currentTimeMillis()
    val delta = (curTime - startTime)
    perfText.text = delta.toString()+"ms"
}

class MyDrawable : Drawable() {
    private val redPaint: Paint = Paint().apply { setARGB(255, 255, 0, 0) }

    override fun draw(canvas: Canvas) {
        // Get the drawable's bounds
        val width: Int = bounds.width()
        val height: Int = bounds.height()
        val radius: Float = Math.min(width, height).toFloat() / 2f

        // Draw a red circle in the center
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, redPaint)
    }

    override fun setAlpha(alpha: Int) {
        // This method is required
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // This method is required
    }

    override fun getOpacity(): Int =
        // Must be PixelFormat.UNKNOWN, TRANSLUCENT, TRANSPARENT, or OPAQUE
        PixelFormat.OPAQUE
}

fun DirectIllumination(ray: Ray, surfaceColor: float3): Int
{
    val I = ray.IntersectionPoint()
    val N = prims[ray.objIdx].getNormal(ray)
    val L = p_light.pos - I

    val dist = L.length()
    L.normalize()

    // Light normal should point downwards
    val lightNormal = float3(0.0f, -1.0f, 0.0f)

    val cos_o = lightNormal.dot(-L)
    val cos_i = L.dot(N);

    if (cos_o > 0.0f && cos_i > 0.0f)
    {
        // Set up shadow ray
        var shadow_ray = Ray(I + L * EPSILON, L)
        shadow_ray.t = dist - 2.0f * EPSILON
        val init_dist = shadow_ray.t

        // TODO: Add proper occlusion check!
        sp0.intersect(shadow_ray)
        if (shadow_ray.t < init_dist)
            return Color.BLACK
        sp1.intersect(shadow_ray)
        if (shadow_ray.t < init_dist)
            return Color.BLACK
        sp2.intersect(shadow_ray)
        if (shadow_ray.t < init_dist)
            return Color.BLACK
        sp3.intersect(shadow_ray)
        if (shadow_ray.t < init_dist)
            return Color.BLACK
        sp4.intersect(shadow_ray)
        if (shadow_ray.objIdx != -1)
            return Color.BLACK

        // BRDF calculation
        var BRDF = float3(INVPI, INVPI, INVPI)
        BRDF *= surfaceColor
        // TODO: Improve solid angle!
        val solidAngle = cos_o / (dist * dist)       // Pretend this is right
        val color = BRDF * p_light.color * solidAngle * cos_i
        return RGB32FtoRGB8(color)
    }

    return Color.BLACK
}

fun TraceRay(ray: Ray, depth: Int): Int
{
    // Intersect spheres
    for (i in prims.indices)
    {
        prims[i].intersect(ray)
    }

    if (depth < 0)
        return Color.BLACK

    // Check for hit
    if (ray.t < 1e30f)
    {
        if (ray.objIdx == 0)
        {
            if (sp0.mat == MaterialType.MIRROR)
            {
                var I = ray.IntersectionPoint()
                val dir = reflect(ray.D, sp0.getNormal(ray))
                var reflect_ray = Ray(I + dir * EPSILON, dir)

                return TraceRay(reflect_ray, depth - 1)
            }
            else
                return DirectIllumination(ray, float3(1.0f, 0.0f, 0.0f))
        }
        else if (ray.objIdx == 1)
            if (sp1.mat == MaterialType.DIALECTRIC)
            {
                val cosI = clamp(ray.D.dot(sp1.getNormal(ray)), -1.0f, 1.0f)
                var n1 = AIR;
                var n2 = 1.03f;

                if (ray.inside)
                {
                    val temp = n1
                    n1 = n2
                    n2 = temp
                }

                val R = Fresnel(ray.D, sp1.getNormal(ray), n1, n2, cosI, 0.1f)

                // TODO: Perhaps randomly!
//                if (R < 1.0f)
//                if (R < Math.random().toFloat())
                if (true)
                {
                    val rD = RefractRay(ray.D, sp1.getNormal(ray), n1, n2, cosI)
                    val rO = ray.IntersectionPoint() + rD * EPSILON

                    val refract_ray = Ray(rO, rD)
//                    refract_ray.inside = !ray.inside

                    if (!ray.inside)
                        refract_ray.inside = true

                    var beer = 255.0f
                    //  Calculate absorption with beers law
                    if (ray.inside)
                    {
                        // TODO: Add proper Absorption!
                        /*beer.x = pow(E, -prim.absorbX * prevRay.t);
                        beer.y = pow(E, -prim.absorbY * prevRay.t);
                        beer.z = pow(E, -prim.absorbZ * prevRay.t);*/
                        beer = pow(E.toDouble(), (-0.5f * ray.t).toDouble()).toFloat()
                    }

//                    return TraceRay(refract_ray, depth - 1) + RGB32FtoRGB8(float3(beer, beer, beer))
                    return TraceRay(refract_ray, depth - 1)
                }
                else
                {
                    var I = ray.IntersectionPoint()
                    val dir = reflect(ray.D, sp1.getNormal(ray))
                    var reflect_ray = Ray(I + dir * EPSILON, dir)

                    return TraceRay(reflect_ray, depth - 1)
                }
            }
            else
                return DirectIllumination(ray, float3(0.0f, 1.0f, 0.0f))
        else if (ray.objIdx == 2)
            return DirectIllumination(ray, float3(1.0f, 1.0f, 0.8f))
        else if (ray.objIdx == 3)
            return DirectIllumination(ray, float3(1.0f, 1.0f, 1.0f))
        else if (ray.objIdx == 4)
            if (sp4.mat == MaterialType.DIALECTRIC)
            {
                val cosI = clamp(ray.D.dot(sp4.getNormal(ray)), -1.0f, 1.0f)
                var n1 = AIR;
                var n2 = 1.03f;

                if (ray.inside)
                {
                    val temp = n1
                    n1 = n2
                    n2 = temp
                }

                val R = Fresnel(ray.D, sp4.getNormal(ray), n1, n2, cosI, 0.1f)

                // TODO: Perhaps randomly!
//                if (R < 1.0f)
//                if (R < Math.random().toFloat())
                if (true)
                {
                    val rD = RefractRay(ray.D, sp4.getNormal(ray), n1, n2, cosI)
                    val rO = ray.IntersectionPoint() + rD * EPSILON

                    val refract_ray = Ray(rO, rD)
//                    refract_ray.inside = !ray.inside

                    if (!ray.inside)
                        refract_ray.inside = true

                    var beer = 255.0f
                    //  Calculate absorption with beers law
                    if (ray.inside)
                    {
                        // TODO: Add proper Absorption!
                        /*beer.x = pow(E, -prim.absorbX * prevRay.t);
                        beer.y = pow(E, -prim.absorbY * prevRay.t);
                        beer.z = pow(E, -prim.absorbZ * prevRay.t);*/
                        beer = pow(E.toDouble(), (-0.5f * ray.t).toDouble()).toFloat()
                    }

//                    return TraceRay(refract_ray, depth - 1) + RGB32FtoRGB8(float3(beer, beer, beer))
                    return TraceRay(refract_ray, depth - 1)
                }
                else
                {
                    var I = ray.IntersectionPoint()
                    val dir = reflect(ray.D, sp4.getNormal(ray))
                    var reflect_ray = Ray(I + dir * EPSILON, dir)

                    return TraceRay(reflect_ray, depth - 1)
                }
            }
            else
                return DirectIllumination(ray, float3(0.0f, 1.0f, 0.0f))
        else
            return Color.BLACK
    }
    // Exited scene
    else
    {
        // Sample HDRI
        val pixel_coords: float2 = Direction2PolarCoords(ray.D)
//        val sample = hdri_map[(pixel_coords.x * (1024 - 1)).toInt(), (pixel_coords.y * (2048 - 1)).toInt()]
        val sample = hdri_map[(pixel_coords.x * (hdri_map.height - 1)).toInt(), (pixel_coords.y * (hdri_map.height - 1)).toInt()]
//        val sample = hdri_map[pixel_coords.x.toInt(), pixel_coords.y.toInt()]
        return sample
    }
}

fun UpdatePixels()
{
//    val cols: IntArray = IntArray(SCRWIDTH*SCRHEIGHT) {1 * it}
    val cols: IntArray = IntArray(SCRWIDTH*SCRHEIGHT)

    // Trace rays
    for (i in 0 until SCRWIDTH)
        for (j in 0 until SCRHEIGHT)
        {
            val ray = cam.GeneratePrimaryRay(i.toFloat(), j.toFloat())

            cols[i + j * SCRWIDTH] = TraceRay(ray, MAX_DEPTH)
        }

//    bitmap.setPixels(cols, 0, 256, 0, 0, SCRWIDTH, SCRHEIGHT)
    bitmap.setPixels(cols, 0, SCRWIDTH, 0, 0, SCRWIDTH, SCRHEIGHT)
}

// **********************************************************************************************
// BVH Functions
// **********************************************************************************************
fun IntersectAABB(ray: Ray, bmin: float3, bmax: float3): Float
{
    val tx1 = (bmin.x - ray.O.x) / ray.D.x
    val tx2 = (bmax.x - ray.O.x) / ray.D.x
    var tmin: Float = min(tx1, tx2)
    var tmax: Float = max(tx1, tx2)
    val ty1 = (bmin.y - ray.O.y) / ray.D.y
    val ty2 = (bmax.y - ray.O.y) / ray.D.y
    tmin = max(tmin, min(ty1, ty2))
    tmax = min(tmax, max(ty1, ty2))
    val tz1 = (bmin.z - ray.O.z) / ray.D.z
    val tz2 = (bmax.z - ray.O.z) / ray.D.z
    tmin = max(tmin, min(tz1, tz2))
    tmax = min(tmax, max(tz1, tz2))

    return if (tmax >= tmin && tmin < ray.t && tmax > 0) tmin else 1e30f
}

fun IntersectBVH(ray: Ray, nodeIdx: UInt)
{
    var node = bvh[nodeIdx.toInt()].copy()

    var node_stack: MutableList<BVHNode> = MutableList(64) { emptyNode.copy() }

    var stack_ptr = 0

    while (true) {
        if (node.triCount > 0u)
        {
            for (i in 0 until node.triCount.toInt())
            {
                var prev_t = ray.t
                prims[triIdx[node.leftFirst.toInt() + i].toInt()].intersect(ray)
                if (prev_t > ray.t)
                {
                    ray.objIdx = triIdx[node.leftFirst.toInt() + i].toInt()
                    prev_t = ray.t
                }
            }

            if (stack_ptr == 0)
                break
            else
                node = node_stack[--stack_ptr].copy()

            continue
        }

        // Children
        var child1 = bvh[node.leftFirst.toInt()].copy()
        var child2 = bvh[node.leftFirst.toInt() + 1].copy()

        var dist1 = IntersectAABB(ray, child1.aabbMin, child1.aabbMax)
        var dist2 = IntersectAABB(ray, child2.aabbMin, child2.aabbMax)

        if (dist1 > dist2)
        {
            val tmp = dist1
            dist1 = dist2
            dist2 = tmp

            child1 = bvh[node.leftFirst.toInt() + 1].copy()
            child2 = bvh[node.leftFirst.toInt()].copy()
        }
        if (dist1 == 1e30f)
        {
            if (stack_ptr == 0)
                break;
            else
                node = node_stack[--stack_ptr];
        }
        else
        {
            node = child1;
            if (dist2 != 1e30f)
                node_stack[stack_ptr++] = child2;
        }
    }
}

fun UpdateNodeBounds(nodeIdx: UInt)
{
    var updatedNode: BVHNode = bvh[nodeIdx.toInt()]
    updatedNode.aabbMin = float3(1e30f, 1e30f, 1e30f)
    updatedNode.aabbMax = float3(-1e30f, -1e30f, -1e30f)

    val first = updatedNode.leftFirst
    for (i in 0 until updatedNode.triCount.toInt())
    {
        val leafTriIdx = triIdx[first.toInt() + i]

        var leafPrim = prims[leafTriIdx.toInt()]

        val s_aabb_min: float3 = leafPrim.center - float3(sqrt(leafPrim.radius))
        val s_aabb_max: float3 = leafPrim.center + float3(sqrt(leafPrim.radius))

        // Adjust
        updatedNode.aabbMin = fminf(updatedNode.aabbMin, s_aabb_min)
        updatedNode.aabbMax = fmaxf(updatedNode.aabbMax, s_aabb_max)
    }

    // TODO: This may not be needed, as Kotlin probably uses pass-by-reference for objects!
    bvh[nodeIdx.toInt()] = updatedNode
}

fun Subdivide(nodeIdx: UInt)
{
    // TODO: Implement SAH for giggles!

    // terminate recursion
    val node = bvh[nodeIdx.toInt()]
    if (node.triCount <= 2u) return
    // determine split axis and position
    val extent: float3 = node.aabbMax - node.aabbMin

    // Mid-point split
    var axis = 0
    if (extent.y > extent.x)
    {axis = 1}
    if (extent.z > extent[axis])
    {axis = 2}
    val splitPos: Float = node.aabbMin[axis] + extent[axis] * 0.5f

    // in-place partition
    var i = node.leftFirst.toInt()
    var j: Int = i + node.triCount.toInt() - 1
    while (i <= j)
    {
        if (prims[triIdx[i].toInt()].center[axis] < splitPos)
            i++
        else
            swap(triIdx, i, j--)
//            swap(triIdx, triIdx[i].toInt(), triIdx[j--].toInt())
    }

    // abort split if one of the sides is empty
    val leftCount: Int = i - node.leftFirst.toInt()
    if (leftCount == 0 || leftCount == node.triCount.toInt()) return

    val b = (8).toUInt()

    // create child nodes
    val leftChildIdx = nodesUsed++
    val rightChildIdx = nodesUsed++
    bvh[leftChildIdx].leftFirst = node.leftFirst
    bvh[leftChildIdx].triCount = leftCount.toUInt()
    bvh[rightChildIdx].leftFirst = i.toUInt()
    bvh[rightChildIdx].triCount = node.triCount - leftCount.toUInt()
    node.leftFirst = leftChildIdx.toUInt()
    node.triCount = 0u

    UpdateNodeBounds(leftChildIdx.toUInt())
    UpdateNodeBounds(rightChildIdx.toUInt())

    // Recurse
    Subdivide(leftChildIdx.toUInt())
    Subdivide(rightChildIdx.toUInt())
}

fun BuildBVH(): Boolean
{
    // Resize lists
    bvh = MutableList(prims.size * 2 - 1) { emptyNode.copy() }
    triIdx = MutableList(prims.size) {0u}

    // populate index array
    for (i in prims.indices)
    {
        triIdx[i] = i.toUInt()
    }

    // Assign all prims to root
    bvh[rootNodeIdx].leftFirst = 0u
    bvh[rootNodeIdx].triCount = prims.size.toUInt()

    // Update root node
    UpdateNodeBounds(rootNodeIdx.toUInt())

    // Subdivide recursively
    Subdivide(rootNodeIdx.toUInt())

    return true
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RayTracerTheme {
        Greeting("Android")
    }
}