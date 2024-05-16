package com.example.raytracer

import float3


data class BVHNode(var aabbMin: float3, var aabbMax: float3, var leftFirst: UInt, var triCount: UInt) {
    fun isLeaf(): Boolean
    {
        return triCount > 0u        // WTF is this syntax???
    }
}