package com.apolo.wms.operaciones.inventario.adapter

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
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.inventario.ConferidosInventario
import com.apolo.wms.operaciones.inventario.ConfirmaInventario
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms2.R

class AdapterConferidoInventario (private val context: Context,
                                  private val dataSource: List<ConferidosInventario>,
                                  private val molde: Int ) : RecyclerView.Adapter<AdapterConferidoInventario.ViewHolder>() {


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



        holder.tvCodDireccion.text = ItemsViewModel.codDireccion
        holder.tvCodArticulo_act.text = ItemsViewModel.codArticulo01
        holder.tvDescArticulo_act.text = ItemsViewModel.descArticulo01
        holder.tvDescUnidadMedida_act.text = ItemsViewModel.referencia01
        holder.tvCantidad_act.text = ItemsViewModel.cantidad01
        holder.tvCodArticulo_conf.text = ItemsViewModel.codArticulo02
        holder.tvDescArticulo_conf.text = ItemsViewModel.descArticulo02
        holder.tvDescUnidadMedida_conf.text = ItemsViewModel.referencia02
        holder.tvCantidad_conf.text = ItemsViewModel.cantidad02


        holder.llBack2.setOnClickListener {
            ConfirmaInventario.posicionConferido = position
            this.notifyDataSetChanged()
        }


        holder.ibtnEliminar.setOnClickListener {

            if (MainActivity.usuarioLogin.codUsuario == "INV") {

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atencion")
                    .setContentText("¿Desea Borrar Conferencia de Articulo ${ItemsViewModel.codArticulo02} ?")
                    .setConfirmText("Si")
                    .setConfirmClickListener { sDialog ->

                        if (ConfirmaInventario.procesoEliminaConferencia(ItemsViewModel)) {

                            ConfirmaInventario.obtieneArticulosConferidosPlanilla()  //COMENTAR PARA VERSION INVENTARIO
                            ConfirmaInventario.obtieneInventarioDet()

                        }
                        sDialog.dismissWithAnimation()

                    }
                    .setCancelButton(
                        "No"
                    ) { sDialog -> sDialog.dismissWithAnimation() }
                    .show()

            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "No tiene permisos para eliminar la conferencia.")

            }

        }


        if (position%2==0){
            holder.llBack.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llBack.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (ConfirmaInventario.posicionConferido == position) {
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
        val ibtnEliminar: ImageButton = itemView.findViewById(R.id.ibtnEliminar)
        val tvCodDireccion: TextView = itemView.findViewById(R.id.tvCodDireccion)
        val tvCodArticulo_act: TextView = itemView.findViewById(R.id.tvCodArticulo_act)
        val tvDescArticulo_act: TextView = itemView.findViewById(R.id.tvDescArticulo_act)
        val tvDescUnidadMedida_act: TextView = itemView.findViewById(R.id.tvDescUnidadMedida_act)
        val tvCantidad_act: TextView = itemView.findViewById(R.id.tvCantidad_act)
        val tvCodArticulo_conf: TextView = itemView.findViewById(R.id.tvCodArticulo_conf)
        val tvDescArticulo_conf: TextView = itemView.findViewById(R.id.tvDescArticulo_conf)
        val tvDescUnidadMedida_conf: TextView = itemView.findViewById(R.id.tvDescUnidadMedida_conf)
        val tvCantidad_conf: TextView = itemView.findViewById(R.id.tvCantidad_conf)




    }


}