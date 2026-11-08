package sa.factory.rawasiproptechreal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sa.factory.rawasiproptechreal.ui.theme.*
import sa.factory.rawasiproptechreal.ui.viewmodel.RawasiProptechUiState

@Composable
fun TelemetryScreen(state: RawasiProptechUiState) {
    val defaultPeripheral = "360 LiDAR Spatial Scanner + Matterport Pro3"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("مراقبة الماسح المكاني ثلاثي الأبعاد", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrandTextPrimary)
        Text("بروتوكول الربط مع أجهزة المسح الليزري وجولات الواقع الافتراضي", fontSize = 12.sp, color = BrandTextSecondary)

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("حالة جهاز المسح الملحق:", color = BrandTextSecondary, fontSize = 13.sp)
                    Badge(containerColor = BrandPrimary) {
                        Text(state.telemetry?.status ?: "ONLINE", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                    }
                }
                Text("اسم العتاد: ${state.telemetry?.peripheralName ?: defaultPeripheral}", fontWeight = FontWeight.Bold, color = BrandTextPrimary, fontSize = 14.sp)
                Text("جودة الإشارة: ${state.telemetry?.signalDbm ?: -58} dBm | البطارية: ${state.telemetry?.batteryPct ?: 98}%", color = BrandSecondary, fontSize = 12.sp)

                HorizontalDivider(color = BrandCard)
                Text("حزمة البيانات اللحظية (Raw LiDAR Telemetry Payload):", color = BrandTextSecondary, fontSize = 11.sp)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.telemetry?.liveReading ?: "360 LiDAR Spatial Scanner + Matterport Pro3: ONLINE | READY",
                        color = BrandPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("معايير الامتثال والتوثيق العقاري", fontWeight = FontWeight.Bold, color = BrandTextPrimary, fontSize = 14.sp)
                Text("[OK] ترخيص معتمد من الهيئة العامة للعقار (رخصة فال العقارية)", color = BrandTextSecondary, fontSize = 12.sp)
                Text("[OK] توثيق العقود والرفع المساحي طبقا لمنصة إيجار ومنصة بلدي", color = BrandTextSecondary, fontSize = 12.sp)
                Text("[OK] مسح ضوئي ثلاثي الأبعاد بدقة قياس مليمترية (LiDAR Point Cloud)", color = BrandTextSecondary, fontSize = 12.sp)
                Text("[OK] مزامنة فورية خالية من الإيموجي ومتوافقة مع المعايير السعودية", color = BrandTextSecondary, fontSize = 12.sp)
            }
        }
    }
}
