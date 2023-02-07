package com.example.puzzlegame5

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private var imPicture: ImageView? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerViewAdapter
    private var imagePieceList: MutableList<Bitmap>? = ArrayList()
    private var container: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        imPicture = findViewById(R.id.imPicture)
        recyclerView = findViewById(R.id.recyclerView)
        container = findViewById(R.id.container)

        val width = 1000
        val height = width / 4 * 6

        val image = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.pobeg)
        val img = ThumbnailUtils.extractThumbnail(image, width, height)
        imPicture?.setImageBitmap(img)

        val b = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.jaguar)

        var btm = Bitmap.createBitmap(b)
        btm = ThumbnailUtils.extractThumbnail(btm, 300, 300)

        imagePieceList?.add(btm)
        imagePieceList?.add(btm)
        imagePieceList?.add(btm)
        imagePieceList?.add(btm)
        imagePieceList?.add(btm)
        imagePieceList?.add(btm)
        imagePieceList?.add(btm)
        imagePieceList?.add(btm)
        Log.d("log", "imagePieceList.size is: " + imagePieceList?.size + "  "+ btm)
        initRecyclerView(imagePieceList!!)
    }

    private fun initRecyclerView(list: MutableList<Bitmap>) {
        recyclerView = findViewById(R.id.recyclerView)
        adapter = RecyclerViewAdapter(list)
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.default_padding).toInt()
            )
        )
    }

    fun removeItem(view: View) {
        if (imagePieceList!!.size > 0) {
            val index = 0
            createImageView(index)
            imagePieceList!!.removeAt(index)
            adapter.notifyItemRemoved(index)
        }

    }

    private fun createImageView(index: Int) {

    }
}