package com.apolo.wms.operaciones.inventario.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apolo.wms.clases.inventario.DetalleInventario2
import com.apolo.wms.operaciones.inventario.ConfirmaInventario
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms2.R

class AdapterDetalleInventario2 (private val context: Context,
                                 private val dataSource: List<DetalleInventario2>,
                                 private val molde: Int ) : RecyclerView.Adapter<AdapterDetalleInventario2.ViewHolder>() {


    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(context)
            .inflate(molde, parent, false)

        return ViewHolder(view)


    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = dataSource[position]

        // sets the image to the imageview from our itemHolder class
        //holder.imageView.setImageResource(ItemsViewModel.image)

        // sets the text to the textview from our itemHolder class
        //holder.textView.text = ItemsViewModel.text




        holder.tvNroOrden_018.text = ItemsViewModel.nroOrden
        holder.tvCodArticulo_018.text = ItemsViewModel.codArticulo01
        holder.tvDescArticulo_018.text = ItemsViewModel.descArticulo01


        holder.llback.setOnClickListener {
            ConfirmaInventario.posicionInventarioDet = position
            this.notifyDataSetChanged()
        }


        if (position%2==0){
            holder.llback.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llback.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (ConfirmaInventario.posicionInventarioDet == position) {
            holder.llback.setBackgroundColor(Color.BLUE)
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return dataSource.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        //val imageView: ImageView = itemView.findViewById(R.id.imageview)
        //val textView: TextView = itemView.findViewById(R.id.textView)
        val llback: LinearLayout = itemView.findViewById(R.id.llBack)
        val tvNroOrden_018: TextView = itemView.findViewById(R.id.tvNroOrden_018)
        val tvCodArticulo_018: TextView = itemView.findViewById(R.id.tvCodArticulo_018)
        val tvDescArticulo_018: TextView = itemView.findViewById(R.id.tvDescArticulo_018)




    }


}