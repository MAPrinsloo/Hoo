import android.content.Context
import android.location.Geocoder
import java.io.IOException
import java.util.*

object GeocoderHelper {
    fun getAddressFromCoordinates(
        context: Context?,
        latitude: Double,
        longitude: Double,
        listener: OnAddressFetchedListener
    ) {
        val geocoder = Geocoder(context!!, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.size > 0) {
                val address = addresses[0]
                val addressString = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    addressString.append(address.getAddressLine(i))
                    if (i < address.maxAddressLineIndex) {
                        addressString.append(", ")
                    }
                }
                listener.onAddressFetched(addressString.toString())
            } else {
                listener.onAddressFetched("No address found")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            listener.onAddressFetched("Error fetching address")
        }
    }

    interface OnAddressFetchedListener {
        fun onAddressFetched(address: String?)
    }
}