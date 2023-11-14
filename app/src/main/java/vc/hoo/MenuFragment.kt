package vc.hoo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import vc.hoo.databinding.FragmentMenuBinding


class MenuFragment() : Fragment() {
    lateinit var MenuBinding: FragmentMenuBinding
    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binding
        MenuBinding = FragmentMenuBinding.inflate(inflater, container, false)
        //----------------------------------------------------------------------------------------//
        //On Document Click
        MenuBinding.btnMenuDocument.setOnClickListener {
            //Retrieve share preferences
            val sharedPref = requireContext().getSharedPreferences("username", Context.MODE_PRIVATE)
            val username = sharedPref.getString("username", "")
            val intent = Intent(requireContext(), DocumentActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
        //----------------------------------------------------------------------------------------//
        //On Navigate Click
        MenuBinding.btnMenuNavigate.setOnClickListener {
            //Retrieve share preferences
            val sharedPref = requireContext().getSharedPreferences("username", Context.MODE_PRIVATE)
            val username = sharedPref.getString("username", "")
            val intent = Intent(requireContext(), NavigateActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
        //----------------------------------------------------------------------------------------//
        //On History Click
        MenuBinding.btnMenuHistory.setOnClickListener {
            //Retrieve share preferences
            val sharedPref = requireContext().getSharedPreferences("username", Context.MODE_PRIVATE)
            val username = sharedPref.getString("username", "")
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        return MenuBinding.root
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