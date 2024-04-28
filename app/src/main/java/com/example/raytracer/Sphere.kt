import kotlin.math.sqrt

class Sphere(val radius: Float, val center: float3, val id: Int) {
    fun intersect(ray: Ray)
    {
        val oc = ray.O - center
        val b = oc.dot(ray.D)
        val c = oc.dot(oc) - radius

        var t = b * b - c
        var d = b * b - c
        if (d <= 0) return
        d = sqrt(d)
        t = -b - d
        var hit: Boolean = t < ray.t && t > 0;
        if (hit)
        {
            ray.t = t
            ray.objIdx = id
            return
        }
        if (c > 0) return; // we're outside; safe to skip option 2
        t = d - b
        hit = t < ray.t && t > 0
        if (hit)
        {
            ray.t = t
            ray.objIdx = id
        }
    }

    fun getNormal(ray: Ray)
    {
        val I = ray.IntersectionPoint()
        var norm = I - center
        norm.normalize()
        insideBacksideCheck(norm, ray.D);
    }
}