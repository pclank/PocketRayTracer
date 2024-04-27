package com.example.raytracer

import Ray
import float3

enum class InputType {
    FWD, AFT, MOVE_LEFT, MOVE_RIGHT, UP, DOWN, ROT_LEFT, ROT_RIGHT
}

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

    fun InputHandle(but: InputType, t: Float)
    {
        val speed = 0.0025f * t
        var ahead = camDir - camPos
        ahead.normalize()
        var tmpUp = float3(0.0f, 1.0f, 0.0f);
        var right = tmpUp.cross(ahead)
        right.normalize()
        var up = ahead.cross(right)
        up.normalize()

        if (but == InputType.MOVE_LEFT) camPos -= right * speed * 2.0f
        if (but == InputType.MOVE_RIGHT) camPos += right * speed * 2.0f
        if (but == InputType.FWD) camPos += ahead * speed * 2.0f
        if (but == InputType.AFT) camPos -= ahead * speed * 2.0f

        camDir = camPos + ahead;

        if (but == InputType.UP) camDir -= up * speed
        if (but == InputType.DOWN) camDir += up * speed
        if (but == InputType.ROT_LEFT) camDir -= right * speed
        if (but == InputType.ROT_RIGHT) camDir += right * speed

        ahead = camDir - camPos
        ahead.normalize()
        up = ahead.cross(right)
        up.normalize()
        right = up.cross(ahead)
        right.normalize()
        topLeft = camPos + ahead * 2.0f - right * aspect + up;
        topRight = camPos + ahead * 2.0f + right * aspect + up;
        bottomLeft = camPos + ahead * 2.0f - right * aspect - up;
    }
}