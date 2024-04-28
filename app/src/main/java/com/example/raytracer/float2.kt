package com.example.raytracer

class float2(var x: Float, var y: Float) {
    // Operators
    operator fun unaryMinus(): float2 {return float2(-x, -y)}
    operator fun float2.plusAssign(v: float2)
    {
        x += v.x
        y += v.y
    }
    operator fun float2.timesAssign(v: Float)
    {
        x *= v
        y *= v
    }
    operator fun float2.divAssign(v: Float)
    {
        x /= v
        y /= v
    }
    operator fun plus(b: float2): float2 {return float2(x + b.x, y + b.y)}
    operator fun minus(b: float2): float2 {return float2(x - b.x, y - b.y)}
    operator fun times(b: float2): float2 {return float2(x * b.x, y * b.y)}
    operator fun times(b: Float): float2 {return float2(x * b, y * b)}
    operator fun Float.times(b: float2): float2 {return b * this}
    operator fun float2.div(b: Float): float2 {return (1 / b) * this}
}