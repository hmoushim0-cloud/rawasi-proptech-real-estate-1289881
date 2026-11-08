package sa.factory.rawasiproptechreal

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import sa.factory.rawasiproptechreal.data.repository.RawasiProptechRepositoryImpl
import sa.factory.rawasiproptechreal.engine.MatterportVirtualTourEngine
import sa.factory.rawasiproptechreal.engine.MatterportVirtualTourEngine.*
import sa.factory.rawasiproptechreal.engine.SpatialLidarScannerProtocolEngine
import sa.factory.rawasiproptechreal.engine.SpatialLidarScannerProtocolEngine.*
import sa.factory.rawasiproptechreal.hardware.HardwareSensorDriver
import sa.factory.rawasiproptechreal.model.RawasiProptechItem

class RawasiProptechRegressionTest {

    private val lidarEngine = SpatialLidarScannerProtocolEngine()
    private val tourEngine = MatterportVirtualTourEngine()

    @Test
    fun testToFDistanceCalculation() {
        // Round trip: 40 nanoseconds = 40,000 picoseconds -> ~6 meters
        val dist = lidarEngine.calculateToFDistanceMeters(40_000L)
        assertTrue("ToF distance should be ~5.99m", dist in 5.9..6.1)

        // Clamping to boundaries
        val minClamp = lidarEngine.calculateToFDistanceMeters(10L)
        assertEquals(SpatialLidarScannerProtocolEngine.MIN_RANGE_METERS, minClamp, 0.001)

        val maxClamp = lidarEngine.calculateToFDistanceMeters(1_000_000_000L)
        assertEquals(SpatialLidarScannerProtocolEngine.MAX_RANGE_METERS, maxClamp, 0.001)
    }

    @Test
    fun testSphericalToCartesianProjection() {
        // Looking straight ahead (azimuth = 0, elevation = 0, distance = 5m)
        val pForward = lidarEngine.sphericalToCartesian(0.0, 0.0, 5.0, 180)
        assertEquals(5.0, pForward.x, 0.01)
        assertEquals(0.0, pForward.y, 0.01)
        assertEquals(0.0, pForward.z, 0.01)

        // Looking 90 deg right (azimuth = 90, elevation = 0, distance = 4m)
        val pRight = lidarEngine.sphericalToCartesian(90.0, 0.0, 4.0, 180)
        assertEquals(0.0, pRight.x, 0.01)
        assertEquals(4.0, pRight.y, 0.01)
        assertEquals(0.0, pRight.z, 0.01)
    }

    @Test
    fun testSurfaceClassificationByIntensity() {
        val glass = lidarEngine.sphericalToCartesian(0.0, 0.0, 2.0, 20)
        assertEquals(SurfaceClassification.GLASS_TRANSPARENT, glass.classification)

        val concrete = lidarEngine.sphericalToCartesian(0.0, 0.0, 3.0, 190)
        assertEquals(SurfaceClassification.CONCRETE, concrete.classification)

        val metal = lidarEngine.sphericalToCartesian(0.0, 0.0, 4.0, 240)
        assertEquals(SurfaceClassification.METAL_FACADE, metal.classification)
    }

    @Test
    fun testLidarRoomSweepDimensions() {
        val syntheticPoints = lidarEngine.synthesizeRoomScan(lengthM = 8.0, widthM = 6.0, heightM = 3.5)
        val sweep = lidarEngine.processLidarRoomSweep(syntheticPoints)

        assertTrue("Sweep ID must follow protocol naming", sweep.sweepId.startsWith("SWEEP-LIDAR-"))
        assertTrue("Gross area should be positive", sweep.grossAreaSqm > 0.0)
        assertTrue("Total point count should be > 100", sweep.totalPoints > 100)
        assertTrue("Ceiling height should be >= 2.4m", sweep.ceilingHeightM >= 2.4)
    }

    @Test
    fun testDollhouseMeshConstruction() {
        val sampleTour = tourEngine.createSampleLuxuryVillaTour()
        val mesh = sampleTour.dollhouse

        assertTrue("Mesh must have positive vertices", mesh.totalVertices > 1000)
        assertTrue("Mesh must have positive faces", mesh.totalFaces > 2000)
        assertEquals(680.0, mesh.surfaceAreaM2, 0.1)
        assertTrue("Volume must be positive", mesh.volumeM3 > 1000.0)
    }

    @Test
    fun testPanoStationDistance() {
        val s1 = PanoStation("ST-01", "المدخل", 0.0, 0.0, 1.6)
        val s2 = PanoStation("ST-02", "المجلس", 3.0, 4.0, 1.6)
        val distance = tourEngine.calculateStationDistance(s1, s2)
        // 3-4-5 right triangle: dx=3, dy=4, dz=0 -> dist=5.0
        assertEquals(5.0, distance, 0.001)
    }

    @Test
    fun testRegaComplianceLicenseFormat() {
        val tour = tourEngine.createSampleLuxuryVillaTour()
        val comp = tour.compliance

        assertTrue("REGA valuer license must start with VAL-SA-", comp.valuerLicenseNumber.startsWith("VAL-SA-"))
        assertTrue("REGA advertising license must start with REGA-AD-", comp.adLicenseNumber.startsWith("REGA-AD-"))
        assertTrue("Compliance must be verified", comp.isVerified)
        assertEquals("الرياض", comp.cityAr)
    }

    @Test
    fun testHardwareSensorDriverStreaming() = runBlocking {
        val driver = HardwareSensorDriver()
        val firstFrame = driver.observePeripheralStream().first()

        assertNotNull(firstFrame.peripheralName)
        assertEquals("CONNECTING", firstFrame.connectionState)
        assertTrue(firstFrame.livePayload.contains("LiDAR"))
    }

    @Test
    fun testCartAndOrderCheckoutFlow() = runBlocking {
        val driver = HardwareSensorDriver()
        val repo = RawasiProptechRepositoryImpl(driver)

        val item1 = RawasiProptechItem("P1", "فيلا حطين", "وصف الفيلا", 5000.0, "معتمد", "فلل فاخرة")
        val item2 = RawasiProptechItem("P2", "مسح LiDAR", "وصف المسح", 2000.0, "خدمة", "جولات 3D حية")

        repo.addToCart(item1)
        repo.addToCart(item2)
        assertEquals(2, repo.activeCart.value.size)

        val order = repo.checkout("Mada / Apple Pay")
        assertEquals(7000.0, order.subtotalSar, 0.01)
        assertEquals(1050.0, order.vatSar, 0.01)
        assertEquals(8050.0, order.totalSar, 0.01)
        assertTrue("Cart should be cleared after checkout", repo.activeCart.value.isEmpty())
    }

    @Test
    fun testZeroEmojiInvariant() {
        val regexEmoji = "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+".toRegex()
        val tours = tourEngine.getTours()
        for (tour in tours) {
            assertFalse("Tour title must not contain emojis", regexEmoji.containsMatchIn(tour.propertyTitle))
            for (st in tour.stations) {
                assertFalse("Station label must not contain emojis: ${st.labelAr}", regexEmoji.containsMatchIn(st.labelAr))
            }
            assertFalse("District name must not contain emojis", regexEmoji.containsMatchIn(tour.compliance.districtNameAr))
        }
    }
}
