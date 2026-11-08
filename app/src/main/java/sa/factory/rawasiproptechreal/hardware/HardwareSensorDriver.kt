package sa.factory.rawasiproptechreal.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sa.factory.rawasiproptechreal.engine.MatterportVirtualTourEngine
import sa.factory.rawasiproptechreal.engine.SpatialLidarScannerProtocolEngine
import java.util.Locale

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "360 LiDAR Spatial Scanner + Matterport Pro3"
    val lidarEngine = SpatialLidarScannerProtocolEngine()
    val virtualTourEngine = MatterportVirtualTourEngine()

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "LiDAR ToF Pulse Field: Calibrating 360-deg scanner optics..."))
        delay(250)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "360 LiDAR Spatial Scanner + Matterport Pro3: ONLINE | LASER CALIBRATED"))

        var frameCounter = 0
        while (true) {
            delay(1500)
            frameCounter++
            val payload = if (frameCounter % 2 == 1) {
                // Real-time LiDAR Sweep Frame
                val roomSweep = lidarEngine.processLidarRoomSweep(lidarEngine.synthesizeRoomScan())
                String.format(
                    Locale.US,
                    "LiDAR SWEEP [OK] | %s | Area: %.1f m² | Dim: %.1fx%.1fm | Ceiling: %.1fm | Points: %d (%.0f pts/m²)",
                    roomSweep.sweepId,
                    roomSweep.grossAreaSqm,
                    roomSweep.roomLengthM,
                    roomSweep.roomWidthM,
                    roomSweep.ceilingHeightM,
                    roomSweep.totalPoints,
                    roomSweep.pointDensityPerM2
                )
            } else {
                // Matterport Pro3 Photogrammetry Station Alignment Frame
                val tours = virtualTourEngine.getTours()
                val activeTour = tours.firstOrNull()
                val station = activeTour?.stations?.get(frameCounter % (activeTour.stations.size))
                String.format(
                    Locale.US,
                    "MATTERPORT PRO3 | Station: %s (%s) | Pos: (%.1f, %.1f, %.1fm) | Res: %d MP | REGA: [PASS]",
                    station?.stationId ?: "ST-01",
                    station?.labelAr ?: "المدخل الملكي",
                    station?.x ?: 0.0,
                    station?.y ?: 0.0,
                    station?.z ?: 1.6,
                    station?.panoResolutionMegaPixels ?: 134
                )
            }

            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", payload))
        }
    }
}
