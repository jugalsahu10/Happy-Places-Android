package com.android.happyplaces.activities

import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.android.happyplaces.R
import com.android.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    private lateinit var tbHappyPlaceDetail: Toolbar
    private lateinit var ivPlaceImage: AppCompatImageView
    private lateinit var tvDescription: TextView
    private lateinit var tvLocation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail)


        tbHappyPlaceDetail = findViewById(R.id.toolbar_happy_place_detail)
        ivPlaceImage = findViewById(R.id.iv_place_image)
        tvDescription = findViewById(R.id.tv_description)
        tvLocation = findViewById(R.id.tv_location)

        val parcelable: HappyPlaceModel? =
            intent.getParcelableExtra(MainActivity.HAPPY_PLACE_DETAIL)
        parcelable?.let {
            setSupportActionBar(tbHappyPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = it.title

            tbHappyPlaceDetail.setNavigationOnClickListener {
                onBackPressed()
            }

            ivPlaceImage.setImageURI(Uri.parse(it.image))
            tvDescription.text = it.description
            tvLocation.text = it.location
        }
    }
}