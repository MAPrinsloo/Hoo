package vc.hoo

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
    lateinit var Username: String
    var Metric: Boolean = true
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
        //
        SettStatsBinding.btnSaveSettings.setOnClickListener()
        {
            saveSettings()
        }
    }
    //----------------------------------------------------------------------------------------//
    //Loads the settings from the db
    private fun loadSettings() {
        val SettingsCollection = db.collection("/$Username/")
        SettingsCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                Metric = document["metric"] as? Boolean ?: true
                val tempDistance = document["max_distance"] as? Long ?: 2.0
                MaxDistance = tempDistance.toInt()
            }
            if (Metric == false) {
                //if user wants to use imperial, convert db from km to miles for display in text box
                MaxDistance = (MaxDistance * 0.612371).toInt()
                SettStatsBinding.tilMaxDistance.suffixText = "Miles"
            }

            SettStatsBinding.tietMaxDistance.setText(MaxDistance.toString())
            SettStatsBinding.cbMetric.isChecked = Metric
            SettStatsBinding.cbImperial.isChecked = !Metric
        }
    }
    //----------------------------------------------------------------------------------------//
    //Save settings to the db
    private fun saveSettings() {
        val settingsDocRef = db.document("/$Username/user_details")

        val newMetric = Metric
        var newMaxDistance = SettStatsBinding.tietMaxDistance.text.toString().toInt()

        if (Metric == false) {
            //if user want to save imperial, convert to km using 1.60934
            newMaxDistance = (newMaxDistance * 1.60934).toInt()
        }

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

        // Set custom enter animation when the activity is relaunched
        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
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