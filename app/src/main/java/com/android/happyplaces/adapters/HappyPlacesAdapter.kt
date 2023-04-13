package com.android.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.happyplaces.R
import com.android.happyplaces.activities.AddHappyPlaceActivity
import com.android.happyplaces.activities.MainActivity
import com.android.happyplaces.database.DatabaseHandler
import com.android.happyplaces.models.HappyPlaceModel
import de.hdodenhof.circleimageview.CircleImageView

class HappyPlacesAdapter(
    private val context: Context,
    private val list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var onClickListener: OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_happy_place, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val happyPlaceModel = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.findViewById<CircleImageView>(R.id.iv_place_image)
                .setImageURI((Uri.parse(happyPlaceModel.image)))
            holder.itemView.findViewById<TextView>(R.id.tvTitle).text = happyPlaceModel.title
            holder.itemView.findViewById<TextView>(R.id.tvDescription).text =
                happyPlaceModel.description
            holder.itemView.setOnClickListener {
                onClickListener?.onClick(position, happyPlaceModel)
            }
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.HAPPY_PLACE_DETAIL, list[position])
        activity.startActivityForResult(intent, requestCode)
    }

    fun removeItem(position: Int) {
        val dbHandler = DatabaseHandler(context)
        dbHandler.deleteHappyPlace(list[position])
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, happyPlaceModel: HappyPlaceModel)
    }
}