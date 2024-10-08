package com.example.aws_01

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.*
import android.util.Base64

//หน้านี้จะเป็นการส่งค่าให้แมพ
class ItemAdapter(private val items: List<Item>) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewLatitude: TextView = itemView.findViewById(R.id.textViewLatitude)
        val textViewLongitude: TextView = itemView.findViewById(R.id.textViewLongitude)
        val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        val viewHolder = ViewHolder(view)

        //30ถ้ากดview จำทำข้างล่าง
        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            val item = items[position]

            //34การย้ายหน้าไปMapsActivity
            val intent = Intent(parent.context, MapsActivity::class.java).apply {
                putExtra("latitude", item.latitude.toDouble())
                putExtra("longitude", item.longitude.toDouble())
                //putExtra คือการโยนค่า
            }
            parent.context.startActivity(intent)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textViewLatitude.text = "Latitude: ${item.latitude}"
        holder.textViewLongitude.text = "Longitude: ${item.longitude}"
        holder.textViewTimestamp.text = "Timestamp: ${item.timestamp}"
        //holder.textViewLatitude.text เอาข้อมูลมาแสดงที่ item_view.xml
        val base64Image = item.imageData
        val imageByteArray: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
        Glide.with(holder.itemView.context)
            .load(bitmap)
            .into(holder.imageView)
    }

    override fun getItemCount() = items.size
}
