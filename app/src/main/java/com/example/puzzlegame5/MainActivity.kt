package com.example.puzzlegame5

import android.graphics.*
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

    fun getCroppedBitmap(src: Bitmap, path: Path?): Bitmap {
        val output = Bitmap.createBitmap(
            src.width,
            src.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = -0x1000000
        canvas.drawPath(path!!, paint)

        // Keeps the source pixels that cover the destination pixels,
        // discards the remaining source and destination pixels.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return output
    }

    fun getPathLeftMidlePuzzle(): Path {
        val path1 = Path()

        path1.moveTo(0f, 100f)

        path1.rLineTo(150f, 0f)
        path1.rCubicTo(-100f, -100f, 200f, -100f, 100f, 0f)
        path1.rLineTo(150f, 0f)

        path1.rLineTo(0f, 150f)
        path1.rCubicTo(100f, -100f, 100f, 200f, 0f, 100f)
        path1.rLineTo(0f, 150f)
        path1.rLineTo(-150f, 0f)
        path1.rCubicTo(100f, -100f, -200f, -100f, -100f, 0f)
        path1.rLineTo(-150f, 0f)
        path1.rLineTo(0f, -400f)
        return resizePath(path1, 250f,250f)

    }

    fun getPathLeftTopPuzzle(): Path {
        val path = Path()
        path.moveTo(0f, 0f)
        path.rLineTo(400f, 0f)
        path.rLineTo(0f, 150f)
        path.rCubicTo(100f, -100f, 100f, 200f, 0f, 100f)
        path.rLineTo(0f, 150f)
        path.rLineTo(-150f, 0f)
        path.rCubicTo(100f, -100f, -200f, -100f, -100f, 0f)
        path.rLineTo(-150f, 0f)
        path.rLineTo(0f, -400f)
        return resizePath(path, 250f,250f)
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

        //продумать как задавать количество фрагментов картинки

        val columns = 4
        val rows = columns/4 * 6
        val piecesNumber = rows * columns

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
        for (row in 0 until rows) {
            var xCoord = 0
            for (column in 0 until columns) {
                //calculate offset for each piece
                var offsetX = 0
                var offsetY = 0

                if (column > 0) {
                    offsetX = pieceWidth / 3
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3
                }

              val  pieceBitmap = Bitmap.createBitmap(
                  croppedBitmap,
                  xCoord - offsetX,
                  yCoord - offsetY,
                  pieceWidth + offsetX,
                  pieceHeight + offsetY
              )

                val piece = PuzzlePiece(applicationContext)
                piece.setImageBitmap(pieceBitmap)
                piece.xCoord = xCoord - offsetX + imageView.left
                piece.yCoord = yCoord - offsetY + imageView.top
                piece.pieceWidth = pieceWidth + offsetX
                piece.pieceHeight = pieceHeight + offsetY

                //this bitmap will hold our final puzzle piece image
                val puzzlePiece = Bitmap.createBitmap(
                    pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888
                )

                //draw path
                val bumpSize = pieceHeight / 4
                val canvas = Canvas(puzzlePiece)
                val path = android.graphics.Path()
                path.moveTo(offsetX.toFloat(), offsetY.toFloat())

                if (row == 0) {
                    //top side piece
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        offsetY.toFloat()
                    )
                } else {
                    //top bump
                    path.lineTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        offsetY.toFloat()
                    )
                    path.cubicTo(
                        ((offsetX + (pieceBitmap.width - offsetX) / 6).toFloat()),
                        (offsetY - bumpSize).toFloat(),
                        ((offsetX + (pieceBitmap.width - offsetX) / 6 * 5)).toFloat(),
                        (offsetY - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 3 * 2).toFloat(),
                        offsetY.toFloat()
                    )

                    path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
                }
                if (column == columns - 1) {
                    //right side piece
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                } else {
                    //right bump
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3).toFloat()
                    )
                    path.cubicTo(
                        (pieceBitmap.width - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6).toFloat(),
                        (pieceBitmap.width - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6 * 5).toFloat(),
                        pieceBitmap.width.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat()
                    )

                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                }
                if (row == rows - 1) {
                    //bottom side piece
                    path.lineTo(
                        offsetX.toFloat(), pieceBitmap.height.toFloat()
                    )
                } else {
                    //bottom bump
                    path.lineTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 3 * 2).toFloat(),
                        pieceBitmap.height.toFloat()
                    )

                    path.cubicTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 6 * 5).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 6).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        pieceBitmap.height.toFloat()
                    )

                    path.lineTo(
                        offsetX.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                }
                if (column == 0) {
                    //left side piece
                    path.close()
                } else {
                    //left bump
                    path.lineTo(
                        offsetX.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat(),
                    )
                    path.cubicTo(
                        (offsetX - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6 * 5).toFloat(),
                        (offsetX - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6).toFloat(),
                        offsetX.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3).toFloat()
                    )

                    path.close()
                }



                //mask the piece
                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = -0x1000000
                paint.style = Paint.Style.FILL
                canvas.drawPath(path, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(pieceBitmap, 0f, 0f, paint)

                //draw a white border
                var border = Paint()
                border.isAntiAlias = true
                border.color = -0x7f000001
                border.style = Paint.Style.STROKE
                border.strokeWidth = 8.0f
                canvas.drawPath(path, border)

                //draw a black border
                border = Paint()
                border.isAntiAlias = true
                border.color = -0x80000000
                border.style = Paint.Style.STROKE
                border.strokeWidth = 3.0f
                canvas.drawPath(path, border)


                //set the resulting bitmap to the piece
                piece.setImageBitmap(puzzlePiece)


                list.add(puzzlePiece)
                xCoord += pieceWidth
            }
            yCoord += pieceHeight
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

    fun createImageView(index: Int) {

    }

    companion object {

        //продумать как передавать сюда размеры

        fun resizePath(path: Path?, width: Float, height: Float): Path {
            val bounds = RectF(0f, 0f, width, height)
            val resizedPath = Path(path)
            val src = RectF()
            resizedPath.computeBounds(src, true)
            val resizeMatrix = Matrix()
            resizeMatrix.setRectToRect(src, bounds, Matrix.ScaleToFit.CENTER)
            resizedPath.transform(resizeMatrix)
            return resizedPath
        }
    }
}
