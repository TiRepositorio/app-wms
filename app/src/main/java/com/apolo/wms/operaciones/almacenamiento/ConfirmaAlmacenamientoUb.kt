package com.apolo.wms.operaciones.almacenamiento

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.almacenamiento.DetalleArticuloAlmacenamiento
import com.apolo.wms.clases.almacenamiento.DireccionAlmacenamiento
import com.apolo.wms.clases.almacenamiento.UnidadMedidaArticuloAlmacenamiento
import com.apolo.wms.clases.almacenamiento.VencimientoArticuloAlmacenamiento
import com.apolo.wms.utilidades.CallableWS
import com.apolo.wms.utilidades.ExecutorRunner
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.entrada_conferencia.*
import kotlinx.android.synthetic.main.entrada_redireccion_ub.*
import kotlinx.android.synthetic.main.entrada_redireccion_ub.btnCancelar
import kotlinx.android.synthetic.main.entrada_redireccion_ub.btnConfirmar
import kotlinx.android.synthetic.main.entrada_redireccion_ub.etCantidad
import kotlinx.android.synthetic.main.entrada_redireccion_ub.etDestino
import kotlinx.android.synthetic.main.entrada_redireccion_ub.etDesvioFocus
import kotlinx.android.synthetic.main.entrada_redireccion_ub.etLote
import kotlinx.android.synthetic.main.entrada_redireccion_ub.etVencimiento
import kotlinx.android.synthetic.main.entrada_redireccion_ub.spDirecciones
import kotlinx.android.synthetic.main.entrada_redireccion_ub.tvDescArticulo
import kotlinx.android.synthetic.main.entrada_redireccion_ub.tvNorma
import kotlinx.android.synthetic.main.lector_codigo_trasnferencia.btnAceptar
import kotlinx.android.synthetic.main.lector_codigo_trasnferencia.btn_volver
import kotlinx.android.synthetic.main.lector_codigo_trasnferencia.etCodigoBarra
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import kotlin.math.floor


class ConfirmaAlmacenamientoUb : AppCompatActivity() {

    companion object {

        lateinit var context : Context

    }

    private lateinit var dialogo_lee_codigo: Dialog

    var detalleArticuloAlmacenamiento = ArrayList<DetalleArticuloAlmacenamiento>()

    var vencimientoArticuloAlmacenamiento = ArrayList<VencimientoArticuloAlmacenamiento>()
    var posicionVencimiento = 0
    var umArticuloAlmacenamiento = ArrayList<UnidadMedidaArticuloAlmacenamiento>()
    var posicionUM = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entrada_redireccion_ub)

        inicializar()

    }


    private fun inicializar() {

        title = "ALMACENAMIENTO"
        tvJaula.text = ""
        context = this


        btnConfirmar.setOnClickListener { confirmarAlmacenamiento() }
        btnCancelar.setOnClickListener{ cancelarAlmacenamiento() }

        etDestino.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n") > -1) {
                    etDesvioFocus.requestFocus()
                }

            }
        })
        etDestino.setOnFocusChangeListener { view, _ ->

            if (!view.hasFocus()) {
                buscaDetalleArticulo()

            }

        }

        etDesvioFocus.setOnFocusChangeListener { view, _ ->

            if (view.hasFocus()) {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etDesvioFocus.windowToken, 0)
            }

        }


        FuncionesUtiles.limitarDecimales(MainActivity.maximoDecimales, etCantidad)
