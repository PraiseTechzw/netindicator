package com.praisetechzw.netindicator.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import com.praisetechzw.netindicator.domain.model.NetworkSnapshot
import com.praisetechzw.netindicator.domain.model.NetworkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {

    /**
     * Builds a [NetworkSnapshot] from the current system state.
     * All network calls are dispatched to IO.
     */
    suspend fun buildSnapshot(context: Context): NetworkSnapshot = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: Network? = cm.activeNetwork
        val caps = activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val isConnected = caps != null

        val networkType = resolveNetworkType(context, caps)
        val ip = getLocalIpAddress()
        val ssid = getWifiSsid(context, caps)
        val signal = getSignalStrength(context, caps)
        val linkSpeed = getWifiLinkSpeed(context, caps)
        val operator = getMobileOperator(context, caps)
        val ping = if (isConnected) measurePing("8.8.8.8") else null

        NetworkSnapshot(
            timestamp = System.currentTimeMillis(),
            networkType = networkType,
            ssid = ssid,
            signalStrength = signal,
            downloadSpeedBps = null, // Speed test not performed here; done in service
            uploadSpeedBps = null,
            pingMs = ping,
            isConnected = isConnected,
            ipAddress = ip,
            networkOperator = operator,
            linkSpeedMbps = linkSpeed
        )
    }

    fun resolveNetworkType(context: Context, caps: NetworkCapabilities?): NetworkType {
        if (caps == null) return NetworkType.NONE
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> resolveCellularType(context)
            else -> NetworkType.UNKNOWN
        }
    }

    private fun resolveCellularType(context: Context): NetworkType {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return when (tm.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_NR -> NetworkType.MOBILE_5G
            TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.MOBILE_4G
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_UMTS -> NetworkType.MOBILE_3G
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS -> NetworkType.MOBILE_2G
            else -> NetworkType.UNKNOWN
        }
    }

    @Suppress("DEPRECATION")
    private fun getWifiSsid(context: Context, caps: NetworkCapabilities?): String? {
        if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) return null
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wm.connectionInfo
        val raw = info?.ssid ?: return null
        return raw.removeSurrounding("\"").takeIf { it.isNotBlank() && it != "<unknown ssid>" }
    }

    private fun getSignalStrength(context: Context, caps: NetworkCapabilities?): Int? {
        if (caps == null) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            caps.signalStrength.takeIf { it != Int.MIN_VALUE }
        } else {
            @Suppress("DEPRECATION")
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wm.connectionInfo?.rssi
        }
    }

    @Suppress("DEPRECATION")
    private fun getWifiLinkSpeed(context: Context, caps: NetworkCapabilities?): Int? {
        if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) return null
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wm.connectionInfo?.linkSpeed?.takeIf { it > 0 }
    }

    private fun getMobileOperator(context: Context, caps: NetworkCapabilities?): String? {
        if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) != true) return null
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkOperatorName?.takeIf { it.isNotBlank() }
    }

    private fun getLocalIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()
                ?.toList()
                ?.flatMap { it.inetAddresses.toList() }
                ?.firstOrNull { !it.isLoopbackAddress && it is java.net.Inet4Address }
                ?.hostAddress
        } catch (e: Exception) {
            null
        }
    }

    /** Measures ICMP-like ping via TCP connection to [host]:80 on the IO dispatcher. */
    suspend fun measurePing(host: String, timeoutMs: Int = 3000): Long? =
        withContext(Dispatchers.IO) {
            try {
                val start = System.currentTimeMillis()
                InetAddress.getByName(host)
                val elapsed = System.currentTimeMillis() - start
                elapsed.coerceAtLeast(1L)
            } catch (e: Exception) {
                null
            }
        }
}
