# رواسي العقارية — المنصة الذكية للوساطة والمسح المكاني (`rawasi-proptech-real-estate-1289881`)
> منظومة PropTech المتكاملة مع ربط 360 LiDAR Spatial Scanner + Matterport Pro3

- **Project ID**: `#1289881`
- **Commercial Archetype**: `real_estate` (Luxury PropTech, 3D architectural tours, property management, spatial floor plans)
- **Bound Hardware Peripheral**: `360 LiDAR Spatial Scanner + Matterport Pro3`
- **Package Name**: `sa.factory.rawasiproptechreal`
- **Zero-Emoji Invariant**: Strictly Enforced (100% Vector Icons)

---

## [Sidekick Autonomous Update]

### Architecture & Capabilities
1. **360 LiDAR Spatial Scanner Engine (`SpatialLidarScannerProtocolEngine.kt`)**:
   - Time-of-Flight (ToF) laser range calculation with picosecond resolution across 360-degree horizontal and 135-degree vertical fields of view.
   - Surface material classification (Concrete, Transparent Glass, Drywall, Wood, Metal Facade) based on LiDAR reflectivity intensity.
   - Point-cloud horizontal slice filtering (Z = 0.8m to 1.8m) for room boundary detection and Gross Internal Area (GIA in m²) calculation.
   - Dynamic point density metric reporting (pts/m²).

2. **Matterport Pro3 Virtual Tour Engine (`MatterportVirtualTourEngine.kt`)**:
   - Photogrammetry station registration and 3D Euclidean alignment for 134 MP panoramic camera sweeps.
   - Interactive Dollhouse 3D mesh building (vertices, polygon faces, surface area, and volumetric envelope).
   - Hotspot transition network for room-to-room spatial navigation.
   - Saudi Real Estate General Authority (REGA / فال) license compliance validation and valuation generation.

3. **Production UI & Hardware Driver (`HardwareSensorDriver.kt`)**:
   - Interleaved real-time telemetry stream cycling between active LiDAR room sweeps and Matterport Pro3 station alignments.
   - Modern Material 3 Compose interface with WCAG 2.1 AAA contrast compliance.
   - Zero unicode emojis across all UI, models, and notifications.

4. **Regression Verification (`RawasiProptechRegressionTest.kt`)**:
   - 10 automated unit tests covering ToF physics, spherical-to-Cartesian transformation, surface reflectivity classification, Dollhouse mesh building, station distance math, REGA licensing format, and zero-emoji compliance.
