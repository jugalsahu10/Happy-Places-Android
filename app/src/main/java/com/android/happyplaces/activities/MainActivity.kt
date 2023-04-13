package com.android.happyplaces.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.happyplaces.R
import com.android.happyplaces.adapters.HappyPlacesAdapter
import com.android.happyplaces.database.DatabaseHandler
import com.android.happyplaces.models.HappyPlaceModel
import com.android.happyplaces.utils.SwipeItemCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    companion object {
        const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        const val HAPPY_PLACE_DETAIL = "HAPPY_PLACE_DETAIL"
    }

    private lateinit var rvHappyPlacesList: RecyclerView
    private lateinit var tvNoRecordsAvailable: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val floatingAddHappyPlaceBtn = findViewById<FloatingActionButton>(R.id.fabAddHappyPlace)
        floatingAddHappyPlaceBtn.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        rvHappyPlacesList = findViewById(R.id.rv_happy_places_list)
        tvNoRecordsAvailable = findViewById(R.id.tv_no_records_available)

        setHappyPlacesList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_PLACE_ACTIVITY_REQUEST_CODE -> setHappyPlacesList()
            }
        }
    }

    private fun setHappyPlacesList() {
        val happyPlaces = getHappyPlaces()
        if (happyPlaces.size > 0) {
            Log.i("HAPPY_PLACE_MODEL_FETCH", "PAYLOAD: ${happyPlaces[0]}")
            setHappyPlacesRecyclerView(happyPlaces)
            tvNoRecordsAvailable.visibility = View.GONE
            rvHappyPlacesList.visibility = View.VISIBLE
        } else {
            tvNoRecordsAvailable.visibility = View.VISIBLE
            rvHappyPlacesList.visibility = View.GONE
        }
    }

    private fun setHappyPlacesRecyclerView(list: ArrayList<HappyPlaceModel>) {
        rvHappyPlacesList.layoutManager = LinearLayoutManager(this)
        rvHappyPlacesList.setHasFixedSize(true)

        val happyPlacesAdapter = HappyPlacesAdapter(applicationContext, list)
        happyPlacesAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, happyPlaceModel: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(HAPPY_PLACE_DETAIL, happyPlaceModel)
                startActivity(intent)
            }
        })

        rvHappyPlacesList.adapter = happyPlacesAdapter

        enableItemEditOnSwipe()
        enableItemDeleteOnSwipe()
    }

    private fun enableItemEditOnSwipe() {
        val editSwipeEditor = object : SwipeItemCallback(this, ItemTouchHelper.RIGHT) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val happyPlacesAdapter = rvHappyPlacesList.adapter as HappyPlacesAdapter
                happyPlacesAdapter.notifyEditItem(
                    this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeEditor)
        editItemTouchHelper.attachToRecyclerView(rvHappyPlacesList)
    }

    private fun enableItemDeleteOnSwipe() {
        val editSwipeEditor = object : SwipeItemCallback(this, ItemTouchHelper.LEFT) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val happyPlacesAdapter = rvHappyPlacesList.adapter as HappyPlacesAdapter

                happyPlacesAdapter.removeItem(viewHolder.adapterPosition)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeEditor)
        editItemTouchHelper.attachToRecyclerView(rvHappyPlacesList)
    }

    private fun getHappyPlaces(): ArrayList<HappyPlaceModel> {
        val dbHandler = DatabaseHandler(this)

        return dbHandler.getHappyPlacesList()
    }
}