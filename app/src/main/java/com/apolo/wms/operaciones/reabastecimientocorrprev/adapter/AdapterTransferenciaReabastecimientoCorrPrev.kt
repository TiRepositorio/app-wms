package com.apolo.wms.operaciones.reabastecimientocorrprev.adapter

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
import com.apolo.wms.clases.reabastecimientocorrprev.TransferenciaReabastecimientoCorrPrev
import com.apolo.wms.operaciones.reabastecimientocorrprev.ReabastecimientoCorrPrev
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms2.R

class AdapterTransferenciaReabastecimientoCorrPrev (private val context: Context,
                                                    private val dataSource: List<TransferenciaReabastecimientoCorrPrev>,
                                                    private val molde: Int ) : RecyclerView.Adapter<AdapterTransferenciaReabastecimientoCorrPrev.ViewHolder>() {


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

        var dir_origen_aux: String = ItemsViewModel.dirOrigen
        if (dir_origen_aux.isNotEmpty()) {
            dir_origen_aux = if (ItemsViewModel.tipMov == "A") {

                dir_origen_aux

            } else {

                dir_origen_aux.substring(0, 3) + "-" + dir_origen_aux.substring(
                    3,
                    6
                ) + "-" + dir_origen_aux.substring(6, 7) + "-" + dir_origen_aux.subSequence(7, 9)

            }

        }

        holder.tvDirOrigen.text = dir_origen_aux


        var dir_destino_aux: String = ItemsViewModel.dirDestino
        if (dir_destino_aux.isNotEmpty()) {
            dir_destino_aux = dir_destino_aux.substring(0, 3) + "-" + dir_destino_aux.substring(
                3,
                6
            ) + "-" + dir_destino_aux.substring(6, 7) + "-" + dir_destino_aux.subSequence(7, 9)
        }

        holder.tvDirDestino.text = dir_destino_aux



        holder.tvCantidad.text = ItemsViewModel.cantidad
        holder.tvDescUnidadMedida.text = ItemsViewModel.descUnidadMedida
        holder.tvCodArticulo.text = ItemsViewModel.codArticulo
        holder.tvDescArticulo.text = ItemsViewModel.descArticulo

        if (ItemsViewModel.tipMov == "R") {
            /*holder.tvDirOrigen.setTextColor(Color.parseColor("#fffafa"))
            holder.tvDirDestino.setTextColor(Color.parseColor("#fffafa"))
            holder.tvCantidad.setTextColor(Color.parseColor("#fffafa"))
            holder.tvDescUnidadMedida.setTextColor(Color.parseColor("#fffafa"))
            holder.tvCodArticulo.setTextColor(Color.parseColor("#fffafa"))
            holder.tvDescArticulo.setTextColor(Color.parseColor("#fffafa"))*/
            holder.tvDirOrigen.setTextColor(Color.parseColor("#000000"))
            holder.tvDirDestino.setTextColor(Color.parseColor("#000000"))
            holder.tvCantidad.setTextColor(Color.parseColor("#000000"))
            holder.tvDescUnidadMedida.setTextColor(Color.parseColor("#000000"))
            holder.tvCodArticulo.setTextColor(Color.parseColor("#000000"))
            holder.tvDescArticulo.setTextColor(Color.parseColor("#000000"))
        } else {

            if (ItemsViewModel.tipMov == "A") {


                holder.tvDirOrigen.setTextColor(Color.parseColor("#008000"))
                holder.tvDirDestino.setTextColor(Color.parseColor("#008000"))
                holder.tvCantidad.setTextColor(Color.parseColor("#008000"))
                holder.tvDescUnidadMedida.setTextColor(Color.parseColor("#008000"))
                holder.tvCodArticulo.setTextColor(Color.parseColor("#008000"))
                holder.tvDescArticulo.setTextColor(Color.parseColor("#008000"))


            } else {

                holder.tvDirOrigen.setTextColor(Color.RED)
                holder.tvDirDestino.setTextColor(Color.RED)
                holder.tvCantidad.setTextColor(Color.RED)
                holder.tvDescUnidadMedida.setTextColor(Color.RED)
                holder.tvCodArticulo.setTextColor(Color.RED)
                holder.tvDescArticulo.setTextColor(Color.RED)

            }


        }


        holder.llback2.setOnClickListener {
            ReabastecimientoCorrPrev.posicionTransferencia = position
            this.notifyDataSetChanged()
        }

        holder.ibtnEliminar.setOnClickListener {

            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Atencion")
                .setContentText("¿Desea Borrar el articulo ${ItemsViewModel.dirOrigen}?")
                .setConfirmText("Si")
                .setConfirmClickListener { sDialog ->


                    if(ReabastecimientoCorrPrev.elimina_reabastcorprev_det(ItemsViewModel)){
                        try {
                            ReabastecimientoCorrPrev.obtieneListaTransferencias();
                        } catch (e: Exception) {
                            var aa = ""
                            aa = e.message.toString()
                        }
                    }


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

        if (ReabastecimientoCorrPrev.posicionTransferencia == position) {
            holder.llback.setBackgroundColor(Color.BLUE)

            holder.tvDirOrigen.setTextColor(Color.parseColor("#000000"))
            holder.tvDirDestino.setTextColor(Color.parseColor("#000000"))
            holder.tvCantidad.setTextColor(Color.parseColor("#000000"))
            holder.tvDescUnidadMedida.setTextColor(Color.parseColor("#000000"))
            holder.tvCodArticulo.setTextColor(Color.parseColor("#000000"))
            holder.tvDescArticulo.setTextColor(Color.parseColor("#000000"))

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
        val ibtnEliminar: ImageButton = itemView.findViewById(R.id.ibtnEliminar)
        val tvDirOrigen: TextView = itemView.findViewById(R.id.tvDirOrigen)
        val tvDirDestino: TextView = itemView.findViewById(R.id.tvDirDestino)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        val tvDescUnidadMedida: TextView = itemView.findViewById(R.id.tvDescUnidadMedida)
        val tvCodArticulo: TextView = itemView.findViewById(R.id.tvCodArticulo)
        val tvDescArticulo: TextView = itemView.findViewById(R.id.tvDescArticulo)




    }


}
