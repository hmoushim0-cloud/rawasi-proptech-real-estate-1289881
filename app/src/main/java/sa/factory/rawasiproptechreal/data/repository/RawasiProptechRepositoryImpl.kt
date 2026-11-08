package sa.factory.rawasiproptechreal.data.repository

import sa.factory.rawasiproptechreal.domain.repository.RawasiProptechRepository
import sa.factory.rawasiproptechreal.hardware.HardwareSensorDriver
import sa.factory.rawasiproptechreal.model.RawasiProptechItem
import sa.factory.rawasiproptechreal.model.RawasiProptechOrder
import sa.factory.rawasiproptechreal.model.RawasiProptechTelemetry
import kotlinx.coroutines.flow.*

class RawasiProptechRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : RawasiProptechRepository {

    private val _catalog = MutableStateFlow<List<RawasiProptechItem>>(listOf(
        RawasiProptechItem(
            id = "PROP-01",
            title = "قصر حطين المودرن — مسح ليزري 3D كامل",
            description = "فيلا فاخرة بمساحة 680 م² مع مسبح خاص وحديقة علوية، موثقة بمسح LiDAR بدقة مليمترية",
            priceSar = 5500.0,
            badge = "مسح معتمد",
            category = "فلل فاخرة"
        ),
        RawasiProptechItem(
            id = "PROP-02",
            title = "بنتهاوس برج كافد المالي — إطلالة بانورامية 360",
            description = "شقة سكنية علوية فاخرة بمساحة 410 م² في مركز الملك عبدالله المالي، مزودة بنموذج تفاعلي ثلاثي الأبعاد",
            priceSar = 4200.0,
            badge = "حصرى",
            category = "أبراج سكنية"
        ),
        RawasiProptechItem(
            id = "PROP-03",
            title = "أرض استثمارية تجارية على طريق الملك سلمان",
            description = "مساحة 2500 م² برخصة بناء تجاري مكاتب ومعارض، مرفوعة مساحيا مع تصوير جوي 3D",
            priceSar = 3500.0,
            badge = "فرصة ذهبية",
            category = "أراضي استثمارية"
        ),
        RawasiProptechItem(
            id = "PROP-04",
            title = "طابق مكاتب ذكية في بوابة الأعمال (Business Gate)",
            description = "مساحة إدارية مفتوحة 850 م² مجهزة بشبكات ألياف ضوئية وجولة Matterport Pro3 واقعية",
            priceSar = 4800.0,
            badge = "جاهز للتسليم",
            category = "مساحات مكتبية"
        ),
        RawasiProptechItem(
            id = "PROP-05",
            title = "جولة مسح مكاني خاصة مع مهندس مساحة معتمد",
            description = "خدمة رفع مساحي ثلاثي الأبعاد بعتاد Matterport Pro3 مع توليد نموذج Dollhouse وملف CAD/BIM",
            priceSar = 2800.0,
            badge = "خدمة ميدانية",
            category = "جولات 3D حية"
        )
    ))
    override val catalogFlow: Flow<List<RawasiProptechItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<RawasiProptechItem>>(emptyList())
    override val activeCart: StateFlow<List<RawasiProptechItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<RawasiProptechTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            RawasiProptechTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 98,
                signalDbm = -52
            )
        }

    override fun addToCart(item: RawasiProptechItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: RawasiProptechItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): RawasiProptechOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = RawasiProptechOrder(
            orderId = "RAWASI-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
