package com.apolo.wms.operaciones.separacion.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apolo.wms.clases.separacion.SeparadorSeparacion
import com.apolo.wms.operaciones.separacion.BuscaGrupoSeparacion
import com.apolo.wms2.R

class AdapterSeparadorSeparacion (private val context: Context,
                                  private val dataSource: List<SeparadorSeparacion>,
                                  private val molde: Int ) : RecyclerView.Adapter<AdapterSeparadorSeparacion.ViewHolder>() {


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

        holder.tvCodSeparador.text = ItemsViewModel.codSeparador
        holder.tvNombreSeparador.text = ItemsViewModel.nombre


        holder.llback.setOnClickListener {
            BuscaGrupoSeparacion.posicionSeparador = position
            this.notifyDataSetChanged()
        }


        if (position%2==0){
            holder.llback.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llback.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (BuscaGrupoSeparacion.posicionSeparador == position) {
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
        val tvCodSeparador: TextView = itemView.findViewById(R.id.tvCodSeparador)
        val tvNombreSeparador: TextView = itemView.findViewById(R.id.tvNombreSeparador)


    }


}
