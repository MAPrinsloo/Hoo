package vc.hoo

import GeocoderHelper.OnAddressFetchedListener
import GeocoderHelper.getAddressFromCoordinates
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Base64
import android.view.Gravity
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import vc.hoo.databinding.ActivityDocumentBinding
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

//used in requesting geolocation permission from user
private const val LOCATION_PERMISSION_REQUEST_CODE = 123

class DocumentActivity//Check if the user has logged in
    () : AppCompatActivity() {
    //binding
    lateinit var DocumentBinding: ActivityDocumentBinding
    //username
    private lateinit var Username: String
    //Capture geoPoint
    private lateinit var Coordinates: GeoPoint
    //temp drawable for camera image button
    private lateinit var tempIbtnDrawable: Drawable

    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Initialize username
        Username = intent.getStringExtra("username").toString()
        DocumentBinding = ActivityDocumentBinding.inflate(layoutInflater)
        val DocumentView = DocumentBinding.root
        //Holds the temporary drawable for IbtnDrawable - reset to this after capture
        tempIbtnDrawable = DocumentBinding.ibtnPhoto.drawable
        //clear inputs on load
        clearInputs()
        //Camera usage
        val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            //If photo is taken
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                var bitmap = it.data!!.extras?.get("data") as Bitmap
                DocumentBinding.ibtnPhoto.setImageBitmap(bitmap)
            }
        }
        //Ensures animation is played on back button
        overridePendingTransition(
            vc.hoo.R.anim.slide_in_right,
            vc.hoo.R.anim.slide_out_left
        )

        //----------------------------------------------------------------------------------------//
        //Nav Menu click
        DocumentBinding.mtDocument.setNavigationOnClickListener()
        {
            val slide = Slide()
            slide.slideEdge = Gravity.START
            TransitionManager.beginDelayedTransition(DocumentView, slide)
            DocumentBinding.flMenuSideSheet.isVisible = !DocumentBinding.flMenuSideSheet.isVisible
        }
        //----------------------------------------------------------------------------------------//
        //Account menu click
        DocumentBinding.mtDocument.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.account) {
                val slide = Slide()
                slide.slideEdge = Gravity.END
                TransitionManager.beginDelayedTransition(DocumentView, slide)
                DocumentBinding.flAccountSideSheet.isVisible =
                    !DocumentBinding.flAccountSideSheet.isVisible
                true
            } else {
                false
            }
        }
        //----------------------------------------------------------------------------------------//
        //ibtnPhoto/Camera click
        DocumentBinding.ibtnPhoto.setOnClickListener()
        {
            try {
                var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                getResult.launch(intent)
            } catch (e: java.lang.IllegalArgumentException) {
                Toast.makeText(
                    this,
                    "Error Occurred, Ensure all values are entered correctly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        //----------------------------------------------------------------------------------------//
        //Capture button clicked
        DocumentBinding.btnCapture.setOnClickListener()
        {
            var birdName = DocumentBinding.tietBirdName.text.toString()
            var numBirds :Int
            try
            {
                numBirds = DocumentBinding.tietNumBirds.text.toString().toInt()
            }
            catch (e: java.lang.NumberFormatException)
            {
                numBirds = 0
            }
            if (ValidateInputs(birdName, numBirds, DocumentBinding) == true) {
                getAddress { locationAddress ->
                    save(
                        locationAddress,
                        numBirds.toString(),
                        birdName,
                        DocumentBinding.ibtnPhoto.drawable.toBitmap(),
                        this.Coordinates
                    )
                    //Clear inputs
                    clearInputs()
                }
            }
        }
        //Showing the page
        setContentView(DocumentView)
    }
    //----------------------------------------------------------------------------------------//
    //clears on the user inputs in the activity
    private fun clearInputs()
    {
        DocumentBinding.ibtnPhoto.setImageDrawable(tempIbtnDrawable)
        DocumentBinding.tietBirdName.text = null
        DocumentBinding.tietNumBirds.text = null
    }
    //----------------------------------------------------------------------------------------//
    //get the users current address
    private fun getAddress(callback: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //gain user perms
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        this.Coordinates = GeoPoint(latitude,longitude)
                        getAddressFromCoordinates(
                            applicationContext,
                            latitude,
                            longitude,
                            object : OnAddressFetchedListener {
                                override fun onAddressFetched(address: String?) {
                                    callback(address ?: "")
                                }
                            }
                        )
                    }
                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    //----------------------------------------------------------------------------------------//
    //Saving the data entered into the shared preferences.
    private fun save(
        location: String,
        num_birds: String,
        bird_name: String,
        picture: Bitmap,
        coordinates: GeoPoint
    ) {
        //Convert bitmap to Base64 encoded string.
        val baos = ByteArrayOutputStream()
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        val encodedImage = Base64.encodeToString(b, Base64.DEFAULT)
        val time: String =
            "${LocalDateTime.now().dayOfMonth} ${LocalDateTime.now().month} ${LocalDateTime.now().hour}:${LocalDateTime.now().minute}"
        //Data to save.
        val timesheetData = hashMapOf(
            "time" to time,
            "location" to location,
            "num_birds" to num_birds,
            "bird_name" to bird_name,
            "picture" to encodedImage,
            "coordinates" to coordinates
        )
        var birdData = hashMapOf(
            "bird_name" to bird_name,
            "num_birds" to num_birds.toLong()
        )
        saveBirdData(bird_name, num_birds.toLong(), birdData)

        saveHistoryData(timesheetData)
    }
    //----------------------------------------------------------------------------------------//
    //Saving Bird data
    private fun saveBirdData(bird_name: String, num_birds: Long, birdData: Any)
    {
        val db = FirebaseFirestore.getInstance()
        val birdCollection = db.collection("/$Username/user_details/birds/")
        val birdName = birdCollection.document(bird_name)
        birdName.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Bird document already exists, update it
                val currentNumBirds = documentSnapshot.getLong("num_birds") ?: 0
                val updatedNumBirds = currentNumBirds + num_birds
                val updatedBirdData = hashMapOf(
                    "bird_name" to bird_name,
                    "num_birds" to updatedNumBirds
                )
                birdName.set(updatedBirdData)
            } else {
                // Bird document doesn't exist, create it
                birdName.set(birdData)
            }
            updateStats(num_birds)
        }
    }
    //----------------------------------------------------------------------------------------//
    //Saving History data
    private fun saveHistoryData(timesheetData: Any)
    {
        val db = FirebaseFirestore.getInstance()
        val timesheetCollection = db.collection("/$Username/user_details/history/")
        timesheetCollection.add(timesheetData)
            .addOnSuccessListener { documentReference ->
                val documentId = documentReference.id
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    //----------------------------------------------------------------------------------------//
    //collects the required db data used to update stats
    private fun updateStats(num_birds: Long)
    {
        val db = FirebaseFirestore.getInstance()
        var arrDbEntries = arrayListOf<String>()
        var least_spotted = ""
        var least_spotted_num: Long = 999
        var most_spotted = ""
        var most_spotted_num: Long = 0

        //Firebase directory
        val birdCollection = db.collection("/$Username/user_details/birds/")


        birdCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                arrDbEntries.add(document.id)
            }
            var count = 0
            for (entryID in arrDbEntries) {
                val entryIdRef = birdCollection.document(entryID)

                entryIdRef.get().addOnSuccessListener { documentSnapshot ->
                    val entry = documentSnapshot.data

                    if (entry != null) {
                        //documented num birds
                        val currentNumBirds = documentSnapshot.getLong("num_birds") ?: 0
                        val currentBirdName = documentSnapshot.getString("bird_name") ?: ""

                        //update most_spotted
                        if (currentNumBirds > most_spotted_num)
                        {
                            most_spotted = currentBirdName as String
                            most_spotted_num = currentNumBirds
                        }
                        //update least_spotted
                        if (currentNumBirds < least_spotted_num)
                        {
                            least_spotted = currentBirdName as String
                            least_spotted_num = currentNumBirds
                        }
                        //when the data is finished being collected
                        if (++count == arrDbEntries.count())
                        {
                            onDataCollectonFinish(most_spotted,most_spotted_num,least_spotted,least_spotted_num,num_birds)
                        }
                    }
                }
            }
        }
    }
    //----------------------------------------------------------------------------------------//
    //Call when the data is finished collecting for update stats
    //updates the users stats in the database
    private fun onDataCollectonFinish(most_spotted: String, most_spotted_num: Long,least_spotted: String,least_spotted_num: Long, num_birds: Long)
    {
        val db = FirebaseFirestore.getInstance()
        val userDetailsCollection = db.collection(this.Username)
        val userDetailsDoc = userDetailsCollection.document("user_details")
        var total_spotted:Long = 0
        userDetailsDoc.get().addOnSuccessListener { querySnapshot ->
            total_spotted = querySnapshot.getLong("total_spotted") ?: 0
            if (querySnapshot.exists()) {
                val updatedUserDetails = hashMapOf(
                    "least_spotted" to least_spotted,
                    "least_spotted_num" to least_spotted_num,
                    "most_spotted" to most_spotted,
                    "most_spotted_num" to most_spotted_num,
                    "total_spotted" to (total_spotted + num_birds)
                )
                userDetailsDoc.update(updatedUserDetails as Map<String, Any>)
            }
        }
    }
    //----------------------------------------------------------------------------------------//
    //Validate all user input
    private fun ValidateInputs(
        birdName: String,
        numBirds: Int,
        binding: ActivityDocumentBinding
    ): Boolean {
        var inputsAreValid = true


        binding.tilBirdName.error = "";
        binding.tilNumBirds.error = "";
        if ((birdName.trim().isEmpty()) == true) {
            binding.tilBirdName.error = "Must enter a bird name"
            inputsAreValid = false
        }
        if ((numBirds <= 0) == true) {
            binding.tilNumBirds.error = "Field cannot be empty"
            inputsAreValid = false
        }
        return inputsAreValid
    }
    //----------------------------------------------------------------------------------------//
    //Runs animation on new intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Username = intent.getStringExtra("username").toString()
        clearInputs()
        DocumentBinding.flAccountSideSheet.isVisible = false
        DocumentBinding.flMenuSideSheet.isVisible = false
        // Set custom enter animation when the activity is relaunched
        overridePendingTransition(
            vc.hoo.R.anim.slide_in_right,
            vc.hoo.R.anim.slide_out_left
        )
    }
    //----------------------------------------------------------------------------------------//
    //Disable Backpressing
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
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