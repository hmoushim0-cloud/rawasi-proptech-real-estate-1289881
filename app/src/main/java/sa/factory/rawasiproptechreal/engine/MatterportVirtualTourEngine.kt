package sa.factory.rawasiproptechreal.engine

import java.util.Locale
import kotlin.math.*

/**
 * Matterport Pro3 Virtual Tour & Photogrammetry Engine.
 * Manages 360-degree sweep alignments, Dollhouse 3D mesh generation,
 * interactive walkthrough hotspot graphs, and Saudi REGA real estate licensing compliance.
 */
class MatterportVirtualTourEngine {

    data class PanoStation(
        val stationId: String,
        val labelAr: String,
        val x: Double,
        val y: Double,
        val z: Double = 1.5,
        val connectedStations: List<String> = emptyList(),
        val panoResolutionMegaPixels: Int = 134,
        val isAligned: Boolean = true
    )

    data class DollhouseMesh(
        val totalVertices: Int,
        val totalFaces: Int,
        val surfaceAreaM2: Double,
        val volumeM3: Double,
        val textureResolution: String = "8K Ultra-HD"
    )

    data class RegaComplianceAudit(
        val valuerLicenseNumber: String,
        val adLicenseNumber: String,
        val propertyTypeAr: String,
        val districtNameAr: String,
        val cityAr: String = "الرياض",
        val isVerified: Boolean = true
    )

    data class VirtualTourPackage(
        val tourId: String,
        val propertyTitle: String,
        val stations: List<PanoStation>,
        val dollhouse: DollhouseMesh,
        val compliance: RegaComplianceAudit,
        val pricePerSqmSar: Double,
        val totalValuationSar: Double
    )

    private val activeTours = mutableListOf<VirtualTourPackage>()

    init {
        // Seed premier Riyadh luxury villas and penthouses
        activeTours.add(createSampleLuxuryVillaTour())
    }

    /**
     * Calculates Euclidean distance between two panoramic camera stations.
     */
    fun calculateStationDistance(s1: PanoStation, s2: PanoStation): Double {
        val dx = s2.x - s1.x
        val dy = s2.y - s1.y
        val dz = s2.z - s1.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Constructs a Dollhouse 3D mesh representation from registered stations.
     */
    fun constructDollhouseMesh(stations: List<PanoStation>, grossAreaM2: Double, ceilingM: Double): DollhouseMesh {
        val vertices = stations.size * 2400 + (grossAreaM2 * 80).roundToInt()
        val faces = (vertices * 1.85).roundToInt()
        val volume = grossAreaM2 * ceilingM
        return DollhouseMesh(
            totalVertices = vertices,
            totalFaces = faces,
            surfaceAreaM2 = grossAreaM2,
            volumeM3 = (volume * 10.0).roundToInt() / 10.0
        )
    }

    /**
     * Generates a sample luxury villa virtual tour.
     */
    fun createSampleLuxuryVillaTour(): VirtualTourPackage {
        val s1 = PanoStation("ST-01", "المدخل الملكي والبهو الرئيسي", 0.0, 0.0, 1.6, listOf("ST-02", "ST-03"))
        val s2 = PanoStation("ST-02", "مجلس الضيافة الفاخر", 4.5, 1.2, 1.6, listOf("ST-01", "ST-04"))
        val s3 = PanoStation("ST-03", "صالة المعيشة والإطلالة البانورامية", -3.8, 2.5, 1.6, listOf("ST-01", "ST-05"))
        val s4 = PanoStation("ST-04", "غرفة الطعام والضيافة الكبرى", 8.2, 0.5, 1.6, listOf("ST-02"))
        val s5 = PanoStation("ST-05", "الفناء الخارجي والمسبح الخاص", -6.5, 6.0, 1.6, listOf("ST-03"))

        val stations = listOf(s1, s2, s3, s4, s5)
        val grossArea = 680.0
        val ceiling = 3.8
        val mesh = constructDollhouseMesh(stations, grossArea, ceiling)

        val compliance = RegaComplianceAudit(
            valuerLicenseNumber = "VAL-SA-2026-9912",
            adLicenseNumber = "REGA-AD-773410",
            propertyTypeAr = "فيلا سكنية مودرن مستقلة",
            districtNameAr = "حي حطين النموذجي",
            cityAr = "الرياض"
        )

        val pricePerSqm = 9500.0
        val totalVal = grossArea * pricePerSqm

        return VirtualTourPackage(
            tourId = "TOUR-RAWASI-1289881",
            propertyTitle = "قصر حطين المودرن — جولة 3D تفاعلية مع مسح LiDAR كامل",
            stations = stations,
            dollhouse = mesh,
            compliance = compliance,
            pricePerSqmSar = pricePerSqm,
            totalValuationSar = totalVal
        )
    }

    fun getTours(): List<VirtualTourPackage> = activeTours.toList()
}
