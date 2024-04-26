package com.example.raytracer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ColorSpace
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.createBitmap
import com.example.raytracer.ui.theme.RayTracerTheme
import java.nio.Buffer
import java.nio.IntBuffer

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
                    Greeting("Android")
                    PixelColorTest(baseContext)
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

@Composable
fun PixelColorTest(con: Context)
{
//    val imview = ImageView(con)
//    val myDrawing = MyDrawable()
//    imview.setImageDrawable(myDrawing)
    val cols: IntArray = IntArray(256*256) {1 * it}
    var bitmap = createBitmap(width=256, height=256, config=Bitmap.Config.RGB_565)

    bitmap.setPixels(cols, 0, 256, 0, 0, 256, 256)
    val bmap = bitmap.asImageBitmap()
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