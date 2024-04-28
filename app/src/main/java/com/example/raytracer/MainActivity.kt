package com.example.raytracer

import Direction2PolarCoords
import MaterialType
import Ray
import Sphere
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import com.example.raytracer.ui.theme.RayTracerTheme
import float3
import reflect
import java.time.Instant


// Constants
//    val SCRWIDTH = 256
//    val SCRHEIGHT = 256
val SCRWIDTH = 512
val SCRHEIGHT = 512
val MAX_DEPTH = 2

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
val sp2 = Sphere(1.0f, float3(2.5f, 0.0f, 0.0f), 2)
//val sp3 = Sphere(40.0f, float3(0.0f, -7.0f, 0.0f), 3)

var bitmap: Bitmap = createBitmap(width=SCRWIDTH, height=SCRHEIGHT, config=Bitmap.Config.RGB_565)
var hdri_map = createBitmap(2048, 1024)
var prevTime = Instant.EPOCH.epochSecond.toFloat()

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // Set up buttons
        fwdBut = findViewById(R.id.fwd_but)
        fwdBut.setOnClickListener {
            cam.InputHandle(InputType.FWD, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        aftBut = findViewById(R.id.aft_but)
        aftBut.setOnClickListener {
            cam.InputHandle(InputType.AFT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        leftBut = findViewById(R.id.left_but)
        leftBut.setOnClickListener {
            cam.InputHandle(InputType.MOVE_LEFT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        rightBut = findViewById(R.id.right_but)
        rightBut.setOnClickListener {
            cam.InputHandle(InputType.MOVE_RIGHT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        upBut = findViewById(R.id.up_but)
        upBut.setOnClickListener {
            cam.InputHandle(InputType.UP, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        downBut = findViewById(R.id.down_but)
        downBut.setOnClickListener {
            cam.InputHandle(InputType.DOWN, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        rotLeftBut = findViewById(R.id.rot_left_but)
        rotLeftBut.setOnClickListener {
            cam.InputHandle(InputType.ROT_LEFT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }
        rotRightBut = findViewById(R.id.rot_right_but)
        rotRightBut.setOnClickListener {
            cam.InputHandle(InputType.ROT_RIGHT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bitmap)
        }

        // Load HDRI
        hdri_map = BitmapFactory.decodeResource(resources, R.drawable.symmetrical_garden_02_2k1714319279)

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

        UpdatePixels()
        newView.setImageBitmap(bitmap)
    }
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

fun TraceRay(ray: Ray, depth: Int): Int
{
    // Intersect spheres
    sp0.intersect(ray)
    sp1.intersect(ray)
    sp2.intersect(ray)
//    sp3.intersect(ray)

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
                return Color.RED
        }
        else if (ray.objIdx == 1)
            return Color.GREEN
        else if (ray.objIdx == 2)
            return Color.YELLOW
        else if (ray.objIdx == 3)
            return Color.WHITE
        else
            return Color.BLACK
    }
    // Exited scene
    else
    {
//        return Color.BLUE
        // Sample HDRI
        val pixel_coords: float2 = Direction2PolarCoords(ray.D)
        val sample = hdri_map[(pixel_coords.x * (1024 - 1)).toInt(), (pixel_coords.y * (2048 - 1)).toInt()]
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