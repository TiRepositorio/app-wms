package com.apolo.wms.operaciones.reabastecimiento.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.clases.reabastecimiento.DireccionReabastecimiento
import com.apolo.wms.operaciones.entrada.EntradaMercaderia
import com.apolo.wms.operaciones.reabastecimiento.ReabastecimientoMarcaderiaDriveInNew
import com.apolo.wms2.R

class AdapterDireccionReabastecimiento (private val context: Context,
                                        private val dataSource: List<DireccionReabastecimiento>,
                                        private val molde: Int ) : RecyclerView.Adapter<AdapterDireccionReabastecimiento.ViewHolder>() {


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

        holder.tvDireccion.text = ItemsViewModel.codDireccionDes
        holder.tvCantidadDisp.text = ItemsViewModel.cantidad


        holder.llback2.setOnClickListener {
            ReabastecimientoMarcaderiaDriveInNew.posicionDireccion = position
            this.notifyDataSetChanged()
        }


        holder.ibEliminar.setOnClickListener {

            ReabastecimientoMarcaderiaDriveInNew.posicionDireccion = position

            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Atencion")
                .setContentText("¿Desea Borrar Direccion ${ItemsViewModel.codDireccionDes}?")
                .setConfirmText("Si")
                .setConfirmClickListener { sDialog ->

                    ReabastecimientoMarcaderiaDriveInNew.eliminaReabastDet(ItemsViewModel.nroOrden);
                    ReabastecimientoMarcaderiaDriveInNew.cargaReabastecimientoDirecciones()
                    sDialog.dismissWithAnimation()

                }
                .setCancelButton(
                    "No"
                ) { sDialog -> sDialog.dismissWithAnimation() }
                .show()

        }


        if (position%2==0){
            holder.llback.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llback.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (ReabastecimientoMarcaderiaDriveInNew.posicionDireccion == position) {
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
        val llback2: LinearLayout = itemView.findViewById(R.id.llBack2)
        val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        val tvCantidadDisp: TextView = itemView.findViewById(R.id.tvCantidadDisp)
        val ibEliminar: ImageButton = itemView.findViewById(R.id.ibtnEliminar)





    }


}
