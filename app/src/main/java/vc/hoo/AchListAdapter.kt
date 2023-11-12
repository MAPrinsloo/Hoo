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
    private val title: ArrayList<String>,
    private val description: ArrayList<String>,
    private val achImgId: ArrayList<Drawable>,
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