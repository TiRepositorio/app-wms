package com.apolo.wms.operaciones.reabastecimiento

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.Operaciones
import com.apolo.wms.clases.reabastecimiento.DireccionReabastecimiento
import com.apolo.wms.clases.reabastecimiento.SugerenciaPulmonReabastecimiento
import com.apolo.wms.clases.reabastecimiento.UnidadMedidaReabastecimiento
import com.apolo.wms.operaciones.almacenamiento.ConfirmaAlmacenamientoUb
import com.apolo.wms.operaciones.inventario.DetallePlanillaInventario
import com.apolo.wms.operaciones.inventario.DetallePlanillaInventarioG
import com.apolo.wms.operaciones.reabastecimiento.adapter.AdapterDireccionReabastecimiento
import com.apolo.wms.operaciones.reabastecimiento.adapter.AdapterSugerenciaPulmon
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.entrada_redireccion_ub.spDirecciones
import kotlinx.android.synthetic.main.lector_codigo_articulo_reabast_normal.btnAceptar
import kotlinx.android.synthetic.main.lector_codigo_articulo_reabast_normal.btn_volver
import kotlinx.android.synthetic.main.lector_codigo_articulo_reabast_normal.etCodigoBarra
import kotlinx.android.synthetic.main.lector_codigo_jaula_almacenamiento.*
import kotlinx.android.synthetic.main.lista_sugerencia_pulmon.*
import kotlinx.android.synthetic.main.reabastecimiento_ingresa_cant_fec.*
import kotlinx.android.synthetic.main.reabastecimiento_mercaderia_drive_in.*
import kotlinx.android.synthetic.main.reabastecimiento_mercaderia_drive_in.etDestino
import kotlinx.android.synthetic.main.reabastecimiento_mercaderia_drive_in.etDesvioFocus
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class ReabastecimientoMarcaderiaDriveInNew : AppCompatActivity() {


    companion object {
        lateinit var context : Context

        var operacionIniciada = 0

        var esDrive = false

        var direccionReabastecimiento = ArrayList<DireccionReabastecimiento>()
        var posicionDireccion = 0

        var umReabastecimiento = ArrayList<UnidadMedidaReabastecimiento>()
        var posicionUM = 0
        var posicionUMBasico = 0


        var articulosEnDireccion: ArrayList<String> = ArrayList()
        var articuloValidado = false
        var codArticuloVal : String = ""

        var sugerenciaPulmonReabastecimiento = ArrayList<SugerenciaPulmonReabastecimiento>()
        var posicionSugerenciaPulmon = 0




        var nreg_detalle_reabast = 0
        var tipo_operacion_reabast: String? = null
        var operacion_permitida = false

        var pickingRef = ""


        var nro_comprobante = ""
        var tip_comprobante = ""
        var ser_comprobante = ""
        var cod_deposito = ""
        var cod_articulo = ""
        var nro_lote = ""
        var fec_vencimiento = ""
        var cod_unidad_med = ""
        var cantidad_disp = "0"
        var cod_direccion_ori = ""
        var cantidad_transferida = "0"
        var direccionOrigen: String = ""


        var cursor_reabast: Cursor? = null

        var m_tip_operacion_rea = arrayOfNulls<String>(0)

        var tipo_operacion_iniciado: String? = null

        var fecha_vencimiento_PKPL: String? = null

        var ind_maneja_vto = "N"

        private lateinit var dialogo_lee_codigo: Dialog
        private lateinit var dialogo_inserta_cantidad: Dialog
        private lateinit var dialog_sugerencia_pulmon: Dialog

        private lateinit var rvReabastDireccion : RecyclerView

        private lateinit var tvDatos3 : TextView
        private lateinit var tvDatos : TextView
        private lateinit var etDestino : EditText


        fun cargaReabastecimientoDirecciones() {

            posicionDireccion = 0
            direccionReabastecimiento = ArrayList()

            cantidad_transferida = "0"
            var total_transferido = 0.0

            val nf: NumberFormat = NumberFormat.getInstance()
            nf.minimumFractionDigits = "0".toInt()
            nf.maximumFractionDigits = "0".toInt()



            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", tip_comprobante)
                .add("SER_COMPROBANTE", ser_comprobante)
                .add("NRO_COMPROBANTE", nro_comprobante)
                .build()

            var result = HttpRequest.call("", "reabastecimiento/carga_reabastecimiento_direcciones", formBody)

            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (carga_reabastecimiento_direcciones) Error ${e.message.toString()} !")
                return
            }

            if (respuestaJson.has("rows")) {
                val direccionReabastecimientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


                for (i in 0 until direccionReabastecimientoArray.length()) {
                    val direccionReabastecimientoObject : JSONObject = direccionReabastecimientoArray.get(i) as JSONObject

                    val dr = DireccionReabastecimiento()
                    dr.codDireccionDes = direccionReabastecimientoObject.get("COD_DIRECCION_DES").toString()
                    dr.cantidad = direccionReabastecimientoObject.get("CANTIDAD").toString()
                    dr.nroOrden = direccionReabastecimientoObject.get("NRO_ORDEN").toString()


                    total_transferido += direccionReabastecimientoObject.get("CANTIDAD").toString()
                        .toDouble()



                    direccionReabastecimiento.add(dr)

                }


            }


            cantidad_transferida = total_transferido.toString()
            nreg_detalle_reabast = direccionReabastecimiento.size


            val gridLayoutManager = GridLayoutManager(context, 1)

            rvReabastDireccion.layoutManager = gridLayoutManager
            rvReabastDireccion.itemAnimator = DefaultItemAnimator()
            rvReabastDireccion.setHasFixedSize(true)


            // this creates a vertical layout Manager
            rvReabastDireccion.layoutManager = LinearLayoutManager(context)


            val adapter = AdapterDireccionReabastecimiento(
                context,
                direccionReabastecimiento,
                R.layout.card_view_reabastecimiento_direccion  )

            // Setting the Adapter with the recyclerview

            rvReabastDireccion.adapter = adapter


            var _cant: String
            val _venc: String
            _cant = nf.format(cantidad_disp.toDouble() - cantidad_transferida.toDouble())
            _cant = cantidadDisponible().toString() + ""
            _cant = _cant.replace(",", "")
            _venc = if (fec_vencimiento == "31/12/2099") {
                "NULL"
            } else {
                fec_vencimiento
            }
            tvDatos3.text = "VENCIMIENTO:   $_venc"
            tvDatos.text = "CANT. DISP:   $_cant"


            etDestino.setText("")
            etDestino.requestFocus()


        }



        @SuppressLint("Range")
        fun cantidadDisponible() : Double {

            val sqlite = ("SELECT COD_DIRECCION_ORIGEN, FEC_VENCIMIENTO, ESTADO "
                    + "        FROM wms_transferencias_manuales "
                    + "       WHERE id = (SELECT MAX(id) FROM wms_transferencias_manuales)")
            try {
                val cursor = Operaciones.bdatos!!.rawQuery(sqlite, null)
                cursor.moveToFirst()
                direccionOrigen = cursor.getString(cursor.getColumnIndex("COD_DIRECCION_ORIGEN"))
            } catch (e: java.lang.Exception) {
                var err = e.message
                err = err
            }

            var cantidad_disponible = "0"

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_DEPOSITO", MainActivity.usuarioLogin.codDepositoVerde)
                .add("COD_DIRECCION", direccionOrigen)
                .add("COD_ARTICULO", cod_articulo)
                .add("NRO_LOTE", nro_lote)
                .add("FEC_VENCIMIENTO", fec_vencimiento)
                .build()

            var result = HttpRequest.call("", "reabastecimiento/cantidad_disponible", formBody)

            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cantidad_disponible) Error ${e.message.toString()} !")
                return 0.0
            }

            if (respuestaJson.has("rows")) {

                val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

                if (filas.length() > 0) {

                    for (i in 0 until filas.length()) {

                        val fila : JSONObject = filas[i] as JSONObject

                        cantidad_disponible = fila.get("CANT_DISP").toString()

                    }

                }

            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cantidadDisponible)")
            }


            return  (cantidad_disponible.toDouble())

        }


        fun eliminaReabastDet(nroOrden: String) {

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_DEPOSITO", cod_deposito)
                .add("TIP_COMPROBANTE", tip_comprobante)
                .add("SER_COMPROBANTE", ser_comprobante)
                .add("NRO_COMPROBANTE", nro_comprobante)
                .add("NRO_ORDEN", nroOrden)
                .build()

            var result = HttpRequest.call("", "reabastecimiento/elimina_reabast_det", formBody)

            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (elimina_reabast_det) Error ${e.message.toString()} !")
                return
            }

            if (respuestaJson.has("respuesta")) {
                val respuesta = respuestaJson.get("respuesta").toString()

                val res: List<String> = respuesta.split("*")
                var mensaje = "REGISTRO ELIMINADO "

                if (res[0].equals("01")) {

                    MainActivity.funciones.mensajeExito(context, "Atencion", mensaje)

                } else {
                    mensaje = respuesta
                    MainActivity.funciones.mensajeError(context, "Atencion", mensaje)
                }





            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (eliminaReabastDet)")

            }



        }



    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reabastecimiento_mercaderia_drive_in)

        inicializar()

    }


    private fun inicializar() {

        context = this

        Companion.rvReabastDireccion = rvReabastDireccion
        Companion.tvDatos3 = tvDatos3
        Companion.tvDatos = tvDatos
        Companion.etDestino = etDestino



        title = "Reabastecimiento".uppercase(Locale.getDefault())

        nreg_detalle_reabast 	= 0
        tipo_operacion_reabast 	= null
        operacion_permitida		= true

        ibtnBuscarSugerencia.setOnClickListener { abreSugerenciaPulmon() }
        btnConfirmar.setOnClickListener{ confirmar() }
        btnCancelar.setOnClickListener{ cancelar() }


        etDestino.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO) {

                //dialogo_lee_codigo.etDesvioFocus.requestFocus()
                etDestino.setText(etDestino.text.toString().replace("\n", ""))
                operacionValidaInicio()
                //return true
            }
            true
        })

        /*codigo de barra de producto*/
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
                etDestino.setText(etDestino.text.toString().replace("\n", ""))
                operacionValidaInicio()

            }


        }


        tvDatos.text = "-"
        tvDatos2.text = "-"
        tvDatos3.text = ""

        liDatos.visibility = View.GONE
        validarTodo()


        if (!operacion_permitida) {

            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Operación iniciada en otro equipo!")
                .setContentText("Es necesario terminar la operación iniciada "
                        + "en otro equipo para proseguir.")
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->

                    finish()
                    sDialog.dismissWithAnimation()

                }
                .show()

        }

    }

    fun validarTodo() {

        verificaReabastecimientoBase()
        obtenerTipoOperacionReabast(ser_comprobante, tip_comprobante, nro_comprobante)

        if (operacionIniciada === 1) {
            val nreg: Int = m_tip_operacion_rea.size
            if (nreg == 0) {
                operacion_permitida = false
                return
            }
            cantidad_disp = cantidadDisponible().toString() + ""
            var dir = ""
            val dir_aux = cod_direccion_ori
            val text2 = ("CANT. DISP.: "
                    + cantidad_disp
                    + "     VENC: "
                    + fec_vencimiento)
            if (dir_aux.length > 0) {
                dir = (dir_aux.substring(0, 3) + "-" + dir_aux.substring(3, 6)
                        + "-" + dir_aux.substring(6, 7) + "-"
                        + dir_aux.subSequence(7, 9))
            }
            tipo_operacion_iniciado = m_tip_operacion_rea[0]
            tvDatos2.text = "INICIADO: $dir_aux  ($cod_articulo) $tipo_operacion_iniciado"
            tvDatos.text = text2
            //			tvDatos.setText("CANTIDAD DISPONIBLE: " + cantidad_disp);
            liDatos.visibility = View.VISIBLE
            esDriveIn(cod_direccion_ori)
        } else {
            tvDatos.text = "-"
            tvDatos2.text = "-"
            tvDatos3.text = ""
            cantidad_disp = "0"
            pickingRef = ""
            // liDatos.setVisibility(View.GONE);
            etDestino.setText("")
            etDestino.requestFocus()
        }

        cargaReabastecimientoDirecciones()

    }


    fun verificaReabastecimientoBase() : Boolean {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/verifica_reabastecimiento_act", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (verifica_reabastecimiento) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)


            if (filas.length() > 0) {

                for (i in 0 until filas.length()) {
                    operacionIniciada = 1

                    val fila : JSONObject = filas[i] as JSONObject

                    nro_comprobante 	= fila.get("NRO_COMPROBANTE").toString()
                    tip_comprobante 	= fila.get("TIP_COMPROBANTE").toString()
                    ser_comprobante 	= fila.get("SER_COMPROBANTE").toString()
                    cod_deposito 		= fila.get("COD_DEPOSITO").toString()
                    cod_articulo 		= fila.get("COD_ARTICULO").toString()
                    nro_lote 			= fila.get("NRO_LOTE_RES").toString()
                    fec_vencimiento 	= fila.get("FEC_VENCIMIENTO_RES").toString()
                    cod_unidad_med 		= fila.get("COD_UNID_MED_RES").toString()
                    cantidad_disp 		= fila.get("CANTIDAD_RES").toString()
                    //el codigo de origen debe ser igual al codigo ingresado
                    cod_direccion_ori 	= fila.get("COD_DIRECCION_RES").toString()

                }


            } else {

                operacionIniciada = 0

            }



            if (operacionIniciada == 1) {

                var formBody: RequestBody = FormBody.Builder()
                    .add("USER", MainActivity.usuarioLogin.codUsuario)
                    .add("PASS", MainActivity.usuarioLogin.password)
                    .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                    .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                    .add("TIP_COMPROBANTE", tip_comprobante)
                    .add("SER_COMPROBANTE", ser_comprobante)
                    .add("NRO_COMPROBANTE", nro_comprobante)
                    .build()


                var result = HttpRequest.call("", "reabastecimiento/verifica_reabastecimiento2", formBody)

                var respuestaJson: JSONObject

                try {
                    respuestaJson = JSONObject(result)
                } catch (e: Exception) {
                    MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (verifica_reabastecimiento2) Error ${e.message.toString()} !")
                    return false
                }

                if (respuestaJson.has("rows")) {

                    val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

                    if (filas.length() == 0) {

                        val delete = "delete from WMS_TRANSFERENCIAS_MANUALES"
                        Operaciones.bdatos!!.execSQL(delete)

                        nro_comprobante = ""
                        tip_comprobante = ""
                        ser_comprobante = ""
                        cod_deposito = ""
                        cod_articulo = ""
                        nro_lote = ""
                        fec_vencimiento = ""
                        cod_unidad_med = ""
                        cantidad_disp = "0"
                        cantidad_transferida = "0"
                        cod_direccion_ori = ""

                        operacionIniciada = 0

                    }

                }

            }


        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (verificaReabastecimientoBase)")
        }

        return true

    }


    @SuppressLint("Range")
    fun obtenerTipoOperacionReabast(_ser: String, _tip: String, _nro: String) {

        val select: String
        try {
            select = ("select tip_operacion_reabast"
                    + "  from wms_transferencias_manuales "
                    + " where ser_comprobante = '" + _ser + "' "
                    + "   and tip_comprobante = '" + _tip + "' "
                    + "   and nro_comprobante = '" + _nro + "' "
                    + " order by id")
            cursor_reabast = Operaciones.bdatos!!.rawQuery(select, null)
        } catch (e: Exception) {
            var err = e.message
            err = "" + err
        }
        var nreg = 0
        tipo_operacion_reabast = if (cursor_reabast!!.moveToFirst()) {
            cursor_reabast!!.getString(cursor_reabast!!.getColumnIndex("TIP_OPERACION_REABAST"))
        } else {
            null
        }
        nreg = cursor_reabast!!.count
        m_tip_operacion_rea = arrayOfNulls<String>(nreg)

        for (i in 0 until nreg) {
            m_tip_operacion_rea[i] = cursor_reabast!!.getString(cursor_reabast!!.getColumnIndex("TIP_OPERACION_REABAST"))
            cursor_reabast!!.moveToNext()
        }
        try {
            tipo_operacion_iniciado = m_tip_operacion_rea[0]
        } catch (e: Exception) {
            tipo_operacion_iniciado = null
        }

    }







    fun esDriveIn(codDireccion : String) {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", codDireccion)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/es_drive_in", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (es_drive_in) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {


            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            esDrive = filas.length() > 0

        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (esDriveIn)")
        }


    }





    fun operacionValidaInicio() {

        if (operacionIniciada === 0) {
            if (validaExistenciaDireccion(etDestino.text.toString())) {
                validaArticulo(etDestino.text.toString())
            }
        } else {
//		1	PL_PK	debe tener ubicacion fija, ANTIGUO
//		2	PL_PL	pulmon destino no debe tener stock
//		3	PK_PL	que lleve solamente la cantidad necesaria,debe asignar vencimiento
//		4	PK_PK	que lleve solamente la cantidad necesaria, y ubicacion fija
            etDestino.setText(etDestino.text.toString().replace("\n", ""))
            if (validaDireccionReabast(etDestino.text.toString())) {
                if (tipo_operacion_reabast == null) {
                    return
                }
                val _band: String = tipo_operacion_reabast as String
                if (_band == "PL_PK") {
                    insertaDetalleReabastecimiento()
                } else if (_band == "PL_PL") {
                    insertaDetalleReabastecimientoPKPL()
                } else if (_band == "PK_PL") {
                    insertaDetalleReabastecimientoPKPL()
                } else if (_band == "PK_PK") {
                    insertaDetalleReabastecimiento()
                } else {
                }
            }
        }

    }


    fun validaDireccionReabast(direccionDes: String) : Boolean {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", cod_deposito)
            .add("COD_DIRECCION_DES", direccionDes)
            .add("COD_ARTICULO", cod_articulo)
            .add("COD_DIRECCION_ORI", cod_direccion_ori)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/valida_direccion_reabast", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_direccion_reabast) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("respuesta")) {
            val respuesta = respuestaJson.get("respuesta").toString()

            val m_resp: List<String> = respuesta.split("-")
            val _dim = m_resp.size

            tipo_operacion_reabast = m_resp[2].trim()


            return if (m_resp[0].trim().equals("S")) {
                true
            } else {
                val mensajeTexto = m_resp[1] + " " + m_resp[2]
                val mensajeTitulo = "Dirección invalida!"
                MainActivity.funciones.mensajeError(context, mensajeTitulo, mensajeTexto)
                false
            }


        } else {
            tipo_operacion_reabast = null
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaDireccionReabast)")
            return false
        }



    }


    fun validaExistenciaDireccion(codDireccion: String) : Boolean {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", codDireccion)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/valida_existencia_direccion", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_existencia_direccion) Error ${e.message.toString()} !")
            return false
        }


        if (respuestaJson.has("respuesta")) {
            val respuesta = respuestaJson.get("respuesta").toString()

            return if (respuesta.trim() == "S") {

                esDriveIn(codDireccion)
                true

            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "DISCULPE, DIRECCION SIN EXISTENCIA")
                false
            }

        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaExistenciaDireccion)")
            return false
        }


    }


    fun validaArticulo(codDireccion: String) {

        codArticuloVal = ""
        articuloValidado = false
        articulosEnDireccion = ArrayList<String>()


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", codDireccion)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/consulta_articulo_en_direccion", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_articulo_en_direccion) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {


            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            if (MainActivity.usuarioLogin.codEmpresa.trim() == "2"){

                for (i in 0 until filas.length()) {
                    val articuloObject : JSONObject = filas[i] as JSONObject

                    codArticuloVal = articuloObject.get("COD_ARTICULO").toString()
                    articulosEnDireccion.add(codArticuloVal)
                }

            } else {
                for (i in 0 until filas.length()) {
                    val articuloObject : JSONObject = filas[i] as JSONObject

                    codArticuloVal = articuloObject.get("COD_ARTICULO").toString()
                }

            }



            //Dialogo para validar el codigo del articulo
            try {
                dialogo_lee_codigo.dismiss()
            } catch (e: Exception) {
            }
            dialogo_lee_codigo = Dialog(context)
            dialogo_lee_codigo.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogo_lee_codigo.setContentView(R.layout.lector_codigo_articulo_reabast_normal)

            //final TextView tvTitulo	= (TextView) dialogo_lee_codigo.findViewById(R.id.tvTituloDireccion);
            //etCodigoArticulo 		= (EditText) dialogo_lee_codigo.findViewById(R.id.etCodigoBarra);


            dialogo_lee_codigo.btnAceptar.setOnClickListener {
                operacionValidacion()
            }

            dialogo_lee_codigo.btn_volver.setOnClickListener {
                dialogo_lee_codigo.dismiss()
            }


            dialogo_lee_codigo.etCodigoBarra.setOnFocusChangeListener { view, _ ->

                if (!view.hasFocus()) {
                    operacionValidacion()

                }

            }

            dialogo_lee_codigo.etCodigoBarra.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().indexOf("\n") > -1) {

                        dialogo_lee_codigo.etCodigoBarra.setText(dialogo_lee_codigo.etCodigoBarra.text.toString().replace("\n", ""))
                        //Validar que el codigo del articulo corresponda a la dirección
                        operacionValidacion()
                    }



                }
            })


            dialogo_lee_codigo.setCancelable(false)
            dialogo_lee_codigo.show()



        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaArticulo)")
        }


    }




    fun operacionValidacion() {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("CODIGO", dialogo_lee_codigo.etCodigoBarra.text.toString())
            .build()

        var result = HttpRequest.call("", "reabastecimiento/valida_articulo", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_articulo) Error ${e.message.toString()} !")
            return
        }


        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            if (MainActivity.usuarioLogin.codEmpresa == "2") {
                articuloValidado = false

                for (i in 0 until filas.length()) {

                    val fila : JSONObject = filas[i] as JSONObject

                    if (articuloValidado == false) {

                        articulosEnDireccion.forEach {

                            if (it == fila.get("COD_ARTICULO")) {

                                articuloValidado = true
                                codArticuloVal = it
                                return@forEach

                            } else {

                                if (articuloValidado == false) {

                                    if (etDestino.text.toString().indexOf("GRA000000") != -1 && operacionIniciada == 1) {

                                        articuloValidado = true
                                        return@forEach

                                    } else {
                                        articuloValidado = false

                                    }

                                }

                            }

                        }

                    }




                }


            } else {

                for (i in 0 until filas.length()) {

                    val fila : JSONObject = filas[i] as JSONObject

                    if(fila.get("COD_ARTICULO") == codArticuloVal){

                        articuloValidado = true

                    } else {

                        articuloValidado = etDestino.text.toString().indexOf("GRA000000") != -1 && operacionIniciada == 1

                    }

                }

            }

            //averiguar que significa tipo_operacion ;
            //Si la operacion no fue iniciada aún, se inserta la cabecera
            if (articuloValidado) {

                if(operacionIniciada == 0){
                    insertaCabeceraReabastecimiento()
                    //sino, se inserta el detalle del reabastecimiento
                }else if(operacionIniciada == 1){
                    insertaDetalleReabastecimiento()
                }

            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "Articulo no corresponde")

            }


            dialogo_lee_codigo.dismiss()



        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (operacionValidacion)")
        }


    }



    fun insertaCabeceraReabastecimiento() {

        var metodo = ""

        metodo = if (MainActivity.usuarioLogin.codEmpresa == "2") {
            "reabastecimiento/inicia_reabastecimiento_sun"
        } else {
            "reabastecimiento/inicia_reabastecimiento"
        }

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", MainActivity.usuarioLogin.codDepositoVerde)
            .add("COD_DIRECCION", etDestino.text.toString())
            .add("COD_ARTICULO", codArticuloVal)
            .build()

        var result = HttpRequest.call("", metodo, formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (insertaCabeceraReabastecimiento) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("respuesta")) {
            val resp = respuestaJson.get("respuesta").toString()

            val respuesta = resp.split("-")

            if (respuesta.size > 7) {

                nro_comprobante = respuesta[0]
                tip_comprobante = respuesta[1]
                ser_comprobante = respuesta[2]
                cod_deposito = respuesta[3]
                cod_articulo = respuesta[4]
                nro_lote = respuesta[5]
                fec_vencimiento = respuesta[6]
                cod_unidad_med = respuesta[7]
                cantidad_disp = respuesta[8]

                direccionOrigen = etDestino.text.toString().trim()


                //INSERTAR DATOS LOCALES
                val values = ContentValues()
                values.put("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                values.put("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                values.put("COD_DIRECCION_ORIGEN", etDestino.text.toString())
                values.put("NRO_COMPROBANTE", nro_comprobante)
                values.put("SER_COMPROBANTE", ser_comprobante)
                values.put("TIP_COMPROBANTE", tip_comprobante)
                values.put("COD_DEPOSITO", cod_deposito)
                values.put("COD_ARTICULO", cod_articulo)
                values.put("NRO_LOTE", nro_lote)
                values.put("COD_UNIDAD_MED", cod_unidad_med)
                values.put("CANTIDAD", cantidad_disp)
                values.put("ESTADO", "P")
                values.put("FEC_VENCIMIENTO", fec_vencimiento)

                Operaciones.bdatos!!.insert("wms_transferencias_manuales", null, values)

                etDestino.setText("")
                etDestino.requestFocus()



            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "Comprueba el stock de origen!!. Error al iniciar Reabastecimiento")

            }



        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (insertaCabeceraReabastecimiento)")
        }


        validarTodo()


    }


    fun insertaDetalleReabastecimiento() {

        try {
            dialogo_inserta_cantidad.dismiss()
        } catch (e: Exception) {

        }

        dialogo_inserta_cantidad = Dialog(context)
        dialogo_inserta_cantidad.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_inserta_cantidad.setContentView(R.layout.reabastecimiento_ingresa_cant)

        cargaUnidadMedida()

        dialogo_inserta_cantidad.btnAceptar.setOnClickListener {

            cod_unidad_med = dialogo_inserta_cantidad.spDirecciones.selectedItem.toString().substring(0,2).trim()
            var _band = tipo_operacion_iniciado
            if(_band.equals("PL_PK")){
                if (verificaTotal(dialogo_inserta_cantidad.etCodigoBarra.text.toString())) {

                    var ingresado = 0.0
                    var multiploUm = 0.0
                    var _ingres = 0.0
                    ingresado 	= dialogo_inserta_cantidad.etCodigoBarra.text.toString().toDouble()
                    multiploUm	= umReabastecimiento[posicionUM].mult.toDouble()

                    _ingres = ingresado * multiploUm
                    insertaReabastDet(ingresado.toInt(), etDestino.text.toString())
                    cargaReabastecimientoDirecciones()
                    dialogo_inserta_cantidad.dismiss()
                }
            }else{
                if(nreg_detalle_reabast<1){
                    if (verificaTotal(dialogo_inserta_cantidad.etCodigoBarra.text.toString())) {

                        var ingresado = 0.0
                        var multiploUm = 0.0
                        var _ingres = 0.0

                        ingresado = dialogo_inserta_cantidad.etCodigoBarra.text.toString().toDouble()
                        multiploUm = umReabastecimiento[posicionUM].mult.toDouble()

                        _ingres = ingresado * multiploUm
                        insertaReabastDet(ingresado.toInt(), etDestino.text.toString())
                        cargaReabastecimientoDirecciones()
                        dialogo_inserta_cantidad.dismiss()
                    }
                }else{
                    var mensajeTexto = "Debe confirmar el reabastecimiento ya ingresado"
                    var mensajeTitulo = "Limite superado!"
                    var nombreBoton = "OK"

                    MainActivity.funciones.mensajeError(context, mensajeTitulo, mensajeTexto)

                }
            }

        }

        dialogo_inserta_cantidad.btn_volver.setOnClickListener {

            etDestino.setText("")
            etDestino.requestFocus()
            dialogo_inserta_cantidad.dismiss()

        }

        dialogo_inserta_cantidad.setCancelable(false)
        dialogo_inserta_cantidad.show()
        dialogo_inserta_cantidad.etCodigoBarra.requestFocus()
        dialogo_inserta_cantidad.etCodigoBarra.selectAll()

    }



    fun insertaDetalleReabastecimientoPKPL() {

        etDestino.setText(etDestino.text.toString().replace("\n", ""))

        if (validaDireccionReabast(etDestino.text.toString())) {

            try {
                dialogo_inserta_cantidad.dismiss()
            } catch (e: Exception) {

            }

            dialogo_inserta_cantidad = Dialog(context)
            dialogo_inserta_cantidad.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogo_inserta_cantidad.setContentView(R.layout.reabastecimiento_ingresa_cant_fec)

            cargaUnidadMedida()

            var _band = tipo_operacion_reabast
            if(_band.equals("PL_PL")){
                dialogo_inserta_cantidad.etFechaVenciento.setText(fec_vencimiento)
                dialogo_inserta_cantidad.etFechaVenciento.isEnabled = false
            }else{
                dialogo_inserta_cantidad.etFechaVenciento.isEnabled = true
            }

            dialogo_inserta_cantidad.btnAceptar.setOnClickListener {


                cod_unidad_med = dialogo_inserta_cantidad.spDirecciones.selectedItem.toString().substring(0,2).trim()

                var fec = dialogo_inserta_cantidad.etFechaVenciento.text.toString()
                if(!validaFechaVencimiento(fec)){
                    return@setOnClickListener
                }

                var _band = tipo_operacion_iniciado
                if(_band.equals("PL_PK")){
                    if (verificaTotal(dialogo_inserta_cantidad.etCodigoBarra.text.toString())) {

                        var ingresado = 0.0
                        var multiploUm = 0.0
                        var _ingres = 0.0
                        ingresado 	= dialogo_inserta_cantidad.etCodigoBarra.text.toString().toDouble()
                        multiploUm	= umReabastecimiento[posicionUM].mult.toDouble()

                        _ingres = ingresado * multiploUm
                        insertaReabastDet(ingresado.toInt(), etDestino.text.toString())
                        cargaReabastecimientoDirecciones()
                        dialogo_inserta_cantidad.dismiss()
                    }
                }else{
                    if (esDrive) {
                        if (verificaTotal(dialogo_inserta_cantidad.etCodigoBarra.getText().toString())) {


                            var ingresado = 0.0
                            var multiploUm = 0.0
                            var _ingres = 0.0
                            ingresado 	= dialogo_inserta_cantidad.etCodigoBarra.getText().toString().toDouble()
                            multiploUm	= umReabastecimiento[posicionUM].mult.toDouble()

                            _ingres = ingresado * multiploUm
                            if(_band.equals("PL_PL")){
                                insertaReabastDet(ingresado.toInt(), etDestino.text.toString())
                            }else{
                                insertaReabastDetPKPL(ingresado.toInt(), etDestino.text.toString())
                            }
                            cargaReabastecimientoDirecciones()
                            dialogo_inserta_cantidad.dismiss()
                        }
                    } else {
                        if(nreg_detalle_reabast<1){
                            if (verificaTotal(dialogo_inserta_cantidad.etCodigoBarra.getText().toString())) {

                                var ingresado 	= dialogo_inserta_cantidad.etCodigoBarra.getText().toString().toDouble()
                                var multiploUm	= umReabastecimiento[posicionUM].mult.toDouble()

                                var _ingres = ingresado * multiploUm
                                if(_band.equals("PL_PL")){
                                    insertaReabastDet(ingresado.toInt(), etDestino.text.toString())
                                }else{
                                    insertaReabastDetPKPL(ingresado.toInt(), etDestino.text.toString())
                                }
                                cargaReabastecimientoDirecciones()
                                dialogo_inserta_cantidad.dismiss()
                            }
                        }else{
                            var mensajeTexto 	= "Debe confirmar el reabastecimiento ya ingresado"
                            var mensajeTitulo	= "Limite superado!"
                            MainActivity.funciones.mensajeError(context, mensajeTitulo, mensajeTexto)
                        }
                    }
                }

            }

            dialogo_inserta_cantidad.btn_volver.setOnClickListener {

                etDestino.setText("")
                etDestino.requestFocus()
                dialogo_inserta_cantidad.dismiss()

            }



            dialogo_inserta_cantidad.setCancelable(false)
            dialogo_inserta_cantidad.show()
            dialogo_inserta_cantidad.etCodigoBarra.requestFocus()
            dialogo_inserta_cantidad.etCodigoBarra.selectAll()


        }


    }

    fun validaFechaVencimiento(_fecha: String) : Boolean {

        var resu = false

        var fec: String
        fec = _fecha

        val fec2: String = condensaFecha2(fec)

        fec = fec2
        if (fec.length == 6) {
            try {
                val dfDate = SimpleDateFormat("dd/MM/yyyy")
                dfDate.setLenient(false)
                fec = fec.substring(0, 2) + "/" + fec.substring(2, 4) + "/20" + fec.substring(4, 6)
                if (!consultarFechaDestino(fec)) {

                    SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Atencion")
                        .setContentText("Fecha incorrecta")
                        .setConfirmText("OK")
                        .setConfirmClickListener { sDialog ->

                            sDialog.dismissWithAnimation()

                        }
                        .show()

                    return false
                }
                dfDate.parse(fec)
                fecha_vencimiento_PKPL = fec
                //				etFechaVencPKPL.setText(fec);
                resu = true
            } catch (e: java.lang.Exception) {
                val err = e.message //

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atencion")
                    .setContentText("FECHA INGRESADA INCORRECTA")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        dialogo_inserta_cantidad.etFechaVenciento.requestFocus()
                        sDialog.dismissWithAnimation()

                    }
                    .show()


            }
        } else {
            if (ind_maneja_vto == "S") {

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atencion")
                    .setContentText("FECHA INGRESADA INCORRECTA")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        dialogo_inserta_cantidad.etFechaVenciento.requestFocus()
                        sDialog.dismissWithAnimation()

                    }
                    .show()
            }
        }

        return resu

    }

    fun consultarFechaDestino(fecha: String) : Boolean {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", etDestino.text.toString())
            .add("COD_ARTICULO", cod_articulo)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/consultar_fecha_destino", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consultar_fecha_destino) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("rows")) {

            var fechaVenc = ""
            var cantidadDisponibleProv = ""


            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filas.length()) {
                val fila : JSONObject = filas.get(i) as JSONObject

                fechaVenc = fila.get("FEC_VENCIMIENTO").toString()
                cantidadDisponibleProv = fila.get("CANT_DISP").toString()

                fechaVenc = "${fechaVenc.substring(8,10)}/${fechaVenc.substring(5, 7)}/${fechaVenc.substring(0, 4)}"

                if (fecha == fechaVenc) {
                    return true
                }

            }

            if (fechaVenc.isEmpty()) {
                return true
            }

            return false;


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (consultarFechaDestino)")
            return false

        }



    }

    private fun condensaFecha2(viejaFecha: String): String {
        var nuevaFecha = viejaFecha
        val _fec = ""
        var _dd = ""
        var _mm = ""
        var _aa = ""
        val mSinBarra = viejaFecha.split("/".toRegex()).toTypedArray()
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


    fun cargaUnidadMedida() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", etDestino.text.toString())
            .add("COD_ARTICULO", cod_articulo)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/consulta_unidad_medida", formBody)


        posicionUM = 0
        posicionUMBasico = 0
        umReabastecimiento = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_unidad_medida) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {
            val umReabastecimientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until umReabastecimientoArray.length()) {
                val umReabastecimientoObject : JSONObject = umReabastecimientoArray.get(i) as JSONObject
                val um = UnidadMedidaReabastecimiento()
                um.codUnidadRel = umReabastecimientoObject.get("COD_UNIDAD_REL").toString()
                um.referencia = umReabastecimientoObject.get("REFERENCIA").toString()
                um.mult = umReabastecimientoObject.get("MULT").toString()
                um.indBasico = umReabastecimientoObject.get("IND_BASICO").toString()
                um.lastro = umReabastecimientoObject.get("LASTRO").toString()
                um.capas = umReabastecimientoObject.get("CAPAS").toString()

                umReabastecimiento.add(um)


                if (um.indBasico == "S") {
                    posicionUMBasico = i
                }

            }

            val spinnerAdapter : ArrayAdapter<UnidadMedidaReabastecimiento> =
                ArrayAdapter(context, R.layout.spinner_adapter, umReabastecimiento)
            spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            dialogo_inserta_cantidad.spDirecciones.adapter = spinnerAdapter



            dialogo_inserta_cantidad.spDirecciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    posicionUM = position

                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }

            if (umReabastecimiento.size != 0) {
                dialogo_inserta_cantidad.spDirecciones.setSelection(posicionUMBasico)
            }


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaUnidadMedida)")


        }



    }




    fun verificaTotal(ingresado: String) : Boolean {

        var umSeleccionado = umReabastecimiento[posicionUM]

        cantidad_disp = cantidadDisponible().toString() + ""
        val _ingres: Double = ingresado.toDouble() * umSeleccionado.mult.toDouble()

        return if (ingresado.isNotEmpty() && _ingres > 0) {
            if (cantidad_disp.toDouble() >= _ingres) {
                true
            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "CANTIDAD TOTAL SOBREPASA LO DISPONIBLE EN DIRECCION")
                false
            }
        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "DEBE INGRESAR CANTIDAD MAYOR A 0")
            false
        }

    }




    fun insertaReabastDet(ingresado: Int, codDireccionDes: String) {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", cod_deposito)
            .add("TIP_COMPROBANTE", tip_comprobante)
            .add("SER_COMPROBANTE", ser_comprobante)
            .add("NRO_COMPROBANTE", nro_comprobante)
            .add("COD_DIRECCION", cod_direccion_ori)
            .add("COD_DIRECCION_DES", codDireccionDes)
            .add("COD_ARTICULO", cod_articulo)
            .add("COD_UNIDAD_MEDIDA", cod_unidad_med)
            .add("CANTIDAD", ingresado.toString())
            .add("FEC_VENCIMIENTO", fec_vencimiento)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/inserta_reabast_det_act", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (inserta_reabast_det_act) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            val res: List<String> = respuesta.split("*")

            var  mensaje = ""


            if (res[0] == "01") {
                mensaje = "REGISTRADO CON EXITO"
            } else {
                mensaje = respuesta
            }

            SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Atencion")
                .setContentText(mensaje)
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->

                    sDialog.dismissWithAnimation()

                }
                .show()

            actualizaTipoOperacionReabast()



        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (insertaReabastDet)")

        }


    }


    fun insertaReabastDetPKPL(ingresado: Int, codDireccionDes: String) {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", cod_deposito)
            .add("TIP_COMPROBANTE", tip_comprobante)
            .add("SER_COMPROBANTE", ser_comprobante)
            .add("NRO_COMPROBANTE", nro_comprobante)
            .add("COD_DIRECCION", cod_direccion_ori)
            .add("COD_DIRECCION_DES", codDireccionDes)
            .add("COD_ARTICULO", cod_articulo)
            .add("COD_UNIDAD_MEDIDA", cod_unidad_med)
            .add("CANTIDAD", ingresado.toString())
            .add("FEC_VENCIMIENTO", fecha_vencimiento_PKPL)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/inserta_reabast_det_act", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (inserta_reabast_det_act) Error ${e.message.toString()} !")
            return
        }


        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            val res: List<String> = respuesta.split("*")

            var  mensaje = ""


            if (res[0] == "01") {
                mensaje = "REGISTRADO CON EXITO"
            } else {
                mensaje = respuesta
            }

            SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Atencion")
                .setContentText(mensaje)
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->


                    sDialog.dismissWithAnimation()

                }
                .show()




        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (insertaReabastDetPKPL)")

        }


    }


    fun actualizaTipoOperacionReabast() : Boolean {

        var resu = false

        try {
            Operaciones.bdatos!!.beginTransaction()
            //----------------------------------------------------//
            // Modifica el estado de tipo_operacion_reabast
            val sqlUpdate: String
            sqlUpdate = ("update wms_transferencias_manuales "
                    + "   set TIP_OPERACION_REABAST = '" + tipo_operacion_reabast + "' "
                    + " where SER_COMPROBANTE	= '" + ser_comprobante + "' "
                    + "   and TIP_COMPROBANTE	= '" + tip_comprobante + "' "
                    + "   and NRO_COMPROBANTE	= '" + nro_comprobante + "' "
                    + "   and length(TIP_OPERACION_REABAST)=0")
            Operaciones.bdatos!!.execSQL(sqlUpdate)
            //----------------------------------------------------//
            Operaciones.bdatos!!.setTransactionSuccessful()
            Operaciones.bdatos!!.endTransaction()
            resu = true
        } catch (e: java.lang.Exception) {
            Operaciones.bdatos!!.endTransaction()
        }
        return resu

    }


    fun abreSugerenciaPulmon() {

        try {
            dialog_sugerencia_pulmon.dismiss()
        } catch (e: Exception) {

        }

        dialog_sugerencia_pulmon = Dialog(context)
        dialog_sugerencia_pulmon.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog_sugerencia_pulmon.setContentView(R.layout.lista_sugerencia_pulmon)


        dialog_sugerencia_pulmon.etCodigoArticuloSugerencia.setOnFocusChangeListener { view, _ ->

            if (!view.hasFocus()) {
                dialog_sugerencia_pulmon.etCodigoArticuloSugerencia.setText(dialog_sugerencia_pulmon.etCodigoArticuloSugerencia.text
                    .toString().replace("\n", ""))
                cargaSugerenciasPulmones(dialog_sugerencia_pulmon.etCodigoArticuloSugerencia.text.toString())

            }

        }

        dialog_sugerencia_pulmon.etCodigoArticuloSugerencia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n") > -1) {
                    dialog_sugerencia_pulmon.etDesvioFocus.requestFocus()
                }

            }
        })


        dialog_sugerencia_pulmon.btnCerrarSugerencias.setOnClickListener {

            var dir = pickingRef

            var dirAux = dir
            if (dirAux.length == 9) {
                dir = "${dirAux.substring(0, 3)}-${dirAux.substring(3, 6)}-${dirAux.substring(6, 7)}-${dirAux.subSequence(7, 9)}"
            }


            tvDatos3.text = "Ref. $dir"
            dialog_sugerencia_pulmon.dismiss()

        }



        dialog_sugerencia_pulmon.setCancelable(false)
        dialog_sugerencia_pulmon.show()

    }


    fun cargaSugerenciasPulmones(codigo: String) {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("CODIGO", codigo)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/consulta_sugerencia_pulmon", formBody)



        sugerenciaPulmonReabastecimiento = ArrayList()
        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_sugerencia_pulmon) Error ${e.message.toString()} !")
            return
        }


        if (respuestaJson.has("rows")) {
            val sugerenciaPulmonReabastecimientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until sugerenciaPulmonReabastecimientoArray.length()) {
                val sugerenciaPulmonReabastecimientoObject : JSONObject = sugerenciaPulmonReabastecimientoArray[i] as JSONObject
                val spr = SugerenciaPulmonReabastecimiento()
                spr.codArticulo = sugerenciaPulmonReabastecimientoObject.get("COD_ARTICULO").toString()
                spr.codDireccion = sugerenciaPulmonReabastecimientoObject.get("COD_DIRECCION").toString()
                spr.descripcion = sugerenciaPulmonReabastecimientoObject.get("DESCRIPCION").toString()
                spr.cantidad = sugerenciaPulmonReabastecimientoObject.get("CANTIDAD").toString()
                spr.fecVencimiento = sugerenciaPulmonReabastecimientoObject.get("FEC_VENCIMIENTO").toString()
                spr.vencimiento = sugerenciaPulmonReabastecimientoObject.get("VENCIMIENTO").toString()

                sugerenciaPulmonReabastecimiento.add(spr)

                pickingRef = codigo
            }

            val gridLayoutManager = GridLayoutManager(context, 1)

            dialog_sugerencia_pulmon.rvSugerenciaPulmon.layoutManager = gridLayoutManager
            dialog_sugerencia_pulmon.rvSugerenciaPulmon.itemAnimator = DefaultItemAnimator()
            dialog_sugerencia_pulmon.rvSugerenciaPulmon.setHasFixedSize(true)


            // this creates a vertical layout Manager
            dialog_sugerencia_pulmon.rvSugerenciaPulmon.layoutManager = LinearLayoutManager(
                context
            )

            // This loop will create 20 Views containing
            // the image with the count of view
            // This will pass the ArrayList to our Adapter
            val adapter = AdapterSugerenciaPulmon(
                context,
                sugerenciaPulmonReabastecimiento,
                R.layout.card_view_reabastecimiento_sugerencia_pulmon  )

            // Setting the Adapter with the recyclerview
            dialog_sugerencia_pulmon.rvSugerenciaPulmon.adapter = adapter


        } else {


            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaSugerenciasPulmones)")


        }

    }




    fun cancelar() {

        if (operacionIniciada === 1) {
            if (direccionReabastecimiento.size > 0) {


                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atención!")
                    .setContentText("Elimine el detalle del reabastecimiento para cancelar.")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        sDialog.dismissWithAnimation()

                    }
                    .show()

                return
            }



            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Atencion!")
                .setContentText("¿Cancelar reabastecimiento actual?")
                .setConfirmText("Si")
                .setConfirmClickListener { sDialog ->

                    cancelaReabastPicking()
                    liDatos.visibility = View.GONE
                    sDialog.dismissWithAnimation()

                }
                .setCancelButton(
                    "No"
                ) { sDialog -> sDialog.dismissWithAnimation() }
                .show()


        } else {

            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Atención!")
                .setContentText("\"No se encuentra ninguna operacion iniciada")
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->

                    sDialog.dismissWithAnimation()

                }
                .show()
        }

    }


    fun cancelaReabastPicking() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", cod_direccion_ori)
            .add("TIP_COMPROBANTE", tip_comprobante)
            .add("SER_COMPROBANTE", ser_comprobante)
            .add("NRO_COMPROBANTE", nro_comprobante)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/cancela_reabast_picking", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cancela_reabast_picking) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            val res: List<String> = respuesta.split("*")

            var  mensaje = ""

            if (res[0] == "01") {
                val update = "delete from wms_transferencias_manuales   "
                Operaciones.bdatos!!.execSQL(update)
                nro_comprobante = ""
                ser_comprobante = ""
                tip_comprobante = ""
                mensaje = "Reabastecimiento Cancelado"
            }

            SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Atencion")
                .setContentText(mensaje)
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->


                    sDialog.dismissWithAnimation()

                }
                .show()




        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (insertaReabastDetPKPL)")

        }

        validarTodo()


    }


    fun confirmar() {

        if (operacionIniciada == 1) {



            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Atencion!")
                .setContentText("¿Finalizar reabastecimiento?")
                .setConfirmText("Si")
                .setConfirmClickListener { sDialog ->

                    var total_sob = cantidad_disp.toDouble() - cantidad_transferida.toDouble()

                    obtenerTipoOperacionReabast(ser_comprobante, tip_comprobante, nro_comprobante)

                    var _band = tipo_operacion_iniciado

                    if(_band.equals("PL_PK")){
                        finalizaReabastPicking()
                    }else{
                        finalizaReabastPicking()
                    }


                    sDialog.dismissWithAnimation()

                }
                .setCancelButton(
                    "No"
                ) { sDialog -> sDialog.dismissWithAnimation() }
                .show()



        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "No se encuentra ninguna operacion iniciada")
        }



    }


    fun finalizaReabastPicking() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", tip_comprobante)
            .add("SER_COMPROBANTE", ser_comprobante)
            .add("NRO_COMPROBANTE", nro_comprobante)
            .build()

        var result = HttpRequest.call("", "reabastecimiento/finaliza_reabast_picking_act", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (finaliza_reabast_picking_act) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            val res: List<String> = respuesta.split("*")

            var  mensaje = ""

            if (res[0].equals("01")) {
                var update = "delete from wms_transferencias_manuales";
                Operaciones.bdatos!!.execSQL(update);
                nro_comprobante = "";
                ser_comprobante = "";
                tip_comprobante = "";
                mensaje = "Reabastecimiento Finalizado";
            } else {

                mensaje = respuesta

            }

            SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Atencion")
                .setContentText(mensaje)
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->


                    sDialog.dismissWithAnimation()

                }
                .show()

        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (finalizaReabastPicking)")

        }

        validarTodo();

    }


}