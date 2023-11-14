package vc.hoo
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class AchListAdapter(
    private val context: Activity,
    //Holds the titles of the achievements
    private val title: ArrayList<String>,
    //Holds descriptions of the achievements & progress to next star
    private val description: ArrayList<String>,
    //Achievement images list
    private val achImgId: ArrayList<Drawable>,
    //List of achievement star progression images
    private val starImgId: ArrayList<Drawable>
) : ArrayAdapter<String>(context, R.layout.custom_ach_list, title) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.custom_ach_list, null, true)

        val titleText = rowView.findViewById(R.id.title) as TextView
        val ivAch = rowView.findViewById(R.id.icon) as ImageView
        val ivStar = rowView.findViewById(R.id.starIcon) as ImageView
        val subtitleText = rowView.findViewById(R.id.description) as TextView

        titleText.text = title[position]
        ivAch.setImageDrawable(achImgId[position])
        ivStar.setImageDrawable(starImgId[position])
        subtitleText.text = description[position]

        return rowView
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