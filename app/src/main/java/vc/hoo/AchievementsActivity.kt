package vc.hoo

import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import vc.hoo.databinding.ActivityAchievementsBinding


class AchievementsActivity : AppCompatActivity() {
    lateinit var AchievementsBinding: ActivityAchievementsBinding

    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AchievementsBinding = ActivityAchievementsBinding.inflate(layoutInflater)
        val AchievementsView = AchievementsBinding.root
        setContentView(AchievementsView)

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
    }

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