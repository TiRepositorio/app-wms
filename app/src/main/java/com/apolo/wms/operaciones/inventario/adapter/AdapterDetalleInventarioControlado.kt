package com.apolo.wms.operaciones.inventario.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apolo.wms.clases.inventario.DetalleInventarioControlado
import com.apolo.wms.operaciones.inventario.DetallePlanillaInventarioG
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms2.R

class AdapterDetalleInventarioControlado (private val context: Context,
                                          private val dataSource: List<DetalleInventarioControlado>,
                                          private val molde: Int ) : RecyclerView.Adapter<AdapterDetalleInventarioControlado.ViewHolder>() {


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



        var dir_origen_aux: String = ItemsViewModel.codDireccion
        if (dir_origen_aux.isNotEmpty()) {
            dir_origen_aux = dir_origen_aux.substring(0, 3) + "-" + dir_origen_aux.substring(
                3,
                6
            ) + "-" + dir_origen_aux.substring(6, 7) + "-" + dir_origen_aux.subSequence(7, 9)
        }

        holder.tvDireccion.text = dir_origen_aux
        holder.tvCodArticulo.text = ItemsViewModel.codArticulo01
        holder.tvDescArticulo.text = ItemsViewModel.descArticulo


        holder.llback.setOnClickListener {
            DetallePlanillaInventarioG.posicionDetalle = position
            this.notifyDataSetChanged()
        }


        if (position%2==0){
            holder.llback.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llback.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (DetallePlanillaInventarioG.posicionDetalle == position) {
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
        val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        val tvCodArticulo: TextView = itemView.findViewById(R.id.tvCodArticulo)
        val tvDescArticulo: TextView = itemView.findViewById(R.id.tvDescArticulo)




    }


}