/*
        etCantidad.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().contains(".") ) {

                    var texto = s.toString().split(".")
                    if (texto.size == 2) {

                        if (texto[1].toString().length > 3) {

                            etCantidad.setText("${texto[0]}.${texto[1].substring(0,3)}")
                            etCantidad.setSelection(etCantidad.text.toString().length)

                        }

                    }

                }

                if (s.toString().contains(",") ) {

                    var texto = s.toString().split(",")
                    if (texto.size == 2) {

                        if (texto[1].toString().length > 3) {

                            etCantidad.setText("${texto[0]},${texto[1].substring(0,3)}")
                            etCantidad.setSelection(etCantidad.text.toString().length)

                        }

                    }

                }

            }
        })*/

    }

    private fun buscaDetalleArticulo() {

        etDestino.setText(etDestino.text.toString().replace("\n", ""))

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_BARRA", etDestino.text.toString())
            .build()

        executorRunner.execute(
            CallableWS("almacenamiento/busca_detalle_articulo", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    tvDescArticulo.text = ""
                    etCantidad.inputType = InputType.TYPE_CLASS_NUMBER


                    detalleArticuloAlmacenamiento = ArrayList()
                    var respuestaJson: JSONObject


                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscaDetalleArticulo) Error ${e.message.toString()} !")

                        return
                    }


                    if (respuestaJson.has("rows")) {
                        val detalleArticuloAlmacenamientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
                        var direccion = ""
                        var direccionCaja = ""

                        for (i in 0 until detalleArticuloAlmacenamientoArray.length()) {
                            val detalleArticuloAlmacenamientoObject : JSONObject = detalleArticuloAlmacenamientoArray.get(i) as JSONObject
                            val da = DetalleArticuloAlmacenamiento()
                            da.codArticulo = detalleArticuloAlmacenamientoObject.get("COD_ARTICULO").toString()
                            da.descArticulo = detalleArticuloAlmacenamientoObject.get("DESCRIPCION").toString()
                            da.indManejaVto = detalleArticuloAlmacenamientoObject.get("IND_MANEJA_VTO").toString()
                            da.unidad = detalleArticuloAlmacenamientoObject.get("UNIDAD").toString()
                            da.codDeposito = detalleArticuloAlmacenamientoObject.get("COD_DEPOSITO").toString()
                            da.codDireccion = detalleArticuloAlmacenamientoObject.get("COD_DIRECCION").toString()
                            da.codDireccionCaja = detalleArticuloAlmacenamientoObject.get("COD_DIRECCION_CAJA").toString()
                            da.artAdicional = detalleArticuloAlmacenamientoObject.get("ART_ADICIONAL").toString()
                            da.esPesable = detalleArticuloAlmacenamientoObject.get("ES_PESABLE").toString()

                            detalleArticuloAlmacenamiento.add(da)


                            if (da.codDireccion.isNotEmpty()) {
                                direccion = da.codDireccion.substring(0, 3)+"-"+da.codDireccion.substring(3, 6)+"-"+da.codDireccion.substring(6, 7)+"-"+da.codDireccion.subSequence(7, 9)
                            }

                            if (da.codDireccionCaja.isNotEmpty()) {
                                direccionCaja = da.codDireccionCaja.substring(0, 3)+"-"+da.codDireccionCaja.substring(3, 6)+"-"+da.codDireccionCaja.substring(6, 7)+"-"+da.codDireccionCaja.subSequence(7, 9)
                            }

                            tvDireccion.text = "(COD:${da.codArticulo}) DIR(UND): $direccion"
                            tvDireccion2.text = "                      DIR(CAJA): $direccionCaja"
                            tvDescArticulo.text = da.descArticulo

                        }


                        if (detalleArticuloAlmacenamiento.size > 0) {

                            val da = detalleArticuloAlmacenamiento[detalleArticuloAlmacenamiento.size - 1]


                            if (direccionCaja != "" || direccion != "") {
                                cargarVencimientoSP(da)
                            }

                            if (da.unidad != "") {
                                cargarUnidadMedidaSP(da)
                            }

                            if (da.esPesable == "S") {
                                etCantidad.inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                            }

                        }

                        var c = 0
                        if (etDestino.text.toString().indexOf(" ") > -1) {
                            c = 1
                            etDestino.setText(etDestino.text.toString().replace(" ", ""))
                            etDestino.setSelection(etDestino.text.toString().length)
                        }


                        if (c == 1) {



                            if (detalleArticuloAlmacenamiento.size > 0) {

                                val da = detalleArticuloAlmacenamiento[detalleArticuloAlmacenamiento.size - 1]

                                etCantidad.requestFocus()
                                if (da.indManejaVto == "S") {
                                    etLote.isEnabled = true
                                } else {
                                    etLote.isEnabled = true
                                }

                            } else {

                                cancelarAlmacenamiento()
                                MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ningun articulo con el codigo ingresado")

                            }

                        } else {

                            etLote.setText("")
                            etVencimiento.setText("")

                            if (detalleArticuloAlmacenamiento.size > 0) {

                                val da = detalleArticuloAlmacenamiento[detalleArticuloAlmacenamiento.size - 1]

                                etCantidad.requestFocus()
                                if (da.indManejaVto == "S") {
                                    etLote.isEnabled = true
                                    etVencimiento.isEnabled = false
                                } else {
                                    etLote.isEnabled = true
                                    etVencimiento.isEnabled = false
                                    etLote.setText(MainActivity.lote_defecto)
                                    etVencimiento.setText(MainActivity.fec_vencimiento_defecto)
                                }

                            } else {

                                etLote.isEnabled = true
                                etVencimiento.isEnabled = false
                                etLote.setText(MainActivity.lote_defecto)
                                etVencimiento.setText(MainActivity.fec_vencimiento_defecto)

                            }

                        }

                        if(tvDescArticulo.text.toString().trim() == ""){

                            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Atencion")
                                .setContentText("ARTÍCULO NO LOCALIZADO!!")
                                .setConfirmText("OK")
                                .setConfirmClickListener { sDialog ->

                                    cancelarAlmacenamiento()
                                    etDestino.requestFocus()
                                    sDialog.dismissWithAnimation()

                                }
                                .show()

                        }



                    } else {

                        cancelarAlmacenamiento()

                    }


                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })



    }

    private fun cargarUnidadMedidaSP(articulo: DetalleArticuloAlmacenamiento) {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", articulo.codArticulo)
            .build()

        executorRunner.execute(
            CallableWS("almacenamiento/busca_unidad_medida_articulo", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task


                    posicionUM = 0
                    umArticuloAlmacenamiento = ArrayList()
                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cargarUnidadMedidaSP) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("rows")) {
                        val umArticuloAlmacenamientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


                        for (i in 0 until umArticuloAlmacenamientoArray.length()) {
                            val umArticuloAlmacenamientoObject : JSONObject = umArticuloAlmacenamientoArray.get(i) as JSONObject
                            val um = UnidadMedidaArticuloAlmacenamiento()
                            um.codUnidadRel = umArticuloAlmacenamientoObject.get("COD_UNIDAD_REL").toString()
                            um.referencia = umArticuloAlmacenamientoObject.get("REFERENCIA").toString()
                            um.mult = umArticuloAlmacenamientoObject.get("MULT").toString()
                            um.indBasico = umArticuloAlmacenamientoObject.get("IND_BASICO").toString()
                            um.lastro = umArticuloAlmacenamientoObject.get("LASTRO").toString()
                            um.capas = umArticuloAlmacenamientoObject.get("CAPAS").toString()

                            umArticuloAlmacenamiento.add(um)



                        }

                    }

                    val spinnerAdapter : ArrayAdapter<UnidadMedidaArticuloAlmacenamiento> =
                        ArrayAdapter(context, R.layout.spinner_adapter, umArticuloAlmacenamiento)
                    spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    spDirecciones.adapter = spinnerAdapter



                    spDirecciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                            posicionUM = position


                            if (umArticuloAlmacenamiento[posicionUM].lastro != "") {

                                try {

                                    var aux = floor(vencimientoArticuloAlmacenamiento[posicionVencimiento].cantDisponible.toDouble() / umArticuloAlmacenamiento[posicionUM].mult.toDouble())

                                    tvNorma.text =  "NORMA  LASTRO: ${umArticuloAlmacenamiento[posicionUM].lastro}" +
                                            " X ALTO:${umArticuloAlmacenamiento[posicionUM].capas}" +
                                            " (${aux.toInt()})"

                                } catch (e : Exception) {

                                    tvNorma.text = " - "

                                }


                            } else {
                                tvNorma.text = " - "
                            }



                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) { }
                    }




                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })

    }


    private fun cargarVencimientoSP(articulo: DetalleArticuloAlmacenamiento) {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", articulo.codArticulo)
            .build()

        executorRunner.execute(
            CallableWS("almacenamiento/busca_vencimiento_articulo", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task


                    posicionVencimiento = 0
                    vencimientoArticuloAlmacenamiento = ArrayList()
                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cargarVencimientoSP) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("rows")) {
                        val vencimientoArticuloAlmacenamientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


                        for (i in 0 until vencimientoArticuloAlmacenamientoArray.length()) {
                            val vencimientoArticuloAlmacenamientoObject : JSONObject = vencimientoArticuloAlmacenamientoArray.get(i) as JSONObject
                            val va = VencimientoArticuloAlmacenamiento()
                            va.codArticulo = articulo.codArticulo
                            va.fecVencimiento = vencimientoArticuloAlmacenamientoObject.get("FEC_VENCIMIENTO").toString()
                            va.cantDisponible = vencimientoArticuloAlmacenamientoObject.get("CANT_DISP").toString()

                            vencimientoArticuloAlmacenamiento.add(va)



                        }


                    }

                    val spinnerAdapter : ArrayAdapter<VencimientoArticuloAlmacenamiento> =
                        ArrayAdapter(context, R.layout.spinner_adapter, vencimientoArticuloAlmacenamiento)
                    spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    spVencimiento.adapter = spinnerAdapter


                    spVencimiento.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                            try {
                                etVencimiento.setText(FuncionesUtiles.condensaFecha(vencimientoArticuloAlmacenamiento[position].fecVencimiento))
                            } catch (e: Exception) {
                                etVencimiento.setText(vencimientoArticuloAlmacenamiento[position].fecVencimiento)
                            }

                            etSaldoDisponible.setText(vencimientoArticuloAlmacenamiento[position].cantDisponible)


                            var miValor: String = etSaldoDisponible.text.toString().trim()
                            if (miValor.trim().length == 0) {
                                miValor = "0"
                            }

                            posicionVencimiento = position


                            try {
                                if (umArticuloAlmacenamiento.size > 0) {

                                    if (umArticuloAlmacenamiento[posicionUM].lastro != "") {

                                        var aux = floor(vencimientoArticuloAlmacenamiento[position].cantDisponible.toDouble() / umArticuloAlmacenamiento[posicionUM].mult.toDouble())

                                        tvNorma.text = "NORMA  LASTRO: ${umArticuloAlmacenamiento[posicionUM].lastro}" +
                                                " X ALTO:${umArticuloAlmacenamiento[posicionUM].capas}" +
                                                " (${aux.toInt()})"
                                    } else {
                                        tvNorma.text = " - "
                                    }

                                }
                            } catch (e: Exception) {

                                spDirecciones.setSelection(posicionUM)

                            }

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) { }
                    }

                    if (vencimientoArticuloAlmacenamiento.size > 0) {
                        posicionVencimiento = vencimientoArticuloAlmacenamiento.size - 1
                        spVencimiento.setSelection(posicionVencimiento)




                    } else {

                        etSaldoDisponible.setText("0")

                    }





                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })

    }


    private fun confirmarAlmacenamiento() {



        if (detalleArticuloAlmacenamiento.size == 0) {
            MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ningun articulo con el codigo ingresado")
            tvDescArticulo.text = "0"
            return
        }

        if (umArticuloAlmacenamiento.size == 0) {
            MainActivity.funciones.mensajeError(context, "Atencion", "No existe ninguna unidad de medida para este articulo")
            tvDescArticulo.text = "0"
            return
        }

        if (vencimientoArticuloAlmacenamiento.size == 0) {
            MainActivity.funciones.mensajeError(context, "Atencion", "No existe ninguna fecha de vencimiento con stock")
            tvDescArticulo.text = "0"
            return
        }

        val da = detalleArticuloAlmacenamiento[detalleArticuloAlmacenamiento.size - 1]
        val um = umArticuloAlmacenamiento[posicionUM]
        val venArt = vencimientoArticuloAlmacenamiento[posicionVencimiento]
        var cantidadConferencia = 0.0
        try {
            cantidadConferencia = etCantidad.text.toString().toDouble()
        } catch (e: java.lang.Exception) {
        }

        if (cantidadConferencia <= 0) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Debe ingresar una cantidad mayor que 0")
            return
        }

        var fec: String = FuncionesUtiles.condensaFecha2(etVencimiento.text.toString())


        if(fec.length == 6){

            try {
                val dfDate = SimpleDateFormat("dd/MM/yyyy")
                dfDate.isLenient = false
                fec = fec.substring(0, 2) + "/" + fec.substring(2, 4) + "/20" + fec.substring(4, 6)
                dfDate.parse(fec)
                etVencimiento.setText(fec)
            } catch (e: Exception) {

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atencion")
                    .setContentText("FECHA INGRESADA INCORRECTA")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        etVencimiento.requestFocus()
                        sDialog.dismissWithAnimation()

                    }
                    .show()
                return

            }

        } else {

            if(da.indManejaVto == "S"){

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atencion")
                    .setContentText("FECHA INGRESADA INCORRECTA")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        etVencimiento.requestFocus()
                        sDialog.dismissWithAnimation()

                    }
                    .show()
                return

            }

        }


        var multiplo = 1.0
        try {
            multiplo = um.mult.trim().toDouble()
        } catch (e: java.lang.Exception) {
        }

        var cantDisponible = 0.0
        try {
            cantDisponible = venArt.cantDisponible.trim().toDouble()
        } catch (e: java.lang.Exception) {
        }

        var totales = 0.0
        try {
            totales = cantDisponible / multiplo
        } catch (e: java.lang.Exception) {
        }


        var cantidad = 0.0
        try {
            cantidad = etCantidad.text.toString().toDouble()
        } catch (e: java.lang.Exception) {
        }

        if (cantidad <= totales) {

            val executorRunner = ExecutorRunner()
            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_DEPOSITO", MainActivity.usuarioLogin.codDepositoVerde)
                .add("COD_ARTICULO", da.codArticulo)
                .add("COD_UNIDAD_MEDIDA", um.codUnidadRel)
                .add("CANTIDAD", cantidad.toString().replace(".", ","))
                .build()

            executorRunner.execute(
                CallableWS("almacenamiento/valida_existe_deposito_origen", formBody),
                object : ExecutorRunner.Callback<String> {
                    override fun onComplete(result: String) { // handle the result obtained from the asynchronous task



                        var respuestaJson: JSONObject

                        try {
                            respuestaJson = JSONObject(result)
                        } catch (e: Exception) {
                            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_existe_deposito_origen) Error ${e.message.toString()} !")

                            return
                        }

                        if (respuestaJson.has("respuesta")) {
                            val respuesta = respuestaJson.get("respuesta").toString()

                            if (respuesta.trim() == "S") {

                                buscarDestino()

                            } else {

                                MainActivity.funciones.mensajeError(context, "Atencion", "No se encuentra cantidad suficiente en direccion de origen")

                            }

                        }




                    }

                    override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                        if (e != null) {
                            MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                        } else {
                            MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                        }
                    }
                })


        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "El saldo disponible es insuficiente para esta fecha.")


        }

    }



    fun buscarDestino() {


        try {
            dialogo_lee_codigo.dismiss()
        } catch (e: Exception) {
        }
        dialogo_lee_codigo = Dialog(context)
        dialogo_lee_codigo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_lee_codigo.setContentView(R.layout.lector_codigo_trasnferencia)



        dialogo_lee_codigo.etCodigoBarra.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus){
                buscaDestinoFuncion()
            }
        }

        dialogo_lee_codigo.etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n") > -1) {
                    dialogo_lee_codigo.etDesvioFocus.requestFocus()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        dialogo_lee_codigo.btnAceptar.text = "Aceptar"


        dialogo_lee_codigo.btnAceptar.setOnClickListener {
            buscaDestinoFuncion()
        }

        dialogo_lee_codigo.btn_volver.setOnClickListener {
            dialogo_lee_codigo.dismiss()
        }


        dialogo_lee_codigo.setCancelable(false)
        dialogo_lee_codigo.show()



    }


    fun buscaDestinoFuncion() {


        dialogo_lee_codigo.etCodigoBarra.setText(dialogo_lee_codigo.etCodigoBarra.text.toString().replace("\n", ""))
        dialogo_lee_codigo.etCodigoBarra.setSelection(dialogo_lee_codigo.etCodigoBarra.text.toString().length)



        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", dialogo_lee_codigo.etCodigoBarra.text.toString())
            .build()

        executorRunner.execute(
            CallableWS("almacenamiento/valida_direccion_apm", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_direccion_apm) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("respuesta")) {
                        val respuesta = respuestaJson.get("respuesta").toString()

                        if (respuesta.trim() == "X") {


                            consultaDireccion()



                        } else {

                            MainActivity.funciones.mensajeError(context, "Atencion", respuesta)

                        }

                    }

                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })


    }


    fun consultaDireccion() {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", dialogo_lee_codigo.etCodigoBarra.text.toString())
            .build()

        executorRunner.execute(
            CallableWS("almacenamiento/consulta_direccion", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    val dir = DireccionAlmacenamiento()

                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_direccion) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("rows")) {
                        val direccionAlmacenamientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

                        for (i in 0 until direccionAlmacenamientoArray.length()) {
                            val direccionAlmacenamientoObject : JSONObject = direccionAlmacenamientoArray.get(i) as JSONObject

                            dir.codDireccion = dialogo_lee_codigo.etCodigoBarra.text.toString()
                            dir.esPicking = direccionAlmacenamientoObject.get("ES_PICKING").toString()
                            dir.codTipoAlm = direccionAlmacenamientoObject.get("COD_TIPO_ALM").toString()


                        }

                        //isPickingOdrive
                        if (dir.esPicking == "S" && dir.codTipoAlm == "02") {

                            validaPickingArticulo()

                        } else {

                            //isPulmonOdrive
                            if (dir.esPicking == "N" && dir.codTipoAlm == "02") {

                                validaVencimiento(etVencimiento.text.toString())

                            } else {

                                //isPicking
                                if (dir.esPicking == "S") {
                                    validaPickingArticulo()

                                } else {

                                    validaExistenciaDireccion(dialogo_lee_codigo.etCodigoBarra.text.toString())

                                }

                            }

                        }


                    }



                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })

    }

    fun validaExistenciaDireccion(codDireccion : String) {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", codDireccion)
            .build()

        executorRunner.execute(
            CallableWS("almacenamiento/valida_existencia_direccion", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task


                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (validaExistenciaDireccion) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("respuesta")) {
                        val respuesta = respuestaJson.get("respuesta").toString()

                        if (respuesta.trim() == "N") {

                            procesaEntradaAlmRedir()


                        } else {

                            MainActivity.funciones.mensajeError(context, "Atencion", "DIRECCION DE DESTINO CON EXISTENCIA")

                        }

                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaExistenciaDireccion)")
                    }


                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })

    }

    fun validaVencimiento(fecVencimiento : String) {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", dialogo_lee_codigo.etCodigoBarra.text.toString())
            .build()

        executorRunner.execute(
            CallableWS("almacenamiento/valida_vencimiento", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    val vaa = VencimientoArticuloAlmacenamiento()

                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (validaVencimiento) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("rows")) {



                        val vencimientoArticuloAlmacenamientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

                        if (vencimientoArticuloAlmacenamientoArray.length() > 0) {

                            for (i in 0 until vencimientoArticuloAlmacenamientoArray.length()) {
                                val vencimientoArticuloAlmacenamientoObject : JSONObject = vencimientoArticuloAlmacenamientoArray.get(0) as JSONObject

                                vaa.codArticulo = vencimientoArticuloAlmacenamientoObject.get("COD_ARTICULO").toString()
                                vaa.fecVencimiento = vencimientoArticuloAlmacenamientoObject.get("FEC_VENCIMIENTO").toString()
                                vaa.cantDisponible = vencimientoArticuloAlmacenamientoObject.get("CANT_DISP").toString()
                                vaa.codDireccion = vencimientoArticuloAlmacenamientoObject.get("COD_DIRECCION").toString()


                            }

                            val fecha: String =
                                vaa.fecVencimiento.substring(8, 10).toString() + "/" +
                                        vaa.fecVencimiento.substring(5, 7) + "/" +
                                        vaa.fecVencimiento.substring(0, 4)


                            if (fecha == fecVencimiento.trim()) {
                                MainActivity.funciones.mensajeExito(context, "Vencimiento", "OK")
                                procesaEntradaAlmRedir()
                            }else{

                                MainActivity.funciones.mensajeError(context, "Verificar fecha de vencimiento!", "La fecha de vencimiento seleccionada no coincide con la fecha de vencimiento de los articulos en esta dirección.")

                            }

                        } else {

                            procesaEntradaAlmRedir()

                        }





                    } else {

                        MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaVencimiento)")

                    }

                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })

    }

    fun validaPickingArticulo() {


        val articulo = detalleArticuloAlmacenamiento[detalleArticuloAlmacenamiento.size - 1]

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", MainActivity.usuarioLogin.codDepositoVerde)
            .add("COD_DIRECCION", dialogo_lee_codigo.etCodigoBarra.text.toString())
            .add("COD_ARTICULO", articulo.codArticulo)
            .build()

        executorRunner.execute(
            CallableWS("almacenamiento/valida_picking_articulo", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (validaPickingArticulo) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("respuesta")) {
                        val respuesta = respuestaJson.get("respuesta").toString()

                        if (respuesta.trim() == "S") {


                            procesaEntradaAlmRedir()



                        } else {

                            MainActivity.funciones.mensajeError(context, "Atencion", "DIRECCION NO VALIDA PARA ARTICULO EN PICKING")

                        }

                    }



                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })


    }


    fun procesaEntradaAlmRedir() {

        val da = detalleArticuloAlmacenamiento[detalleArticuloAlmacenamiento.size - 1]
        val um = umArticuloAlmacenamiento[posicionUM]

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", MainActivity.usuarioLogin.codDepositoVerde)
            .add("COD_ARTICULO", da.codArticulo)
            .add("COD_UNIDAD_MEDIDA", um.codUnidadRel)
            .add("CANTIDAD", etCantidad.text.toString().trim().replace(".", ","))
            .add("FEC_VENCIMIENTO", etVencimiento.text.toString().trim())
            .add("COD_DIRECCION", dialogo_lee_codigo.etCodigoBarra.text.toString())
            .build()

        executorRunner.execute(
            CallableWS("almacenamiento/procesa_entrada_alm_redir_act", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task


                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (procesa_entrada_alm_redir_act) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("respuesta")) {
                        val respuesta = respuestaJson.get("respuesta").toString()

                        SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Atencion")
                            .setContentText(respuesta)
                            .setConfirmText("OK")
                            .setConfirmClickListener { sDialog ->

                                cancelarAlmacenamiento()
                                etDestino.requestFocus()
                                sDialog.dismissWithAnimation()

                            }
                            .show()


                        try {
                            dialogo_lee_codigo.dismiss()
                            cancelarAlmacenamiento()
                        } catch (e: java.lang.Exception) {
                        }

                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (procesaEntradaAlmRedir)")
                    }


                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })


    }



    private fun cancelarAlmacenamiento() {
        /*opCancelar = 1; */

        tvDescArticulo.text = ""
        tvNorma.text = ""

        etDestino.setText("")
        etCantidad.setText("")
        etVencimiento.setText("")
        etLote.setText("")

        etDestino.requestFocus()
        tvDireccion.text = ""
        tvDireccion2.text = ""

        etSaldoDisponible.setText("")
        spDirecciones.adapter = null
        spVencimiento.adapter = null


        detalleArticuloAlmacenamiento = ArrayList()
        vencimientoArticuloAlmacenamiento = ArrayList()
        posicionVencimiento = 0
        umArticuloAlmacenamiento = ArrayList()
        posicionUM = 0



    }


}