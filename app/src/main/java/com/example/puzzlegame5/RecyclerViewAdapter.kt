package com.example.puzzlegame5

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(private val imagePieceList: MutableList<Bitmap>) :
    RecyclerView.Adapter<RecyclerViewAdapter.ImagePieceViewHolder>() {

    class ImagePieceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPiece: ImageView = itemView.findViewById(R.id.ivPiece)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePieceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.piece_item,parent,false)
        return ImagePieceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImagePieceViewHolder, position: Int) {
        holder.ivPiece.setImageBitmap(imagePieceList[position])
    }

    override fun getItemCount(): Int {
        return imagePieceList.size
    }
}