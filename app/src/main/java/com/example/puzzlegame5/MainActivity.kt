package com.example.puzzlegame5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.Button
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
    private var imPictureWidth: Int? = null
    private var imPictureHeight: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //убираем ActionBar
        supportActionBar?.hide()

        //получаем размеры экрана устройства
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val screenWidth: Int = size.x
        val screenHeight: Int = size.y
        Log.d(
            "log",
            "screenWidth is: " + screenWidth + " screenHeight is: " + screenHeight
        )

        imPicture = findViewById(R.id.imPicture)
        recyclerView = findViewById(R.id.recyclerView)
        container = findViewById(R.id.container)

        // Конвертируем Drawable в Bitmap
        var image = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.pobeg)

        // Вычисляем нужные нам ширину и высоту изображения
        var width = screenWidth - screenWidth / 100 * 30
        var height = width / 4 * 6

        //устанавливаем их для изображения
        image = ThumbnailUtils.extractThumbnail(image, width, height)

        // Выводим Bitmap в ImageView
        imPicture?.setImageBitmap(image)

        val button: Button = findViewById<View>(R.id.btnCreateList) as Button
        button.setOnClickListener(View.OnClickListener {
            // определяем разьеры ImageView
            Log.d(
                "log",
                "imPicture?.width is: " + imPicture?.width + " imPicture?.height is: " + imPicture?.height
            )

            imagePieceList = getImagePieceList(imPicture)

            initRecyclerView(imagePieceList!!)

        })


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

    private fun getBitmapPositionInsideImageView(imageView: ImageView?): IntArray {

        val ret = IntArray(4)
        if (imageView == null || imageView.drawable == null) {
            return ret
        }

        //get image dimensions
        //get image matrix values and place them in an array
        val f = FloatArray(9)

        imageView.imageMatrix.getValues(f)

        //extract the scale values using the constants(if aspect ratio maintained scaleX == scaleY)
        val scaleX = f[Matrix.MSCALE_X]
        val scaleY = f[Matrix.MSCALE_Y]

        //get the drawable (could also get the bitmap the drawable and getWidth / getHeight)
        val d = imageView.drawable

        val origW = d.intrinsicWidth
        val origH = d.intrinsicHeight

        //calculate the actual dimensions
        val actW = Math.round(origW * scaleX)
        val actH = Math.round(origH * scaleY)

        ret[2] = actW
        ret[3] = actH

        //get image position
        // we assume that the image is centered into ImageView
        val imageViewW = imageView.width
        val imageViewH = imageView.height

        val top = (imageViewH - actH) / 2
        val left = (imageViewW - actW) / 2

        ret[0] = top
        ret[1] = left



        return ret
    }


    fun getImagePieceList(imageView: ImageView?): ArrayList<Bitmap> {
        val piecesNumber = 24
        val rows = 6
        val columns = 4
//        val imageView = findViewById<ImageView>(R.id.imPicture)
        val list = ArrayList<Bitmap>(piecesNumber)

        //get the scaled bitmap of the source image
        val drawable = imageView!!.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val dimensions = getBitmapPositionInsideImageView(imageView)

        val scaledBitmapLeft = dimensions[0]
        val scaledBitmapTop = dimensions[1]
        val scaledBitmapWidth = dimensions[2]
        val scaledBitmapHeight = dimensions[3]

        val croppedImageWidth = scaledBitmapWidth - 2 * Math.abs(scaledBitmapLeft)
        val croppedImageHeight = scaledBitmapHeight - 2 * Math.abs(scaledBitmapTop)


        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap, scaledBitmapWidth, scaledBitmapHeight, true
        )

        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap, Math.abs(scaledBitmapLeft),
            Math.abs(scaledBitmapTop),
            croppedImageWidth, croppedImageHeight
        )
        //calculate the width and the height of the pieces
        val pieceWidth = croppedImageWidth / columns
        val pieceHeight = croppedImageHeight / rows

        //create each bitmap piece and add it to the result array
        var yCoord = 0
        var xCoord = 0
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                //calculate offset for each piece
                var offsetX = 0
                var offsetY = 0

                xCoord = pieceWidth * column
                yCoord = pieceHeight * row


                //надо прописать отступы для различных фрагментов

//                if (row == 0 && column == 0) {
//                    xCoord = 0
//                    yCoord = 0
//                }
//                if (row == 0 && column < columns - 1) {
//                    xCoord = xCoord + pieceWidth
//                    yCoord = 0
//                }
//                if (row == 0 && column != 0 && column != columns - 1) {
//
//                }
//                if (row > 0 && row < rows - 1 && column > 0 && column < columns - 1) {
//
//                }
//                if (row > 0 && row < rows - 1 && column == columns - 1) {
//
//                }
//                if (row == rows - 1 && column == columns - 1) {
//
//                }
//                if (row == rows - 1 && column == 0) {
//
//                }
//                if (row == rows - 1 && column != 0 && column != columns - 1) {
//
//                }
//                if (row > 0 && row < rows - 1 && column == 0) {
//
//                }
//
//                if (column > 0) {
//                    offsetX = pieceWidth / 3
//                    xCoord += pieceWidth
//                }
//                if (row > 0) {
//                    offsetY = pieceHeight / 3
//                    yCoord += pieceHeight
//                }


                val pieceBitmap = Bitmap.createBitmap(
                    croppedBitmap,
                    xCoord,
                    yCoord,
                    pieceWidth,
                    pieceHeight
                )
                list.add(pieceBitmap)
            }
        }




        return list
    }

    fun removePiece(view: View) {
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