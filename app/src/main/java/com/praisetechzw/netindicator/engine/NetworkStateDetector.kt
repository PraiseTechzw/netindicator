package com.praisetechzw.netindicator.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Model encapsulating the detected network state type
enum class NetworkState {
    WIFI,
    MOBILE_DATA,
    VPN,
    DISCONNECTED,
    UNKNOWN
}

/**
 * Robust listener relying on [ConnectivityManager.NetworkCallback] to instantly
 * detect when the Android device swaps between WiFi, Mobile, VPN, or loses connection completely.
 */
class NetworkStateDetector(private val context: Context) {

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStateFlow = MutableStateFlow(NetworkState.UNKNOWN)
    
    /**
     * Hot reactive state tracking what interface is active. 
     */
    val networkStateFlow: StateFlow<NetworkState> = _networkStateFlow.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            updateState(networkCapabilities)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            // Immediately label as disconnected when active capability resolves to null
            val activeCaps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            updateState(activeCaps)
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            val activeCaps = connectivityManager.getNetworkCapabilities(network)
            updateState(activeCaps)
        }
    }

    /**
     * Registers callbacks to be instantly notified when the user drops connection 
     * or swaps between cellular / Wi-Fi routers.
     */
    fun startDetection() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
        
        // Push initial state
        val initialCaps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        updateState(initialCaps)
    }

    /** Unregisters when no longer actively tracking. */
    fun stopDetection() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            // Already unregistered
        }
    }

    private fun updateState(caps: NetworkCapabilities?) {
        val newState = when {
            caps == null -> NetworkState.DISCONNECTED
            !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) && 
            !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkState.DISCONNECTED
            
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkState.VPN
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkState.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkState.MOBILE_DATA
            
            else -> NetworkState.UNKNOWN
        }
        _networkStateFlow.value = newState
    }
}
