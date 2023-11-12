package vc.hoo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.common.io.Resources
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.maps.extension.style.expressions.dsl.generated.switchCase
import vc.hoo.databinding.ActivityAchievementsBinding


class AchievementsActivity : AppCompatActivity() {
    //binding
    lateinit var AchievementsBinding: ActivityAchievementsBinding
    //firebase db
    lateinit private var db: FirebaseFirestore
    //username
    lateinit var Username: String
    //Array lists for displaying data in listview
    var ach_name = arrayListOf<String>()
    var ach_discription = arrayListOf<String>()
    var achImage = arrayListOf<Drawable>()
    var achStarsImage = arrayListOf<Drawable>()
    val starImages = arrayListOf<Drawable>()

    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initialize username
        Username = intent.getStringExtra("username").toString()
        //initialize db
        db = Firebase.firestore
        //binding
        AchievementsBinding = ActivityAchievementsBinding.inflate(layoutInflater)
        val AchievementsView = AchievementsBinding.root

        //Star drawables to the array
        starImages.add(resources.getDrawable(R.drawable.achivement_star_0, null))
        starImages.add(resources.getDrawable(R.drawable.achivement_star_1, null))
        starImages.add(resources.getDrawable(R.drawable.achivement_star_2, null))
        starImages.add(resources.getDrawable(R.drawable.achivement_star_3, null))

        populateDisplayArrays()
        //populate adatper
        val displayAdapter =
            AchListAdapter(this, title = ach_name, description = ach_discription, achImgId = achImage, starImgId = achStarsImage)
        AchievementsBinding.lvAchievements.adapter = displayAdapter
        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )


        //----------------------------------------------------------------------------------------//
        //Nav Menu
        AchievementsBinding.mtAchievements.setNavigationOnClickListener()
        {
            val slide = Slide()
            slide.slideEdge = Gravity.START
            TransitionManager.beginDelayedTransition(AchievementsView, slide)
            AchievementsBinding.flMenuSideSheet.isVisible =
                !AchievementsBinding.flMenuSideSheet.isVisible
        }
        //----------------------------------------------------------------------------------------//
        //Account Menu
        AchievementsBinding.mtAchievements.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.account) {
                val slide = Slide()
                slide.slideEdge = Gravity.END
                TransitionManager.beginDelayedTransition(AchievementsView, slide)
                AchievementsBinding.flAccountSideSheet.isVisible =
                    !AchievementsBinding.flAccountSideSheet.isVisible
                true
            } else {
                false
            }
        }
        setContentView(AchievementsView)
    }
    //----------------------------------------------------------------------------------------//
    //Retrieves data from db and populates the corresponding arrays
    private fun populateDisplayArrays() {
        var arrDbEntries = arrayListOf<String>()
        //Firebase directory

        val achivementCollection = db.collection("/$Username/")
        val birdCollection = db.collection("/$Username/user_details/birds/")
        var mostSpottedAmount = 0
        var totalSpotted = 0
        var numTypesSpotted = 0

        achivementCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                mostSpottedAmount = (document["most_spotted_num"] as? Long?: 0).toInt()
                totalSpotted = (document["total_spotted"] as? Long?: 0).toInt()
            }
            //Bird specialist
            checkBirdSpecialistAch(most_spotted_num = mostSpottedAmount)

            //Target acquired
            checkTargetAcquiredAch(total_spotted = totalSpotted)
            AchievementsBinding.lvAchievements.isVisible = false
            AchievementsBinding.lvAchievements.isVisible = true
        }
        birdCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                numTypesSpotted++
            }
            //Variety spotter
            checkVarietySpotterAch(num_types_spotted = numTypesSpotted)
            AchievementsBinding.lvAchievements.isVisible = false
            AchievementsBinding.lvAchievements.isVisible = true
        }
    }
    //----------------------------------------------------------------------------------------//
    //Checks the data entered against the achievement requirements
    private fun checkBirdSpecialistAch(most_spotted_num: Int) {
        ach_name.add("Bird Specialist")

        // Default values
        var description = ""

        if(most_spotted_num < 5)
        {
            description = "Spot 1 type of bird 5 times. $most_spotted_num / 5"
            achStarsImage.add(starImages[0])
        }
        else if(most_spotted_num < 10)
        {
            description = "Spot 1 type of bird 10 times. $most_spotted_num / 10"
            achStarsImage.add(starImages[1])
        }
        else if(most_spotted_num < 15)
        {
            description = "Spot 1 type of bird 15 times. $most_spotted_num / 15"
            achStarsImage.add(starImages[2])
        }
        else if(most_spotted_num >= 15)
        {
            description = "You have spotted 1 type of bird 15 times."
            achStarsImage.add(starImages[3])
        }

        ach_discription.add(description)
        achImage.add(resources.getDrawable(R.drawable.bird_specialist_ach, null))
    }

    //----------------------------------------------------------------------------------------//
    //Checks the data entered against the achievement requirements
    private fun checkTargetAcquiredAch(total_spotted: Int)
    {
        ach_name.add("Target Acquired")

        // Default values
        var description = ""

        if(total_spotted < 3)
        {
            description = "Spot 3 birds. $total_spotted / 3"
            achStarsImage.add(starImages[0])
        }
        else if(total_spotted < 15)
        {
            description = "Spot 15 birds. $total_spotted / 15"
            achStarsImage.add(starImages[1])
        }
        else if(total_spotted < 30)
        {
            description = "Spot 30 birds. $total_spotted / 30"
            achStarsImage.add(starImages[2])
        }
        else if(total_spotted >= 30)
        {
            description = "You have spotted 30 birds."
            achStarsImage.add(starImages[3])
        }


        ach_discription.add(description)
        achImage.add(resources.getDrawable(R.drawable.target_acquired_ach, null))
    }
    //----------------------------------------------------------------------------------------//
    //Checks the data entered against the achievement requirements
    private fun checkVarietySpotterAch(num_types_spotted: Int)
    {
        ach_name.add("Variety Spotter")
        var description = ""

        if(num_types_spotted < 5)
        {
            description = "Spot 5 different types of birds. $num_types_spotted / 5"
            achStarsImage.add(starImages[0])
        }
        else if(num_types_spotted < 10)
        {
            description = "Spot 10 different types of birds. $num_types_spotted / 10"
            achStarsImage.add(starImages[1])
        }
        else if(num_types_spotted < 15)
        {
            description = "Spot 15 different types of birds. $num_types_spotted / 15"
            achStarsImage.add(starImages[2])
        }
        else if(num_types_spotted >= 15)
        {
            description = "You have spotted 15 different types of birds."
            achStarsImage.add(starImages[3])
        }

        ach_discription.add(description)
        achImage.add(resources.getDrawable(R.drawable.variety_spotter_ach, null))
    }
    //----------------------------------------------------------------------------------------//
    //Runs animation on new intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        AchievementsBinding.flAccountSideSheet.isVisible = false
        AchievementsBinding.flMenuSideSheet.isVisible = false
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
*/