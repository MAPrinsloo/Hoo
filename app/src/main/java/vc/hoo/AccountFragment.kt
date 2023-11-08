package vc.hoo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import vc.hoo.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {
    lateinit var AccountBinding: FragmentAccountBinding

    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Holds the users username
        val sharedPref = requireContext().getSharedPreferences("username", Context.MODE_PRIVATE)
        //If the username is not gotten then return ""
        val username = sharedPref.getString("username", "")
        //Binding
        AccountBinding = FragmentAccountBinding.inflate(inflater, container, false)

        //----------------------------------------------------------------------------------------//
        //Opens the AchievementsActivity
        AccountBinding.btnAcntAchievements.setOnClickListener {
            val intent = Intent(requireContext(), AchievementsActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
        //----------------------------------------------------------------------------------------//
        //Opens the SettingsAndStatsActivity
        AccountBinding.btnAcntSettStats.setOnClickListener {
            val intent = Intent(requireContext(), SettingsAndStatsActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
        //----------------------------------------------------------------------------------------//
        //Opens LoginActivity
        AccountBinding.btnAcntSignOut.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            //sharedPref.edit().clear().apply()
        }

        return AccountBinding.root
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