package vc.hoo

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.MalformedURLException
import java.net.URL

class NetworkUtil {

    //Max distance used for filtering radius of url request
    private var MaxDistance: String = "2"
    //Ebird url to request
    private lateinit var EBIRD_URL: String
    //Firebase db
    lateinit private var db: FirebaseFirestore
    //Username of current user
    lateinit var Username: String
    //Latitude point of which to base radius off of
    lateinit var Lat: String
    //Longitude point of which to base radius off of
    lateinit var Lng: String

    private lateinit var sharedPreferences: SharedPreferences


    private val PARAM_API_KEY = "key"
    private val LOGGING_TAG = "URLWECREATED"
    //----------------------------------------------------------------------------------------//
    //Constructor to pass context for sharepreferences
    constructor(context: Context) {
        sharedPreferences = context.getSharedPreferences("username", Context.MODE_PRIVATE)
    }

    fun buildURLForEbird(): URL? {
        db = Firebase.firestore
        Username = sharedPreferences.getString("username", "").toString()
        Lat = sharedPreferences.getString("lat", "").toString()
        Lng = sharedPreferences.getString("lng", "").toString()
        //get max distance from shared preference
        MaxDistance = sharedPreferences.getString("maxDistance", "2").toString()
        //pass values into url
        //dist is radius around location for ebird hotspots
        EBIRD_URL =
            "https://api.ebird.org/v2/ref/hotspot/geo?lat=$Lat&lng=$Lng&dist=$MaxDistance&fmt=json"

        val buildUri: Uri = Uri.parse(EBIRD_URL).buildUpon()
            .appendQueryParameter(
                PARAM_API_KEY,
                BuildConfig.EBIRD_API_KEY
            ) // passing in api key
            .build()
        var url: URL? = null
        try {
            url = URL(buildUri.toString())
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        Log.i(LOGGING_TAG, "buildURLForHotspot: $url")
        return url
    }
}
/*
References

Slide Animation
https://www.geeksforgeeks.org/how-to-add-slide-animation-between-activities-in-android/

Adding icons to list views - list adapter
https://medium.com/@hasperong/custom-listview-using-kotlin-7ceb1caaf3cf

Callback functionality
https://www.baeldung.com/kotlin/callback-functions

Regex
https://www.baeldung.com/kotlin/regular-expressions

Mapbox routing and markers
//https://docs.mapbox.com/android/navigation/examples/render-route-line/
https://docs.mapbox.com/mapbox-gl-js/api/markers/#geolocatecontrol
https://docs.mapbox.com/android/navigation/guides/migrate-to-v2/#core-components-in-v2
https://docs.mapbox.com/android/navigation/guides/turn-by-turn-navigation/route-generation/
https://docs.mapbox.com/android/navigation/guides/get-started/initialization/
https://docs.mapbox.com/android/navigation/guides/get-started/initialization/#create-the-mapboxnavigation-object
https://docs.mapbox.com/android/navigation/guides/ui-components/route-line/
https://docs.mapbox.com/android/navigation/guides/get-started/install/
https://docs.mapbox.com/android/navigation/guides/migrate-to-v2/#navigationmaproute-was-replaced

Firebase Auth
https://medium.com/swlh/firebase-authentication-with-kotlin-46da70bf8a4d

Locaton to geopoint
https://stackoverflow.com/questions/11711147/convert-location-to-geopoint

Disable back button
https://stackoverflow.com/questions/50720273/how-to-disable-back-home-multitask-physical-buttons
*/