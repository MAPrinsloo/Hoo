package vc.hoo

import GeocoderHelper
import GeocoderHelper.getAddressFromCoordinates
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.*
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import org.json.JSONArray
import org.json.JSONException
import vc.hoo.databinding.ActivityNavigateBinding
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.concurrent.thread


var mapView: com.mapbox.maps.MapView? = null
private const val LOCATION_PERMISSION_REQUEST_CODE = 123

class NavigateActivity : AppCompatActivity() {
    private lateinit var Username: String
    private lateinit var db: FirebaseFirestore

    //here are variables taken from:
    //https://docs.mapbox.com/android/navigation/examples/render-route-line/
    //will stipulate start and end of ref
    //--START--//
    private lateinit var locationComponent: LocationComponentPlugin
    private val mapboxReplayer = MapboxReplayer()
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)
    private lateinit var codedRoute: DirectionsRoute
    private val viewBinding: ActivityNavigateBinding by lazy {
        ActivityNavigateBinding.inflate(layoutInflater)
    }
    private val navigationLocationProvider by lazy {
        NavigationLocationProvider()
    }
    private val routeLineResources: RouteLineResources by lazy {
        RouteLineResources.Builder()
            .routeLineColorResources(RouteLineColorResources.Builder().build())
            .build()
    }
    private val options: MapboxRouteLineOptions by lazy {
        MapboxRouteLineOptions.Builder(this)
            .withVanishingRouteLineEnabled(true)
            .withRouteLineResources(routeLineResources)
            .withRouteLineBelowLayerId("road-label-navigation")
            .build()
    }
    private val routeLineView by lazy {
        MapboxRouteLineView(options)
    }
    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(options)
    }
    private val routeArrowApi: MapboxRouteArrowApi by lazy {
        MapboxRouteArrowApi()
    }
    private val routeArrowOptions by lazy {
        RouteArrowOptions.Builder(this)
            .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID)
            .build()
    }
    private val routeArrowView: MapboxRouteArrowView by lazy {
        MapboxRouteArrowView(routeArrowOptions)
    }
    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
        routeLineApi.setNavigationRoutes(
            routeUpdateResult.navigationRoutes
        ) { value ->
            mapView?.getMapboxMap()?.getStyle()?.apply {
                routeLineView.renderRouteDrawData(this, value)
            }
        }
    }
    private val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        val result = routeLineApi.updateTraveledRouteLine(point)
        mapView?.getMapboxMap()?.getStyle()?.apply {
            routeLineView.renderRouteLineUpdate(this, result)
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        routeLineApi.updateWithRouteProgress(routeProgress) { result ->
            mapView?.getMapboxMap()?.getStyle()?.apply {
                routeLineView.renderRouteLineUpdate(this, result)
            }
        }
        val arrowUpdate = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
        mapView?.getMapboxMap()?.getStyle()?.apply {
            routeArrowView.renderManeuverUpdate(this, arrowUpdate)
        }
    }
    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {}
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
        }
    }
    private val mapboxNavigation1: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
            }
        },
        onInitialize = this::initNavigation
    )
    //--END--//

    //Binding
    lateinit var NavigateBinding: ActivityNavigateBinding
    //Holds the lng and lat for the hotspots
    var BirdNavPointList = mutableListOf<BirdNavPoints>()
    //holds allPoints - user location and hotspots
    var AllPoints = mutableListOf<PointAnnotationManager>()
    //initialise mapboxNavigation
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onInitialize = this::initNavigation
    )
    //Origin location with temp points
    private var originLocation: Location = Location("userLocation").apply {
        longitude = -122.4192
        latitude = 37.7627
        bearing = 10f
    }
    //Destination location with temp points
    private var destination = Point.fromLngLat(-122.4106, 37.7676)

    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NavigateBinding = ActivityNavigateBinding.inflate(layoutInflater)
        val NavigateView = NavigateBinding.root
        setContentView(NavigateView)

        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(this)
                    .accessToken(BuildConfig.MAPBOX_DOWNLOADS_TOKEN)
                    .build()
            }
        }
        //initialise username
        Username = intent.getStringExtra("username").toString()
        //initialise db
        db = Firebase.firestore
        //load the max distance pref from db.
        LoadMaxDistance()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        mapView = findViewById(R.id.mvMap)
        //load current location on map startup
        mapView?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS,
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    LoadCurrentLocation(NavigateBinding)
                }
            }
        )
        //----------------------------------------------------------------------------------------//
        //Nav Menu click
        NavigateBinding.mtNavigate.setNavigationOnClickListener {
            val slide = Slide()
            slide.slideEdge = Gravity.START
            TransitionManager.beginDelayedTransition(NavigateView, slide)
            NavigateBinding.flMenuSideSheet.isVisible = !NavigateBinding.flMenuSideSheet.isVisible
        }
        //--------------------------------------------------------------------------------------------//
        //Account menu click
        NavigateBinding.mtNavigate.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.account) {
                val slide = Slide()
                slide.slideEdge = Gravity.END
                TransitionManager.beginDelayedTransition(NavigateView, slide)
                NavigateBinding.flAccountSideSheet.isVisible =
                    !NavigateBinding.flAccountSideSheet.isVisible
                true
            } else {
                false
            }
        }
        //--------------------------------------------------------------------------------------------//
        //
        NavigateBinding.msMapView.setOnClickListener()
        {
            LoadCurrentLocation(NavigateBinding);
        }
        //--------------------------------------------------------------------------------------------//
        //ibtnCenterOnUser click
        NavigateBinding.ibtnCenterOnUser.setOnClickListener()
        {
            LoadCurrentLocation(NavigateBinding)
        }
        //--------------------------------------------------------------------------------------------//
        //map click
        //Checks if the position clicked has a marker within a tolerance
        mapView?.getMapboxMap()?.addOnMapClickListener { point ->
            for (marker in BirdNavPointList) {
                //https://discuss.kotlinlang.org/t/how-do-you-round-a-number-to-n-decimal-places/8843/3
                val mLng: Double =
                    BigDecimal(marker.Lng).setScale(3, RoundingMode.HALF_UP).toDouble()
                val mLat: Double =
                    BigDecimal(marker.Lat).setScale(3, RoundingMode.HALF_UP).toDouble()
                val pLng: Double =
                    BigDecimal(point.longitude()).setScale(3, RoundingMode.HALF_UP).toDouble()
                val pLat: Double =
                    BigDecimal(point.latitude()).setScale(3, RoundingMode.HALF_UP).toDouble()
                val markerPoint = Point.fromLngLat(marker.Lng, marker.Lat)
                //the tolerance regarding user precision
                val tolerance = 0.001

                //if you minus the two values it must be less than the tolerance
                if (Math.abs(mLat - pLat) < tolerance &&
                    Math.abs(mLng - pLng) < tolerance
                ) {
                    //Set the destination to the precise marker location
                    destination = Point.fromLngLat(marker.Lng, marker.Lat)
                    //Fetch route to destination
                    fetchARoute()
                    return@addOnMapClickListener true
                }
            }
            false // Continue with default map click handling if no marker was clicked
        }
    }
    //----------------------------------------------------------------------------------------//
    //Initialise navigation
    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .locationEngine(replayLocationEngine)
                .build()
        )

        locationComponent = mapView?.location?.apply {
            setLocationProvider(navigationLocationProvider)
            addOnIndicatorPositionChangedListener(onPositionChangedListener)
            enabled = true
        }!!

        replayOriginLocation()
    }
    //----------------------------------------------------------------------------------------//
    //https://docs.mapbox.com/android/navigation/examples/render-route-line/
    private fun replayOriginLocation() {
        mapboxReplayer.pushEvents(
            listOf(
                ReplayRouteMapper.mapToUpdateLocation(
                    Date().time.toDouble(),
                    Point.fromLngLat(-122.4192, 37.7627)
                )
            )
        )
        mapboxReplayer.playFirstLocation()
        mapboxReplayer.playbackSpeed(3.0)
    }
    //----------------------------------------------------------------------------------------//
    //https://docs.mapbox.com/android/navigation/examples/render-route-line/
    override fun onDestroy() {
        super.onDestroy()
        mapboxReplayer.finish()
        routeLineView.cancel()
        routeLineApi.cancel()
        locationComponent.removeOnIndicatorPositionChangedListener(onPositionChangedListener)
    }
    //--------------------------------------------------------------------------------------------//
    //https://docs.mapbox.com/android/navigation/guides/turn-by-turn-navigation/route-generation/
    //Fetches a json route from origin to location
    private fun fetchARoute() {
        val originPoint = Point.fromLngLat(
            originLocation.longitude,
            originLocation.latitude
        )

        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this)
            .coordinatesList(listOf(originPoint, destination))
            .alternatives(false)
            .bearingsList(
                listOf(
                    Bearing.builder()
                        .angle(originLocation.bearing.toDouble())
                        .degrees(45.0)
                        .build(),
                    null
                )
            )
            .build()
        mapboxNavigation.requestRoutes(
            routeOptions,
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    //GSON instance used only to print the response prettily
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val json = routes.map {
                        gson.toJson(
                            JsonParser.parseString(it.directionsRoute.toJson())
                        )
                    }
                    //assign json to route
                    codedRoute = DirectionsRoute.fromJson("""${json[0]}""")
                    //initialise navigation
                    initNavigation()
                    //set the route
                    mapboxNavigation.setNavigationRoutes(
                        listOf(codedRoute).toNavigationRoutes(RouterOrigin.Offboard)
                    )
                    //hide alternative routes
                    NavigateBinding.mvMap.getMapboxMap().getStyle()?.apply {
                        routeLineView.hideAlternativeRoutes(this)
                    }
                }
            }
        )
    }
    //--------------------------------------------------------------------------------------------//
    //Adds a marker to the map using lng and lat
    private fun addAnnotationToMap(lng: Double, lat: Double, markerDrawable: Int) {
        bitmapFromDrawableRes(this@NavigateActivity, markerDrawable)?.let {
            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(lng, lat))
                .withIconImage(it)
            pointAnnotationManager?.create(pointAnnotationOptions)
            if (pointAnnotationManager != null) {
                AllPoints.add(pointAnnotationManager)
            }
        }
    }
    //--------------------------------------------------------------------------------------------//
    //https://docs.mapbox.com/android/maps/examples/default-point-annotation/
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))
    //--------------------------------------------------------------------------------------------//
    //https://docs.mapbox.com/android/maps/examples/default-point-annotation/
    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }
    //--------------------------------------------------------------------------------------------//
    //
    private fun clearAllMarkers() {
        for (marker in AllPoints) {
            marker.deleteAll()
        }
    }
    //--------------------------------------------------------------------------------------------//
    //Clears and checks for new bird hotspots and adds them to the map
    fun consumeJson(birdReportJSON: String?) {
        BirdNavPointList.clear()
        if (birdReportJSON != null) {
            try {
                val birdReportArray = JSONArray(birdReportJSON)

                for (i in 0 until birdReportArray.length()) {
                    val birdNavObject = BirdNavPoints()
                    val birdReport = birdReportArray.getJSONObject(i)

                    val latTemp = birdReport.optDouble("lat", 0.0)
                    val lngTemp = birdReport.optDouble("lng", 0.0)

                    birdNavObject.Lat = latTemp
                    birdNavObject.Lng = lngTemp

                    BirdNavPointList.add(birdNavObject)
                    addAnnotationToMap(lngTemp, latTemp, R.drawable.red_marker)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
    //--------------------------------------------------------------------------------------------//
    //
    fun AddObservationMarkers() {
        BirdNavPointList.clear()
        var arrDbEntries = arrayListOf<String>()

        //Firebase directory
        val historyCollection = db.collection("/$Username/user_details/history/")
        historyCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                arrDbEntries.add(document.id)
            }
            for (entryID in arrDbEntries) {
                val entryIdRef = historyCollection.document(entryID)

                entryIdRef.get().addOnSuccessListener { documentSnapshot ->
                    val entry = documentSnapshot.data

                    if (entry != null) {
                        val birdNavObject = BirdNavPoints()
                        val birdGeoPoint = (entry["coordinates"] as? GeoPoint ?: GeoPoint(0.0,0.0))
                        var tempGeoPoint: GeoPoint = birdGeoPoint
                        birdNavObject.Lat = tempGeoPoint.latitude
                        birdNavObject.Lng = tempGeoPoint.longitude

                        BirdNavPointList.add(birdNavObject)
                        addAnnotationToMap(tempGeoPoint.longitude, tempGeoPoint.latitude, R.drawable.bird_map_icon)
                    }
                }
            }
        }
    }
    //--------------------------------------------------------------------------------------------//
    //Loads the users current location onto the map
    private fun LoadCurrentLocation(binding: ActivityNavigateBinding) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        clearAllMarkers()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        var latitude = location.latitude
                        var longitude = location.longitude

                        val sharedPref = getSharedPreferences("username", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("lat", latitude.toString())
                        editor.apply()
                        editor.putString("lng", longitude.toString())
                        editor.apply()

                        getAddressFromCoordinates(
                            applicationContext,
                            latitude,
                            longitude,
                            object : GeocoderHelper.OnAddressFetchedListener {
                                override fun onAddressFetched(address: String?) {
                                    val locationAddress = address ?: ""
                                    val cameraLocation = CameraOptions.Builder()
                                        .center(Point.fromLngLat(longitude, latitude))
                                        .pitch(45.0)
                                        .zoom(15.5)
                                        .bearing(-17.6)
                                        .build()

                                    //Set camera position
                                    Toast.makeText(
                                        this@NavigateActivity,
                                        locationAddress,
                                        Toast.LENGTH_LONG
                                    ).show()

                                    mapView?.getMapboxMap()?.setCamera(cameraLocation)
                                    thread {
                                        val hotspot = try {
                                            //fetch from network util to build the URL
                                            val netUtil: NetworkUtil =
                                                NetworkUtil(applicationContext)
                                            netUtil.buildURLForEbird()?.readText()
                                        } catch (e: Exception) {
                                            return@thread
                                        }
                                        runOnUiThread { if (NavigateBinding.msMapView.isChecked){AddObservationMarkers()}else{ consumeJson(hotspot)} }
                                    }
                                    //add user location to map
                                    addAnnotationToMap(location.longitude, location.latitude, R.drawable.user_map_icon)
                                    //user location is also the origin location
                                    originLocation.longitude = location.longitude
                                    originLocation.latitude = location.latitude

                                }
                            }
                        )
                    }
                }

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    //--------------------------------------------------------------------------------------------//
    //checks the firebase db for the max distance and applies that as a radius for hotspots
    private fun LoadMaxDistance() {
        val SettingsCollection = db.collection("/$Username/")
        SettingsCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                val maxDistance = document["max_distance"] as? Long ?: 2.0

                val sharedPref = getSharedPreferences("username", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("maxDistance", maxDistance.toInt().toString())
                editor.apply()
            }
        }
    }
    //--------------------------------------------------------------------------------------------//
    //Runs animation on new intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
*/