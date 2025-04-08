package com.apolo.wms.utilidades

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*


class Adapter {

    class AdapterGenericoCabecera(private val context: Context,
                                  private val dataSource: ArrayList<*>,
                                  private val molde: Int,
                                  private val vistas: IntArray,
                                  private val valores: Array<String>) : BaseAdapter()
    {

        val colors = intArrayOf(Color.parseColor("#696969"), Color.parseColor("#808080"))

        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return dataSource.size
        }

        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rowView = inflater.inflate(molde, parent, false)

            /*for (i in vistas.indices){
                try {
                    rowView.findViewById<TextView>(vistas[i]).visibility = View.VISIBLE
                    rowView.findViewById<TextView>(vistas[i]).text= (dataSource[position] as ObtenerAtributoPorNombre).getFieldByString(valores[i])
                    //rowView.findViewById<TextView>(vistas[i]).setBackgroundResource(R.drawable.border_textview)
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }*/



            if (position%2==0){
                rowView.setBackgroundColor(Color.parseColor("#EEEEEE"))
            } else {
                rowView.setBackgroundColor(Color.parseColor("#CCCCCC"))
            }

            //rowView.setBackgroundColor(colors[position%2])

            //if (FuncionesUtiles.posicionCabecera == position){
                //rowView.setBackgroundColor(Color.parseColor("#aabbaa"))
                //rowView.setBackgroundColor(Color.BLUE)
            //}

            return rowView
        }

    }







}

