package com.example.raytracer

import Sphere
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.example.raytracer.ui.theme.RayTracerTheme
import float3
import kotlinx.coroutines.delay
import java.util.Timer
import java.util.TimerTask

// Constants
//    val SCRWIDTH = 256
//    val SCRHEIGHT = 256
val SCRWIDTH = 512
val SCRHEIGHT = 512

// Create camera
val cam = Camera(float3(0.0f, 0.0f, -2.0f), float3(0.0f, 0.0f, -1.0f), SCRWIDTH, SCRHEIGHT)

var bmap: ImageBitmap = ImageBitmap(SCRWIDTH, SCRHEIGHT)

val t: Timer = Timer()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RayTracerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    Greeting("Android")
                    t.scheduleAtFixedRate(
                        object : TimerTask() {
                            override fun run() {
                                UpdatePixels()
                            }},
                        0, 1000
                    )
                    PixelColorTest(baseContext)
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Button(onClick = { cam.InputHandle(InputType.FWD, 0.001f) }) {
                        Text(text = "Fwd")
                    }
                }
                Surface(
                    modifier = Modifier
                        .width(250.dp)
                        .padding(75.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Button(onClick = { cam.InputHandle(InputType.MOVE_LEFT, 0.001f) }) {
                        Text(text = "Left")
                    }
                }
                Surface(
                    modifier = Modifier
                        .width(250.dp)
                        .padding(75.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Button(onClick = { cam.InputHandle(InputType.MOVE_RIGHT, 0.001f) }) {
                        Text(text = "Right")
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(125.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Button(onClick = { cam.InputHandle(InputType.AFT, 0.001f) }) {
                        Text(text = "Aft")
                    }
                }
            }
        }
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
    // Add sphere
    val sp0 = Sphere(1.0f, float3(0.0f, 0.0f, 0.0f), 0)

//    val cols: IntArray = IntArray(SCRWIDTH*SCRHEIGHT) {1 * it}
    val cols: IntArray = IntArray(SCRWIDTH*SCRHEIGHT)

    // Trace rays
    for (i in 0 until SCRWIDTH)
        for (j in 0 until SCRHEIGHT)
        {
            val ray = cam.GeneratePrimaryRay(i.toFloat(), j.toFloat())

            // Intersect sphere
            sp0.intersect(ray)

            // Check for hit
            if (ray.t < 1e30f)
            {
                ray.objIdx = sp0.id
                cols[i + j * SCRWIDTH] = 255
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
fun PixelColorTest(con: Context)
{
    // Add sphere
    val sp0 = Sphere(1.0f, float3(0.0f, 0.0f, 0.0f), 0)

//    val cols: IntArray = IntArray(SCRWIDTH*SCRHEIGHT) {1 * it}
    val cols: IntArray = IntArray(SCRWIDTH*SCRHEIGHT)

    // Trace rays
    for (i in 0 until SCRWIDTH)
        for (j in 0 until SCRHEIGHT)
        {
            val ray = cam.GeneratePrimaryRay(i.toFloat(), j.toFloat())

            // Intersect sphere
            sp0.intersect(ray)

            // Check for hit
            if (ray.t < 1e30f)
            {
                ray.objIdx = sp0.id
                cols[i + j * SCRWIDTH] = 255
            }
            else
                cols[i + j * SCRWIDTH] = 0
        }

    var bitmap = createBitmap(width=SCRWIDTH, height=SCRHEIGHT, config=Bitmap.Config.RGB_565)

//    bitmap.setPixels(cols, 0, 256, 0, 0, SCRWIDTH, SCRHEIGHT)
    bitmap.setPixels(cols, 0, SCRWIDTH, 0, 0, SCRWIDTH, SCRHEIGHT)
    bmap = bitmap.asImageBitmap()
    Image(bitmap = bmap, "yes")
    //image.contentDescription = resources.getString(R.string.my_image_desc)
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