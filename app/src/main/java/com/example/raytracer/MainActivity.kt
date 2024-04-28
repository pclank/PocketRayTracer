package com.example.raytracer

import Sphere
import android.graphics.Bitmap
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
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.createBitmap
import com.example.raytracer.ui.theme.RayTracerTheme
import float3
import java.time.Instant

// Constants
//    val SCRWIDTH = 256
//    val SCRHEIGHT = 256
val SCRWIDTH = 512
val SCRHEIGHT = 512

// Create camera
val cam = Camera(float3(0.0f, 0.0f, -5.0f), float3(0.0f, 0.0f, -1.0f), SCRWIDTH, SCRHEIGHT)

// Add sphere
val sp0 = Sphere(1.0f, float3(0.0f, 0.0f, 0.0f), 0)
val sp1 = Sphere(1.0f, float3(-2.0f, 0.0f, 0.0f), 1)
val sp2 = Sphere(1.0f, float3(2.0f, 0.0f, 0.0f), 2)

var bmap: ImageBitmap = ImageBitmap(SCRWIDTH, SCRHEIGHT)

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
            newView.setImageBitmap(bmap.asAndroidBitmap())
        }
        aftBut = findViewById(R.id.aft_but)
        aftBut.setOnClickListener {
            cam.InputHandle(InputType.AFT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bmap.asAndroidBitmap())
        }
        leftBut = findViewById(R.id.left_but)
        leftBut.setOnClickListener {
            cam.InputHandle(InputType.MOVE_LEFT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bmap.asAndroidBitmap())
        }
        rightBut = findViewById(R.id.right_but)
        rightBut.setOnClickListener {
            cam.InputHandle(InputType.MOVE_RIGHT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bmap.asAndroidBitmap())
        }
        upBut = findViewById(R.id.up_but)
        upBut.setOnClickListener {
            cam.InputHandle(InputType.UP, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bmap.asAndroidBitmap())
        }
        downBut = findViewById(R.id.down_but)
        downBut.setOnClickListener {
            cam.InputHandle(InputType.DOWN, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bmap.asAndroidBitmap())
        }
        rotLeftBut = findViewById(R.id.rot_left_but)
        rotLeftBut.setOnClickListener {
            cam.InputHandle(InputType.ROT_LEFT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bmap.asAndroidBitmap())
        }
        rotRightBut = findViewById(R.id.rot_right_but)
        rotRightBut.setOnClickListener {
            cam.InputHandle(InputType.ROT_RIGHT, (Instant.EPOCH.epochSecond.toFloat() - prevTime))
            prevTime = Instant.EPOCH.epochSecond.toFloat()
            UpdatePixels()
            newView.setImageBitmap(bmap.asAndroidBitmap())
        }

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
        newView.setImageBitmap(bmap.asAndroidBitmap())
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

fun UpdatePixels()
{
//    val cols: IntArray = IntArray(SCRWIDTH*SCRHEIGHT) {1 * it}
    val cols: IntArray = IntArray(SCRWIDTH*SCRHEIGHT)

    // Trace rays
    for (i in 0 until SCRWIDTH)
        for (j in 0 until SCRHEIGHT)
        {
            val ray = cam.GeneratePrimaryRay(i.toFloat(), j.toFloat())

            // Intersect spheres
            sp0.intersect(ray)
            sp1.intersect(ray)
            sp2.intersect(ray)

            // Check for hit
            if (ray.t < 1e30f)
            {
                if (ray.objIdx == 0)
                    cols[i + j * SCRWIDTH] = Color.RED
                else if (ray.objIdx == 1)
                    cols[i + j * SCRWIDTH] = Color.GREEN
                else if (ray.objIdx == 2)
                    cols[i + j * SCRWIDTH] = Color.YELLOW
            }
            else
                cols[i + j * SCRWIDTH] = 0
        }

    var bitmap = createBitmap(width=SCRWIDTH, height=SCRHEIGHT, config=Bitmap.Config.RGB_565)

//    bitmap.setPixels(cols, 0, 256, 0, 0, SCRWIDTH, SCRHEIGHT)
    bitmap.setPixels(cols, 0, SCRWIDTH, 0, 0, SCRWIDTH, SCRHEIGHT)
    bmap = bitmap.asImageBitmap()
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