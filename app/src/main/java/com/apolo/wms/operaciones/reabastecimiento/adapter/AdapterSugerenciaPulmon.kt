package com.apolo.wms.operaciones.reabastecimiento.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.clases.reabastecimiento.SugerenciaPulmonReabastecimiento
import com.apolo.wms.operaciones.reabastecimiento.ReabastecimientoMarcaderiaDriveInNew
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms2.R

class AdapterSugerenciaPulmon (private val context: Context,
                               private val dataSource: List<SugerenciaPulmonReabastecimiento>,
                               private val molde: Int ) : RecyclerView.Adapter<AdapterSugerenciaPulmon.ViewHolder>() {


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

        holder.tvDireccion.text = "${ItemsViewModel.codDireccion} - ${ItemsViewModel.vencimiento}"
        holder.tvCantidadDisp.text = ItemsViewModel.cantidad


        holder.llback.setOnClickListener {
            ReabastecimientoMarcaderiaDriveInNew.posicionSugerenciaPulmon = position
            this.notifyDataSetChanged()
        }



        if (position%2==0){
            holder.llback.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llback.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (ReabastecimientoMarcaderiaDriveInNew.posicionSugerenciaPulmon == position) {
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
        val llback: LinearLayout = itemView.findViewById(R.id.llBack) as LinearLayout
        val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion) as TextView
        val tvCantidadDisp: TextView = itemView.findViewById(R.id.tvCantidadDisp) as TextView




    }


}
