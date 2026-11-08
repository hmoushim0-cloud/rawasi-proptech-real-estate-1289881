package sa.factory.rawasiproptechreal.engine

import java.util.Locale
import kotlin.math.*

/**
 * 360 LiDAR Spatial Scanner Protocol Engine.
 * Handles Time-of-Flight (ToF) laser range measurements,
 * point-cloud voxel filtering, room boundary detection,
 * and Gross Internal Area (GIA in m²) architectural calculations.
 */
class SpatialLidarScannerProtocolEngine {

    companion object {
        const val SPEED_OF_LIGHT_M_PER_S = 299792458.0
        const val MIN_RANGE_METERS = 0.15
        const val MAX_RANGE_METERS = 120.0
        const val HORIZONTAL_FOV_DEG = 360.0
        const val VERTICAL_FOV_DEG = 135.0
    }

    enum class SurfaceClassification {
        CONCRETE,
        GLASS_TRANSPARENT,
        DRYWALL,
        WOOD_TIMBER,
        METAL_FACADE,
        UNKNOWN
    }

    data class Point3D(
        val x: Double,
        val y: Double,
        val z: Double,
        val intensity: Int,
        val classification: SurfaceClassification
    )

    data class LidarScanSweep(
        val sweepId: String,
        val totalPoints: Int,
        val roomLengthM: Double,
        val roomWidthM: Double,
        val ceilingHeightM: Double,
        val grossAreaSqm: Double,
        val pointDensityPerM2: Double,
        val timestamp: Long = System.currentTimeMillis()
    )

    private var currentSweepIndex = 0

    /**
     * Computes distance from laser Time-of-Flight (round trip picoseconds).
     */
    fun calculateToFDistanceMeters(picoseconds: Long): Double {
        val seconds = picoseconds / 1e12
        val oneWayDist = (seconds * SPEED_OF_LIGHT_M_PER_S) / 2.0
        return oneWayDist.coerceIn(MIN_RANGE_METERS, MAX_RANGE_METERS)
    }

    /**
     * Converts spherical laser coordinates (azimuth, elevation, distance) to Cartesian 3D (X, Y, Z).
     */
    fun sphericalToCartesian(
        azimuthDeg: Double,
        elevationDeg: Double,
        distanceM: Double,
        intensity: Int = 180
    ): Point3D {
        val azRad = Math.toRadians(azimuthDeg)
        val elRad = Math.toRadians(elevationDeg)

        val x = distanceM * cos(elRad) * cos(azRad)
        val y = distanceM * cos(elRad) * sin(azRad)
        val z = distanceM * sin(elRad)

        val classification = when {
            intensity > 220 -> SurfaceClassification.METAL_FACADE
            intensity > 160 -> SurfaceClassification.CONCRETE
            intensity > 100 -> SurfaceClassification.DRYWALL
            intensity > 60 -> SurfaceClassification.WOOD_TIMBER
            intensity < 30 -> SurfaceClassification.GLASS_TRANSPARENT
            else -> SurfaceClassification.UNKNOWN
        }

        return Point3D(
            x = (x * 1000.0).roundToInt() / 1000.0,
            y = (y * 1000.0).roundToInt() / 1000.0,
            z = (z * 1000.0).roundToInt() / 1000.0,
            intensity = intensity,
            classification = classification
        )
    }

    /**
     * Derives architectural floor plan area and room dimensions from a 360-degree LiDAR sweep.
     */
    fun processLidarRoomSweep(
        measuredPoints: List<Point3D>
    ): LidarScanSweep {
        currentSweepIndex++
        val sweepId = "SWEEP-LIDAR-%04d".format(Locale.US, currentSweepIndex)

        // Filter horizontal slice at eye-level (1.0m to 1.5m Z)
        val wallSlice = measuredPoints.filter { it.z in 0.8..1.8 }

        val minX = wallSlice.minOfOrNull { it.x } ?: -4.5
        val maxX = wallSlice.maxOfOrNull { it.x } ?: 4.5
        val minY = wallSlice.minOfOrNull { it.y } ?: -3.5
        val maxY = wallSlice.maxOfOrNull { it.y } ?: 3.5
        val maxZ = measuredPoints.maxOfOrNull { it.z } ?: 3.2

        val length = (maxX - minX).coerceAtLeast(1.0)
        val width = (maxY - minY).coerceAtLeast(1.0)
        val ceiling = maxZ.coerceAtLeast(2.4)
        val grossArea = length * width
        val density = measuredPoints.size / grossArea.coerceAtLeast(1.0)

        return LidarScanSweep(
            sweepId = sweepId,
            totalPoints = measuredPoints.size,
            roomLengthM = (length * 100.0).roundToInt() / 100.0,
            roomWidthM = (width * 100.0).roundToInt() / 100.0,
            ceilingHeightM = (ceiling * 100.0).roundToInt() / 100.0,
            grossAreaSqm = (grossArea * 100.0).roundToInt() / 100.0,
            pointDensityPerM2 = (density * 10.0).roundToInt() / 10.0
        )
    }

    /**
     * Synthesizes a realistic high-density room scan (e.g. Luxury Majlis / Villa Master Suite).
     */
    fun synthesizeRoomScan(lengthM: Double = 8.5, widthM: Double = 6.0, heightM: Double = 3.6): List<Point3D> {
        val points = mutableListOf<Point3D>()
        // Wall points sampling
        for (az in 0 until 360 step 3) {
            val dist = if (az % 90 in 40..50) (widthM / 2.0) * 1.3 else widthM / 2.0
            for (el in -30..60 step 5) {
                points.add(sphericalToCartesian(az.toDouble(), el.toDouble(), dist, 185))
            }
        }
        return points
    }
}
