package com.apolo.wms.utilidades

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.android.synthetic.main.entrada_redireccion_ub.*


class FuncionesUtiles {


    //Variables
    companion object{
        var posicionCabecera: Int = 0
        var posicionDetalle : Int = 0


        fun condensaFecha(viejaFecha: String): String {
            var nuevaFecha = ""
            var _dd = ""
            var _mm = ""
            var _aa = ""
            val mSinBarra = viejaFecha.split("/").toTypedArray()
            for (i in mSinBarra.indices) {
                if (i == 0) {
                    _dd = mSinBarra[i]
                } else if (i == 1) {
                    _mm = mSinBarra[i]
                } else {
                    _aa = mSinBarra[i]
                    if (_aa.length > 2) {
                        _aa = _aa.substring(2, 4)
                    }
                }
            }
            nuevaFecha = _dd + _mm + _aa
            return nuevaFecha
        }


        fun condensaFecha2(viejaFecha: String): String {
            var nuevaFecha = viejaFecha
            var _dd = ""
            var _mm = ""
            var _aa = ""
            val mSinBarra = viejaFecha.split("/").toTypedArray()
            val dimSinBarra = mSinBarra.size
            if (dimSinBarra > 1) {
                for (i in mSinBarra.indices) {
                    if (i == 0) {
                        _dd = mSinBarra[i]
                    } else if (i == 1) {
                        _mm = mSinBarra[i]
                    } else {
                        _aa = mSinBarra[i]
                        if (_aa.length > 2) {
                            _aa = _aa.substring(2, 4)
                        }
                    }
                }
                nuevaFecha = _dd + _mm + _aa
            }
            return nuevaFecha
        }


        fun limitarDecimales(cantDecimales: Int, etCantidad: EditText) {

            etCantidad.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().contains(".") ) {

                        var texto = s.toString().split(".")
                        if (texto.size == 2) {

                            if (texto[1].toString().length > cantDecimales) {

                                etCantidad.setText("${texto[0]}.${texto[1].substring(0,cantDecimales)}")
                                etCantidad.setSelection(etCantidad.text.toString().length)

                            }

                        }

                    }

                    if (s.toString().contains(",") ) {

                        var texto = s.toString().split(",")
                        if (texto.size == 2) {

                            if (texto[1].toString().length > cantDecimales) {

                                etCantidad.setText("${texto[0]},${texto[1].substring(0,cantDecimales)}")
                                etCantidad.setSelection(etCantidad.text.toString().length)

                            }

                        }

                    }

                }
            })

        }



    }







}