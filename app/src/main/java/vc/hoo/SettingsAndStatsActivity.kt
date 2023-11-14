package vc.hoo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import vc.hoo.databinding.ActivitySettingsAndStatsBinding

class SettingsAndStatsActivity : AppCompatActivity() {
    lateinit var SettStatsBinding: ActivitySettingsAndStatsBinding
    lateinit private var db: FirebaseFirestore
    //Logged in users username
    lateinit var Username: String
    //If the measuring distance will be in KM or Miles
    var Metric: Boolean = true
    //Max distance the user is willing to travel
    var MaxDistance: Int = 0

    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Username = intent.getStringExtra("username").toString()

        SettStatsBinding = ActivitySettingsAndStatsBinding.inflate(layoutInflater)
        val SettStatsView = SettStatsBinding.root
        setContentView(SettStatsView)

        //Initialize db
        db = Firebase.firestore
        //Load the setttings
        loadSettings()

        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        //----------------------------------------------------------------------------------------//
        //Metric checkbox click
        SettStatsBinding.cbMetric.setOnClickListener()
        {
            Metric = true
            SettStatsBinding.cbMetric.isChecked = true
            SettStatsBinding.cbImperial.isChecked = false
            SettStatsBinding.tilMaxDistance.suffixText = "Km"
        }
        //----------------------------------------------------------------------------------------//
        //Imperial checkbox click
        SettStatsBinding.cbImperial.setOnClickListener()
        {
            Metric = false
            SettStatsBinding.cbImperial.isChecked = true
            SettStatsBinding.cbMetric.isChecked = false
            SettStatsBinding.tilMaxDistance.suffixText = "Miles"
        }
        //----------------------------------------------------------------------------------------//
        //Nav menu click
        SettStatsBinding.mtSettingsAndStats.setNavigationOnClickListener()
        {
            val slide = Slide()
            slide.slideEdge = Gravity.START
            TransitionManager.beginDelayedTransition(SettStatsView, slide)
            SettStatsBinding.flMenuSideSheet.isVisible = !SettStatsBinding.flMenuSideSheet.isVisible
        }
        //----------------------------------------------------------------------------------------//
        //Account menu click
        SettStatsBinding.mtSettingsAndStats.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.account) {
                val slide = Slide()
                slide.slideEdge = Gravity.END
                TransitionManager.beginDelayedTransition(SettStatsView, slide)
                SettStatsBinding.flAccountSideSheet.isVisible =
                    !SettStatsBinding.flAccountSideSheet.isVisible
                true
            } else {
                false
            }
        }
        //----------------------------------------------------------------------------------------//
        //Save button clicked
        SettStatsBinding.btnSaveSettings.setOnClickListener()
        {
            saveSettings()
        }
    }
    //----------------------------------------------------------------------------------------//
    //Loads the settings from the db onto the activity
    //Retrieves info from db to populate the stats of the activity
    private fun loadSettings() {
        val SettingsCollection = db.collection("/$Username/")
        var leastSpotted = ""
        var leastSpottedAmount = ""
        var mostSpotted = ""
        var mostSpottedAmount = ""
        var totalSpotted = ""
        SettingsCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                Metric = document["metric"] as? Boolean ?: true
                val tempDistance = document["max_distance"] as? Long ?: 2.0
                leastSpotted = document["least_spotted"] as? String?: ""
                leastSpottedAmount = (document["least_spotted_num"] as? Long?: 0).toString()
                mostSpotted = document["most_spotted"] as? String?: ""
                mostSpottedAmount = (document["most_spotted_num"] as? Long?: 0).toString()
                totalSpotted = (document["total_spotted"] as? Long?: 0).toString()

                MaxDistance = tempDistance.toInt()
            }
            if (Metric == false) {
                SettStatsBinding.tilMaxDistance.suffixText = "Miles"
            }
            //Set activity inputs to the stored settings
            SettStatsBinding.tietMaxDistance.setText(MaxDistance.toString())
            SettStatsBinding.cbMetric.isChecked = Metric
            SettStatsBinding.cbImperial.isChecked = !Metric
            //load the stats onto the activity
            loadStatistics(leastSpotted,leastSpottedAmount,mostSpotted,mostSpottedAmount,totalSpotted)
        }
    }
    //----------------------------------------------------------------------------------------//
    //Load the stats onto the activity
    private fun loadStatistics(leastSpotted:String, leastSpottedAmount: String, mostSpotted:String, mostSpottedAmount: String, totalSpotted:String){
        SettStatsBinding.tvLeastSpotted.text = resources.getString(R.string.LeastSpottedBird) + " " + leastSpotted
        SettStatsBinding.tvLeastSpottedAmount.text = resources.getString(R.string.Amount) + " " + leastSpottedAmount

        SettStatsBinding.tvMostSpotted.text = resources.getString(R.string.MostSpottedBird) + " " + mostSpotted
        SettStatsBinding.tvMostSpottedAmount.text = resources.getString(R.string.Amount) + " " + mostSpottedAmount

        SettStatsBinding.tvTotalBirds.text = resources.getString(R.string.TotalBirdsSpotted) + " " + totalSpotted
    }
    //----------------------------------------------------------------------------------------//
    //Save settings to the db
    private fun saveSettings() {
        val settingsDocRef = db.document("/$Username/user_details")

        val newMetric = Metric
        var newMaxDistance = SettStatsBinding.tietMaxDistance.text.toString().toInt()

        val updates = hashMapOf(
            "metric" to newMetric,
            "max_distance" to newMaxDistance
        )

        settingsDocRef.update(updates as Map<String, Int>)
            .addOnSuccessListener {
                Metric = newMetric
                MaxDistance = newMaxDistance
            }
        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
    }
    //----------------------------------------------------------------------------------------//
    //Runs animation on new intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        //loads the new settings on reopen
        loadSettings()
        SettStatsBinding.flAccountSideSheet.isVisible = false
        SettStatsBinding.flMenuSideSheet.isVisible = false
        // Set custom enter animation when the activity is relaunched
        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
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

Firebase Auth
https://medium.com/swlh/firebase-authentication-with-kotlin-46da70bf8a4d

Locaton to geopoint
https://stackoverflow.com/questions/11711147/convert-location-to-geopoint

Disable back button
https://stackoverflow.com/questions/50720273/how-to-disable-back-home-multitask-physical-buttons
*/