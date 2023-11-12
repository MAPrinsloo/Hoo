package vc.hoo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Base64
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import vc.hoo.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {
    //firebase db
    lateinit private var db: FirebaseFirestore
    //binding
    lateinit var HistoryBinding: ActivityHistoryBinding
    //Array lists for displaying data in listview
    var bird_name = arrayListOf<String>()
    var location = arrayListOf<String>()
    var num_birds = arrayListOf<String>()
    var picture = arrayListOf<Bitmap>()
    var time = arrayListOf<String>()
    //Formatted List view of merged arraylists. num_birds + time + location
    var description = arrayListOf<String>()
    //username
    lateinit var Username: String
    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initialize username
        Username = intent.getStringExtra("username").toString()
        //initialize db
        db = Firebase.firestore
        //binding
        HistoryBinding = ActivityHistoryBinding.inflate(layoutInflater)
        val HistoryView = HistoryBinding.root

        populateDisplayArrays()
        //populate adatper
        val displayAdapter =
            HistListAdapter(this, title = bird_name, description = description, imgid = picture)
        HistoryBinding.lvHistory.adapter = displayAdapter
        overridePendingTransition(
            vc.hoo.R.anim.slide_in_right,
            vc.hoo.R.anim.slide_out_left
        )
        //----------------------------------------------------------------------------------------//
        //nav menu click
        HistoryBinding.mtHistory.setNavigationOnClickListener()
        {
            val slide = Slide()
            slide.slideEdge = Gravity.START
            TransitionManager.beginDelayedTransition(HistoryView, slide)
            HistoryBinding.flMenuSideSheet.isVisible = !HistoryBinding.flMenuSideSheet.isVisible
        }
        //----------------------------------------------------------------------------------------//
        //account menu click
        HistoryBinding.mtHistory.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.account) {
                val slide = Slide()
                slide.slideEdge = Gravity.END
                TransitionManager.beginDelayedTransition(HistoryView, slide)
                HistoryBinding.flAccountSideSheet.isVisible =
                    !HistoryBinding.flAccountSideSheet.isVisible
                true
            } else {
                false
            }
        }
        setContentView(HistoryView)
    }

    //----------------------------------------------------------------------------------------//
    //clear all data retrieved
    private fun clearAllData() {
        bird_name.clear()
        location.clear()
        num_birds.clear()
        picture.clear()
        time.clear()
    }

    //----------------------------------------------------------------------------------------//
    //Retrieves data from db and populates the corresponding arrays
    private fun populateDisplayArrays() {
        clearAllData()
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
                        val birdName = entry["bird_name"] as? String ?: ""
                        bird_name.add(birdName)

                        val birdLocation = entry["location"] as? String ?: ""
                        location.add(birdLocation)

                        val numBirds = entry["num_birds"] as? String ?: ""
                        num_birds.add(numBirds)

                        val encodedPicture = (entry["picture"] as? String ?: "")
                        picture.add(decodePicture(encodedPicture))

                        val birdTime = (entry["time"] as? String ?: "")
                        time.add(birdTime)

                        description.add(
                            "Number of birds: $numBirds \r\n" +
                                    "Time: $birdTime \r\n" +
                                    "Location: $birdLocation"
                        )
                        HistoryBinding.lvHistory.isVisible = false
                        HistoryBinding.lvHistory.isVisible = true

                    }
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------//
    //Decodes the base64 string into a bitmap
    private fun decodePicture(encodedPicture: String): Bitmap {
        val decodedBytes = Base64.decode(encodedPicture, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        return bitmap
    }


    //----------------------------------------------------------------------------------------//
    //Runs animation on new intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        HistoryBinding.flAccountSideSheet.isVisible = false
        HistoryBinding.flMenuSideSheet.isVisible = false
        // Set custom enter animation when the activity is relaunched
        overridePendingTransition(
            vc.hoo.R.anim.slide_in_right,
            vc.hoo.R.anim.slide_out_left
        )
    }

    //----------------------------------------------------------------------------------------//
    //Disable Backpressing
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {}


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