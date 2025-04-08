package com.apolo.wms.operaciones.cerrarsesion.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apolo.wms.clases.cerrarsesion.ConsultaSesion
import com.apolo.wms.operaciones.cerrarsesion.CerrarSesion
import com.apolo.wms2.R

class AdapterSesion (private val context: Context,
                     private val dataSource: List<ConsultaSesion>,
                     private val molde: Int ) : RecyclerView.Adapter<AdapterSesion.ViewHolder>() {


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

        holder.tvUsuario.text = ItemsViewModel.codUsuario
        holder.tvDireccion.text = ItemsViewModel.ip
        holder.tvEstado.text = ItemsViewModel.estado
        holder.tvVersion.text = ItemsViewModel.version
        holder.tvDispositivo.text = ItemsViewModel.imei
        holder.tvInicio.text = ItemsViewModel.inicio
        holder.tvCierre.text = ItemsViewModel.cierre


        holder.llback.setOnClickListener {
            CerrarSesion.posicionUsuario = position
            this.notifyDataSetChanged()
        }




        if (position%2==0){
            holder.llback.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llback.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (CerrarSesion.posicionUsuario == position) {
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
        val tvUsuario: TextView = itemView.findViewById(R.id.tvUsuario)
        val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvVersion: TextView = itemView.findViewById(R.id.tvVersion)
        val tvDispositivo: TextView = itemView.findViewById(R.id.tvDispositivo)
        val tvInicio: TextView = itemView.findViewById(R.id.tvInicio)
        val tvCierre: TextView = itemView.findViewById(R.id.tvCierre)




    }


}