package com.bagomri.fajrloop.ui.settings.dialogs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing
import java.util.Locale

data class CityOption(
    val name: String,
    val lat: Double,
    val lng: Double,
    val country: String
)

val defaultCitiesList = listOf(
    CityOption("مكة المكرمة", 21.3891, 39.8579, "السعودية"),
    CityOption("المدينة المنورة", 24.5247, 39.5692, "السعودية"),
    CityOption("الرياض", 24.7136, 46.6753, "السعودية"),
    CityOption("جدة", 21.5433, 39.1728, "السعودية"),
    CityOption("الدمام", 26.4207, 50.0888, "السعودية"),
    CityOption("صنعاء", 15.3694, 44.1910, "اليمن"),
    CityOption("عدن", 12.7855, 45.0187, "اليمن"),
    CityOption("المكلا", 14.5425, 49.1242, "اليمن"),
    CityOption("سيئون", 15.9419, 48.7892, "اليمن"),
    CityOption("تعز", 13.5795, 44.0209, "اليمن"),
    CityOption("دبي", 25.2048, 55.2708, "الإمارات"),
    CityOption("أبوظبي", 24.4539, 54.3773, "الإمارات"),
    CityOption("القاهرة", 30.0444, 31.2357, "مصر"),
    CityOption("الكويت", 29.3759, 47.9774, "الكويت"),
    CityOption("مسقط", 23.5880, 58.3829, "عُمان"),
    CityOption("الدوحة", 25.2854, 51.5310, "قطر"),
    CityOption("عمان", 31.9454, 35.9284, "الأردن")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectionDialog(
    currentCity: String,
    onCitySelect: (cityName: String, lat: Double, lng: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isDetectingGps by remember { mutableStateOf(false) }

    fun detectGpsLocation() {
        isDetectingGps = true
        try {
            val hasFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (!hasFinePermission && !hasCoarsePermission) {
                Toast.makeText(context, "الرجاء تفعيل صلاحية الموقع من إعدادات الهاتف أولاً", Toast.LENGTH_LONG).show()
                isDetectingGps = false
                return
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                Toast.makeText(context, "الرجاء تشغيل خدمة تحديد الموقع (GPS) في هاتفك", Toast.LENGTH_LONG).show()
                isDetectingGps = false
                return
            }

            val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER

            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(location: android.location.Location) {
                    try {
                        locationManager.removeUpdates(this)
                    } catch (e: Exception) {}

                    var detectedName = "موقعي الحالي (GPS)"
                    try {
                        val geocoder = Geocoder(context, Locale("ar"))
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val localityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: addr.subLocality
                            if (!localityName.isNullOrEmpty()) {
                                detectedName = localityName
                            }
                        }
                    } catch (e: Exception) {
                        detectedName = "موقعي الحالي (GPS)"
                    }

                    onCitySelect(detectedName, location.latitude, location.longitude)
                    Toast.makeText(context, "📍 تم تحديد الموقع الفعلي بنجاح: $detectedName", Toast.LENGTH_LONG).show()
                    isDetectingGps = false
                    onDismiss()
                }

                override fun onProviderDisabled(p: String) {}
                override fun onProviderEnabled(p: String) {}
                @Suppress("DEPRECATION")
                override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}
            }

            locationManager.requestLocationUpdates(provider, 0L, 0f, listener, android.os.Looper.getMainLooper())

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isDetectingGps) {
                    val lastKnown = locationManager.getLastKnownLocation(provider)
                    if (lastKnown != null) {
                        try {
                            locationManager.removeUpdates(listener)
                        } catch (e: Exception) {}

                        var detectedName = "موقعي الحالي (GPS)"
                        try {
                            val geocoder = Geocoder(context, Locale("ar"))
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(lastKnown.latitude, lastKnown.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                val localityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                                if (!localityName.isNullOrEmpty()) {
                                    detectedName = localityName
                                }
                            }
                        } catch (e: Exception) {}

                        onCitySelect(detectedName, lastKnown.latitude, lastKnown.longitude)
                        Toast.makeText(context, "📍 تم تحديد الموقع بنجاح: $detectedName", Toast.LENGTH_SHORT).show()
                        isDetectingGps = false
                        onDismiss()
                    } else {
                        isDetectingGps = false
                        Toast.makeText(context, "تعذر التقاط إشارة GPS الآن، اختر مدينتك من القائمة", Toast.LENGTH_LONG).show()
                    }
                }
            }, 3500L)

        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ أثناء تحديد الموقع: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            isDetectingGps = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FajrLoopColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl)
        ) {
            Text(
                text = "اختيار المدينة والموقع (GPS)",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FajrLoopColors.Primary,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            // Auto GPS Button
            FajrPrimaryButton(
                text = if (isDetectingGps) "جاري تحديد الموقع..." else "تحديد الموقع تلقائياً عبر (GPS) 📍",
                onClick = { detectGpsLocation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.lg),
                leadingIcon = Icons.Outlined.MyLocation
            )

            Text(
                text = "أو اختر مدينتك من القائمة التالية:",
                fontFamily = PpNmArabic,
                fontSize = 13.sp,
                color = FajrLoopColors.TextSecondary,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                defaultCitiesList.forEach { city ->
                    val isSelected = currentCity.contains(city.name) || city.name.contains(currentCity)

                    FajrCard(
                        borderColor = if (isSelected) FajrLoopColors.Primary.copy(alpha = 0.5f) else FajrLoopColors.Border,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCitySelect(city.name, city.lat, city.lng)
                                Toast.makeText(context, "تم اختيار مدينة ${city.name}", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = city.name,
                                    fontFamily = PpNmArabic,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp,
                                    color = if (isSelected) FajrLoopColors.Primary else FajrLoopColors.TextPrimary
                                )
                                Text(
                                    text = city.country,
                                    fontFamily = PpNmArabic,
                                    fontSize = 11.sp,
                                    color = FajrLoopColors.TextSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "محدد",
                                    tint = FajrLoopColors.Primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}
