package com.example.puzzlegame5

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    var pieces: ArrayList<PuzzlePiece>? = null
    private var imPicture: ImageView? = null
    private var container: FrameLayout? = null
    private var columns = 6
    private var rows = columns + columns / 2
    private var piecesNumber = columns * rows
    private var xDelta = 0
    private var yDelta = 0
    private var imageTileWidth: Int = 0
    private var imageTileHeight: Int = 0


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

        imPicture = findViewById(R.id.imPicture)
        container = findViewById(R.id.container)
//        recyclerView = findViewById(R.id.recyclerView)
//        textView = findViewById(R.id.textView)
//        seekBar = findViewById(R.id.seekBar)
        // Конвертируем Drawable в Bitmap
        var image = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.pobeg)

        // Вычисляем нужные нам ширину и высоту изображения
        val width = screenWidth - screenWidth / 100 * 30
        val height = width / 4 * 6

        //устанавливаем их для изображения
        image = ThumbnailUtils.extractThumbnail(image, width, height)

        // Выводим Bitmap в ImageView
        imPicture?.setImageBitmap(image)

        // run image related code after the view was laid out to have all dimensions calculated
        imPicture!!.post {
            val touchListener1 = TouchListener(this)
            pieces = getImagePieceList(imPicture)
            for (piece in pieces!!) {
                piece.setOnTouchListener(touchListener1)
                container!!.addView(piece)

                //randomize position on the bottom of screen
                val lParams = piece.layoutParams as FrameLayout.LayoutParams
                lParams.leftMargin = Random.nextInt(
                    container!!.width - piece.pieceWidth
                )
                lParams.topMargin = container!!.height - piece.pieceHeight

                piece.layoutParams = lParams
            }

        }


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


    fun getImagePieceList(imageView: ImageView?): ArrayList<PuzzlePiece> {

        columns = 3
        rows = columns + columns / 2
        piecesNumber = columns * rows



        Log.d(
            "log",
            " \ncolumns: " + columns + "\nrows: " + rows + "\npiecesNumber: " + piecesNumber
        )


        val pieces = ArrayList<PuzzlePiece>(piecesNumber)

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
                piece.xCoord = xCoord
                piece.yCoord = yCoord
                imageTileWidth = pieceWidth
                imageTileHeight = pieceHeight


                pieces.add(piece)
                xCoord += pieceWidth
            }
            yCoord += pieceHeight
        }
        return pieces
    }


    @SuppressLint("ClickableViewAccessibility")
    val touchListener = View.OnTouchListener { view, event ->
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val lParams = view.layoutParams as FrameLayout.LayoutParams
                xDelta = x - lParams.leftMargin
                yDelta = y - lParams.topMargin
            }
            MotionEvent.ACTION_UP -> {
                Toast.makeText(applicationContext, "Объект перемещён", Toast.LENGTH_SHORT).show()
            }
            MotionEvent.ACTION_MOVE -> {
                if (x - xDelta + view.width <= container!!.width && y - yDelta + view.height <= container!!.height && x - xDelta >= 0 && y - yDelta >= 0) {
                    val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                    layoutParams.leftMargin = x - xDelta
                    layoutParams.topMargin = y - yDelta
                    layoutParams.rightMargin = 0
                    layoutParams.bottomMargin = 0
                    view.layoutParams = layoutParams
                }
            }
        }
        container!!.invalidate()
        true
    }


    fun checkGameOver() {
        if (isGameOver) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("You Won!!!")
                .setIcon(R.drawable.ic_celebration)
                .setMessage("You won!\nDo you want to play a new game?")
                .setPositiveButton("Yes") { dialog, _ ->
                    finish()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    finish()
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private val isGameOver: Boolean
        get() {
            for (piece in pieces!!) {
                if (piece.canMove) {
                    return false
                }
            }
            return true
        }


//    companion object {
//
//        //продумать как передавать сюда размеры
//
//        fun resizePath(path: Path?, width: Float, height: Float): Path {
//            val bounds = RectF(0f, 0f, width, height)
//            val resizedPath = Path(path)
//            val src = RectF()
//            resizedPath.computeBounds(src, true)
//            val resizeMatrix = Matrix()
//            resizeMatrix.setRectToRect(src, bounds, Matrix.ScaleToFit.CENTER)
//            resizedPath.transform(resizeMatrix)
//            return resizedPath
//        }
//    }
}
