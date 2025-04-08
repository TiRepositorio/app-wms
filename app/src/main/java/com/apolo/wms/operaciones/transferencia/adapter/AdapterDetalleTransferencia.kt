package com.apolo.wms.operaciones.transferencia.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apolo.wms.clases.transferencia.DetalleTransferencia
import com.apolo.wms.operaciones.transferencia.TransferenciaDeposito
import com.apolo.wms2.R

class AdapterDetalleTransferencia (private val context: Context,
                                   private val dataSource: List<DetalleTransferencia>,
                                   private val molde: Int ) : RecyclerView.Adapter<AdapterDetalleTransferencia.ViewHolder>() {


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

        holder.tvOrden.text = ItemsViewModel.nroOrden
        holder.tvDepositoOrigen.text = ItemsViewModel.codDeposito
        if (ItemsViewModel.codDireccion == null || ItemsViewModel.codDireccion == "null") {
            holder.tvOrigen.text = "Sin dirección"
        } else {
            holder.tvOrigen.text = ItemsViewModel.codDireccion
        }
        holder.tvArticulo.text = ItemsViewModel.codArticulo
        holder.tvDireccion.text = ItemsViewModel.codDireccionDes
        holder.tvDeposito.text = ItemsViewModel.codDepositoEnt
        holder.tvCausa.text = ItemsViewModel.codCausa
        holder.tvCantidad.text = ItemsViewModel.cantidad



        holder.llBack2.setOnClickListener {
            TransferenciaDeposito.posicionDetalleTransferencia = position
            this.notifyDataSetChanged()
        }

        holder.ibtnEliminar.setOnClickListener {

            TransferenciaDeposito.posicionDetalleTransferencia = position
            TransferenciaDeposito.eliminarDetalleTransferencia()

        }


        if (position%2==0){
            holder.llBack.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llBack.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (TransferenciaDeposito.posicionDetalleTransferencia == position) {
            holder.llBack.setBackgroundColor(Color.BLUE)
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
        val llBack: LinearLayout = itemView.findViewById(R.id.llBack)
        val llBack2: LinearLayout = itemView.findViewById(R.id.llBack2)
        val llBack3: LinearLayout = itemView.findViewById(R.id.llBack3)
        val ibtnEliminar: ImageButton = itemView.findViewById(R.id.ibtnEliminar)
        val tvOrden: TextView = itemView.findViewById(R.id.tvOrden)
        val tvDepositoOrigen: TextView = itemView.findViewById(R.id.tvDepositoOrigen)
        val tvOrigen: TextView = itemView.findViewById(R.id.tvOrigen)
        val tvArticulo: TextView = itemView.findViewById(R.id.tvArticulo)
        val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        val tvDeposito: TextView = itemView.findViewById(R.id.tvDeposito)
        val tvCausa: TextView = itemView.findViewById(R.id.tvCausa)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)


    }


}