class Ray(var O: float3, var D: float3) {
    var t: Float = 1e30f
    var objIdx: Int = -1
    var inside: Boolean = false
}