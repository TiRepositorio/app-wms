package com.apolo.wms.operaciones.consulta.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apolo.wms.clases.consulta.stock.DepositoStock
import com.apolo.wms.operaciones.consulta.ConsultaStock
import com.apolo.wms2.R

class AdapterDepositoStock (private val context: Context,
                            private val dataSource: List<DepositoStock>,
                            private val molde: Int ) : RecyclerView.Adapter<AdapterDepositoStock.ViewHolder>() {


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

        holder.tvDeposito.text = ItemsViewModel.deposito
        holder.tvUnidad.text = ItemsViewModel.referencia
        holder.tvCantidad.text = ItemsViewModel.cantBasica

        holder.llback.setOnClickListener {
            ConsultaStock.posicionDeposito = position
            this.notifyDataSetChanged()
        }




        if (position%2==0){
            holder.llback.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llback.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (ConsultaStock.posicionDeposito == position) {
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
        val tvDeposito: TextView = itemView.findViewById(R.id.tvDeposito)
        val tvUnidad: TextView = itemView.findViewById(R.id.tvUnidad)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)




    }


}