package com.apolo.wms.operaciones.reabastecimientocorrprev

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.Operaciones
import com.apolo.wms.clases.reabastecimientocorrprev.TransferenciaReabastecimientoCorrPrev
import com.apolo.wms.operaciones.reabastecimiento.ReabastecimientoMarcaderiaDriveInNew
import com.apolo.wms.operaciones.reabastecimientocorrprev.adapter.AdapterTransferenciaReabastecimientoCorrPrev
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.lector_codigo_articulo.*
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.*
import kotlinx.android.synthetic.main.list_reabast_corr_prev.*
import kotlinx.android.synthetic.main.list_reabast_corr_prev.btnCancelarList
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.sql.SQLException
import java.util.*


class ReabastecimientoCorrPrev : AppCompatActivity() {

    companion object {
        lateinit var context : Context
        lateinit var rvTransferencias : RecyclerView

        var transferenciaReabastecimiento = ArrayList<TransferenciaReabastecimientoCorrPrev>()
        var posicionTransferencia = 0



        fun obtieneListaTransferencias() {


            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .build()

            var result = HttpRequest.call("", "reabastecimientocorrprev/obtiene_lista_transferencias", formBody)


            posicionTransferencia = 0
            transferenciaReabastecimiento = ArrayList()

            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_lista_transferencias) Error ${e.message.toString()} !")
                return
            }

            if (respuestaJson.has("rows")) {
                val transferenciaReabastecimientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


                for (i in 0 until transferenciaReabastecimientoArray.length()) {
                    val transferenciaReabastecimientoObject : JSONObject = transferenciaReabastecimientoArray.get(i) as JSONObject
                    val tr = TransferenciaReabastecimientoCorrPrev()
                    tr.codEmpresa = transferenciaReabastecimientoObject.get("COD_EMPRESA").toString()
                    tr.tipComprobante = transferenciaReabastecimientoObject.get("TIP_COMPROBANTE").toString()
                    tr.serComprobante = transferenciaReabastecimientoObject.get("SER_COMPROBANTE").toString()
                    tr.nroComprobante = transferenciaReabastecimientoObject.get("NRO_COMPROBANTE").toString()
                    tr.dirOrigen = transferenciaReabastecimientoObject.get("COD_DIRECCION").toString()
                    tr.dirDestino = transferenciaReabastecimientoObject.get("COD_DIRECCION_DES").toString()
                    tr.cantidad = transferenciaReabastecimientoObject.get("CANTIDAD").toString()
                    tr.codArticulo = transferenciaReabastecimientoObject.get("COD_ARTICULO").toString()
                    tr.descArticulo = transferenciaReabastecimientoObject.get("DESC_ARTICULO").toString()
                    tr.unidadMedida = transferenciaReabastecimientoObject.get("COD_UNIDAD_MEDIDA").toString()
                    tr.descUnidadMedida = transferenciaReabastecimientoObject.get("DESC_UN_MEDIDA").toString()
                    tr.nroOrden = transferenciaReabastecimientoObject.get("NRO_ORDEN").toString()
                    tr.estado = "P"
                    tr.tipMov = transferenciaReabastecimientoObject.get("TIP_MOV").toString()
                    tr.cantidadRes = transferenciaReabastecimientoObject.get("CANTIDAD_RES").toString()
                    tr.codUnidMedRes = transferenciaReabastecimientoObject.get("COD_UNID_MED_RES").toString()
                    tr.codDeposito = transferenciaReabastecimientoObject.get("COD_DEPOSITO").toString()
                    tr.fecVencimiento = transferenciaReabastecimientoObject.get("FEC_VENCIMIENTO").toString()
                    tr.fecVencim01 = transferenciaReabastecimientoObject.get("FEC_VENCIM_01").toString()
                    tr.cantidadUbRes = transferenciaReabastecimientoObject.get("CANTIDAD_UB_RES").toString()
                    tr.cantidadUb = transferenciaReabastecimientoObject.get("CANTIDAD_UB").toString()
                    tr.relacion = transferenciaReabastecimientoObject.get("RELACION").toString()
                    tr.nroLote = transferenciaReabastecimientoObject.get("NRO_LOTE").toString()

                    transferenciaReabastecimiento.add(tr)


                }


                val gridLayoutManager = GridLayoutManager(context, 1)

                rvTransferencias.layoutManager = gridLayoutManager
                rvTransferencias.itemAnimator = DefaultItemAnimator()
                rvTransferencias.setHasFixedSize(true)


                // this creates a vertical layout Manager
                rvTransferencias.layoutManager = LinearLayoutManager(context)

                // This loop will create 20 Views containing
                // the image with the count of view
                // This will pass the ArrayList to our Adapter
                val adapter = AdapterTransferenciaReabastecimientoCorrPrev(
                    context,
                    transferenciaReabastecimiento,
                    R.layout.card_view_confirmar_reabast_prev  )

                // Setting the Adapter with the recyclerview
                rvTransferencias.adapter = adapter

            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaUnidadMedida)")

            }


        }


        fun elimina_reabastcorprev_det(tr: TransferenciaReabastecimientoCorrPrev) : Boolean {


            var resu = false

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", tr.tipComprobante)
                .add("SER_COMPROBANTE", tr.serComprobante)
                .add("NRO_COMPROBANTE", tr.nroComprobante)
                .add("NRO_ORDEN", tr.nroOrden)
                .add("COD_ARTICULO", tr.codArticulo)
                .build()

            var result = HttpRequest.call("", "reabastecimientocorrprev/elimina_reabastcorprev_det", formBody)


            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (elimina_reabastcorprev_det) Error ${e.message.toString()} !")
                return false
            }

            if (respuestaJson.has("respuesta")) {

                val respuesta = respuestaJson.get("respuesta").toString()

                resu = true

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("¡Atención!")
                    .setContentText(respuesta)
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        sDialog.dismissWithAnimation()

                    }
                    .show()

            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (elimina_reabastcorprev_det)")


            }


            return resu


        }

    }

    var realizar_doble_proceso = false

    var tipo_operacion_reabast: String? = null

    private lateinit var dialogo_lee_codigo: Dialog
    private lateinit var dialogo_confirma: Dialog

    var relacion = ""
    var articuloRelacionado = ""
    var nroComprobante = ""

    var direccion_origen_base = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_reabast_corr_prev)

        inicializar()

    }

    fun inicializar() {

        context = this

        title = "Reabastecimiento Preventivo/Correctivo".uppercase(Locale.getDefault())

        realizar_doble_proceso=false

        Companion.rvTransferencias = rvTransferencias



        /*codigo de barra de producto*/
        etCodigoBarraList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                try {
                    val cTextoCompleto: String = etCodigoBarraList.text.toString()
                    if (cTextoCompleto.indexOf("\n") > -1) { // si existe enter
                        etBuscarArticulo.requestFocus()
                    }
                } catch (e: Exception) {
                    var err = e.message
                }

            }
        })


        etCodigoBarraList.setOnFocusChangeListener { view, _ ->


            if (!view.hasFocus()) {

                val cTexto: String = etCodigoBarraList.text.toString().replace("\n", "")

                if (cTexto.trim().isNotEmpty()) {
                    if (valida_es_palet(cTexto)) {

                        abreCuadroArticulo()

                    } else {

                        if (valida_direccion_origen()) {
                            if (transferenciaReabastecimiento[posicionTransferencia].relacion == "0") {
                                abreCuadroArticulo()
                            } else {
                                abreCuadroDirDestinoRelacion()
                            }
                        } else {

                            MainActivity.funciones.mensajeError(context, "Atencion", "No se encontraron ordenes de reabastecimiento")

                        }

                    }


                }

            }


        }


        etBuscarArticulo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                try {
                    val cTextoCompleto = etBuscarArticulo.text.toString()
                    if (cTextoCompleto.indexOf("\n") > -1) { // si existe enter
                        etDesvioFocusList.requestFocus()
                    }
                } catch (e: java.lang.Exception) {
                    var err = e.message
                }

            }
        })


        etBuscarArticulo.setOnFocusChangeListener { view, _ ->


            if (!view.hasFocus()) {
                val cTexto = etBuscarArticulo.text.toString().replace("\n", "")

                if (cTexto.trim().isNotEmpty()) {
                    try {
                        obtieneListaTransferenciasArticulo()
                        etCodigoBarraList.requestFocus()
                    } catch (e: SQLException) {

                        e.printStackTrace()
                    }
                }

            }


        }


        btnCancelarList.setOnClickListener {
            etCodigoBarraList.setText("")
            etCodigoBarraList.requestFocus()
        }


        btCancelarBusqueda.setOnClickListener {
            etCodigoBarraList.setText("")
            etBuscarArticulo.setText("")
            etCodigoBarraList.requestFocus()
            try {
                obtieneListaTransferencias()
            } catch (e: SQLException) {

                e.printStackTrace()
            }
        }

        obtieneListaTransferencias()


    }

    fun obtieneListaTransferenciasArticulo() {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", etBuscarArticulo.text.toString().trim())
            .build()

        var result = HttpRequest.call("", "reabastecimientocorrprev/obtiene_lista_transferencias_articulo", formBody)


        posicionTransferencia = 0
        transferenciaReabastecimiento = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_lista_transferencias) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {
            val transferenciaReabastecimientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until transferenciaReabastecimientoArray.length()) {
                val transferenciaReabastecimientoObject : JSONObject = transferenciaReabastecimientoArray.get(i) as JSONObject
                val tr = TransferenciaReabastecimientoCorrPrev()
                tr.codEmpresa = transferenciaReabastecimientoObject.get("COD_EMPRESA").toString()
                tr.tipComprobante = transferenciaReabastecimientoObject.get("TIP_COMPROBANTE").toString()
                tr.serComprobante = transferenciaReabastecimientoObject.get("SER_COMPROBANTE").toString()
                tr.nroComprobante = transferenciaReabastecimientoObject.get("NRO_COMPROBANTE").toString()
                tr.dirOrigen = transferenciaReabastecimientoObject.get("COD_DIRECCION").toString()
                tr.dirDestino = transferenciaReabastecimientoObject.get("COD_DIRECCION_DES").toString()
                tr.cantidad = transferenciaReabastecimientoObject.get("CANTIDAD").toString()
                tr.codArticulo = transferenciaReabastecimientoObject.get("COD_ARTICULO").toString()
                tr.descArticulo = transferenciaReabastecimientoObject.get("DESC_ARTICULO").toString()
                tr.unidadMedida = transferenciaReabastecimientoObject.get("COD_UNIDAD_MEDIDA").toString()
                tr.descUnidadMedida = transferenciaReabastecimientoObject.get("DESC_UN_MEDIDA").toString()
                tr.nroOrden = transferenciaReabastecimientoObject.get("NRO_ORDEN").toString()
                tr.estado = "P"
                tr.tipMov = transferenciaReabastecimientoObject.get("TIP_MOV").toString()
                tr.cantidadRes = transferenciaReabastecimientoObject.get("CANTIDAD_RES").toString()
                tr.codUnidMedRes = transferenciaReabastecimientoObject.get("COD_UNID_MED_RES").toString()
                tr.codDeposito = transferenciaReabastecimientoObject.get("COD_DEPOSITO").toString()
                tr.fecVencimiento = transferenciaReabastecimientoObject.get("FEC_VENCIMIENTO").toString()
                tr.fecVencim01 = transferenciaReabastecimientoObject.get("FEC_VENCIM_01").toString()
                tr.cantidadUbRes = transferenciaReabastecimientoObject.get("CANTIDAD_UB_RES").toString()
                tr.cantidadUb = transferenciaReabastecimientoObject.get("CANTIDAD_UB").toString()
                tr.relacion = transferenciaReabastecimientoObject.get("RELACION").toString()
                tr.nroLote = transferenciaReabastecimientoObject.get("NRO_LOTE").toString()

                transferenciaReabastecimiento.add(tr)


            }


            val gridLayoutManager = GridLayoutManager(context, 1)

            rvTransferencias.layoutManager = gridLayoutManager
            rvTransferencias.itemAnimator = DefaultItemAnimator()
            rvTransferencias.setHasFixedSize(true)


            // this creates a vertical layout Manager
            rvTransferencias.layoutManager = LinearLayoutManager(context)

            // This loop will create 20 Views containing
            // the image with the count of view
            // This will pass the ArrayList to our Adapter
            val adapter = AdapterTransferenciaReabastecimientoCorrPrev(
                context,
                transferenciaReabastecimiento,
                R.layout.card_view_confirmar_reabast_prev  )

            // Setting the Adapter with the recyclerview
            rvTransferencias.adapter = adapter




        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (obtieneListaTransferenciasArticulo)")


        }

    }



    fun valida_direccion_origen() : Boolean {


        var resu = false

        var cTexto = etCodigoBarraList.text.toString().replace("\n", "")
        if(cTexto.trim().isEmpty()){
            return false
        }

        etCodigoBarraList.setText(cTexto)
        etCodigoBarraList.setSelection(etCodigoBarraList.text.toString().length)


        transferenciaReabastecimiento.forEachIndexed { index, it ->

            if (it.dirOrigen == etCodigoBarraList.text.toString()) {

                if (it.relacion.trim() == "0") {
                    posicionTransferencia = index
                    resu=true
                }

                if (!resu && it.relacion.trim() != "0") {
                    resu=true
                    posicionTransferencia = index
                }


            }

        }


        return resu


    }



    fun valida_es_palet(direccion: String) : Boolean {


        var resu = false


        var cTexto = direccion
        if(cTexto.trim().isEmpty()){
            return false
        }

        etCodigoBarraList.setText(cTexto)
        etCodigoBarraList.setSelection(etCodigoBarraList.text.toString().length)


        transferenciaReabastecimiento.forEachIndexed { index, it ->

            if (it.dirOrigen == etCodigoBarraList.text.toString()) {

                if (it.tipMov.trim() == "A") {
                    posicionTransferencia = index
                    resu=true
                }

            }

        }


        return resu


    }





    fun abreCuadroArticulo() {

        try {
            dialogo_lee_codigo.dismiss()
        } catch (e: Exception) {
        }

        dialogo_lee_codigo = Dialog(context)
        dialogo_lee_codigo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_lee_codigo.setContentView(R.layout.lector_codigo_articulo)


        /*codigo de barra de producto*/
        dialogo_lee_codigo.etCodigoBarraArt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().indexOf("\n") > -1) {
                    dialogo_lee_codigo.etDesvioFocusArt.requestFocus()
                }

            }
        })


        dialogo_lee_codigo.etCodigoBarraArt.setOnFocusChangeListener { view, _ ->


            if (!view.hasFocus()) {

                buscarDetalleArt()

            }

        }

        dialogo_lee_codigo.btn_volver.setOnClickListener {
            dialogo_lee_codigo.dismiss()
        }

        dialogo_lee_codigo.btnAceptarArt.setOnClickListener {
            dialogo_lee_codigo.dismiss()
        }

        dialogo_lee_codigo.setCanceledOnTouchOutside(false)
        dialogo_lee_codigo.show()
        dialogo_lee_codigo.etCodigoBarraArt.requestFocus()

        rvTransferencias.adapter!!.notifyDataSetChanged()

    }


    fun buscarDetalleArt() {

        dialogo_lee_codigo.etCodigoBarraArt.setText(dialogo_lee_codigo.etCodigoBarraArt.text.toString().replace("\n", ""))
        dialogo_lee_codigo.etCodigoBarraArt.setSelection(dialogo_lee_codigo.etCodigoBarraArt.text.toString().length)

        var filtro = dialogo_lee_codigo.etCodigoBarraArt.text.toString()

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("CODIGO", filtro)
            .build()

        var result = HttpRequest.call("", "reabastecimientocorrprev/busca_detalle_articulo", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (busca_detalle_articulo) Error ${e.message.toString()} !")
            return
        }

        var artAdicional = ""
        var codigo = ""

        if (respuestaJson.has("rows")) {
            val filasArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filasArray.length()) {
                val filaObject : JSONObject = filasArray.get(i) as JSONObject
                codigo = filaObject.get("COD_ARTICULO").toString()
                artAdicional = filaObject.get("ART_ADICIONAL").toString()


            }



            var tr = transferenciaReabastecimiento[posicionTransferencia]

            if (tr.codArticulo == codigo) {

                abreCuadroDirDestino2()

                if (artAdicional == "S") {

                    SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("¡Atención!")
                        .setContentText("Verifique las partes del artículo. \nEl artículo seleccionado tiene más de un volumen.")
                        .setConfirmText("OK")
                        .setConfirmClickListener { sDialog ->

                            sDialog.dismissWithAnimation()

                        }
                        .show()

                }

            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "Artículo no corresponde")
            }




        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (buscarDetalleArt)")


        }


    }


    fun abreCuadroDirDestino2() {


        try {
            dialogo_confirma.dismiss()
        } catch (e: Exception) {
        }

        dialogo_confirma = Dialog(context)
        dialogo_confirma.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_confirma.setContentView(R.layout.lector_codigo_pos_reb_corprev2)

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var tip_mov_act = tr.tipMov
        var _cant_res = tr.cantidadRes.toInt()
        var _cant_act = tr.cantidad.toInt()

        var _saldo = 0
        if(tr.tipMov == "C" ){
        }else{
            if(tr.tipMov == "A" ){
                _saldo = 0

            } else {
                _saldo = _cant_res - _cant_act

            }
        }

        dialogo_confirma.tvDirDestino.text = tr.dirDestino
        dialogo_confirma.tvDirDestino2.text = tr.dirOrigen
        dialogo_confirma.tvCant1.text = "${tr.cantidad}  ${tr.descUnidadMedida}"
        dialogo_confirma.tvCant2.text = "${_saldo}  ${tr.descUnidadMedida}"
        direccion_origen_base = tr.dirOrigen

        if(tip_mov_act == "R"){
            if(_saldo < 1){
                habilitarDestino2(false)
            }
        } else {
            habilitarDestino2(false)
        }


        /*codigo de barra de producto*/
        dialogo_confirma.etCodBarraDir.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().indexOf("\n") > -1) {
                    dialogo_confirma.etDesvioFocusDir.requestFocus()
                }

            }
        })



        dialogo_confirma.btnConfirmarList.setOnClickListener {
            var _destino1 = dialogo_confirma.etCodBarraDir.text.toString().trim()
            var _destino2 = dialogo_confirma.etCodBarraDir2.text.toString().trim()

            if(_destino1.isEmpty()){
                MainActivity.funciones.mensajeError(context, "Atencion", "Ingrese dirección destino 1!!")
                return@setOnClickListener
            }

            if(_destino2.isEmpty()){
                MainActivity.funciones.mensajeError(context, "Atencion", "Ingrese dirección destino 2!!")
                return@setOnClickListener
            }

            if(!validaDireccionDestino1()){
                MainActivity.funciones.mensajeError(context, "Atencion", "Direccion de Destino 1 No corresponde")
                return@setOnClickListener
            }

            if(validaMismaDireccionOrigen(_destino2)){
                //si es misma dirección
                iniciaTratamiento()

            } else{

                if(!validaDireccionReabast(_destino2)){
                    return@setOnClickListener
                }

                if(tipo_operacion_reabast != null){
                    if(!tipo_operacion_reabast.equals("PL_PL")){
                        return@setOnClickListener
                    }
                }else{
                    return@setOnClickListener
                }

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atencion!")
                    .setContentText("¿Está seguro en enviar el saldo a una nueva dirección?")
                    .setConfirmText("Si")
                    .setConfirmClickListener { sDialog ->

                        iniciaTratamiento()
                        sDialog.dismissWithAnimation()

                    }
                    .setCancelButton(
                        "No"
                    ) { sDialog -> sDialog.dismissWithAnimation() }
                    .show()

            }


        }


        dialogo_confirma.btnCancelarList.setOnClickListener {
            dialogo_confirma.dismiss()
        }


        dialogo_confirma.setCanceledOnTouchOutside(false)
        dialogo_confirma.show()



    }


    fun habilitarDestino2(estado: Boolean) {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        dialogo_confirma.etCodBarraDir2.setText(tr.dirOrigen)
        dialogo_confirma.etCodBarraDir2.isEnabled = estado

    }


    fun validaDireccionDestino1() : Boolean {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var resu = false

        dialogo_confirma.etCodBarraDir.setText(dialogo_confirma.etCodBarraDir.text.toString().replace("\n", ""))
        dialogo_confirma.etCodBarraDir.setSelection(dialogo_confirma.etCodBarraDir.text.toString().length)

        var direccion_destino = dialogo_confirma.etCodBarraDir.text.toString().trim()

        if(direccion_destino == tr.dirDestino){
            resu = true
        }
        return resu


    }


    fun validaMismaDireccionOrigen(dirIngresado: String) : Boolean {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var resu = false

        if (dirIngresado == tr.dirOrigen) {
            resu = true
        }
        return resu

    }


    fun iniciaTratamiento() {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var resu = false
        relacion = tr.nroOrden
        articuloRelacionado = tr.codArticulo
        nroComprobante = tr.nroComprobante

        if (tratamientoReabast()) {
            try {
                resu=true
                if(realizar_doble_proceso){
                    obtieneListaTransferenciasNew()
                    resu = confirmaReabascorprevPalmNew()

                }
            } catch (e: SQLException) {

                resu=false

            } finally {

                try {

                    if(!resu){

                        MainActivity.funciones.mensajeError(context, "Atencion", "Error al confirmar, vuelva a intentar")

                    }

                } catch (e: Exception) {

                    MainActivity.funciones.mensajeError(context, "Atencion", "Error al confirmar, vuelva a intentar")
                }

                try {
                    obtieneListaTransferencias()
                } catch (e: Exception) {

                    //e.printStackTrace();
                }

            }
        }

    }


    fun confirmaReabascorprevPalmNew() : Boolean {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var resu = false

        var dirOrigenAux = tr.dirOrigen
        if (tr.tipMov == "A") {
            dirOrigenAux = ""
        }

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", tr.tipComprobante)
            .add("SER_COMPROBANTE", tr.serComprobante)
            .add("NRO_COMPROBANTE", tr.nroComprobante)
            .add("NRO_ORDEN", tr.nroOrden)
            .add("COD_ARTICULO", tr.codArticulo)
            .add("COD_DIRECCION", dirOrigenAux)
            .add("COD_DIRECCION_DES", tr.dirDestino)
            .build()

        var result = HttpRequest.call("", "reabastecimientocorrprev/confirma_reabascorprev_palm2", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (confirma_reabascorprev_palm2) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            if(respuesta == "X"){
                MainActivity.funciones.mensajeError(context, "Atencion", "Error al finalizar Reabastecimiento")
            }else{

                SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("¡Atención!")
                    .setContentText("$respuesta")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->


                        sDialog.dismissWithAnimation()

                    }
                    .show()
                resu = true
            }

        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (confirmaReabascorprevPalm)")


        }


        return resu


    }


    fun obtieneListaTransferenciasNew() {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        var result = HttpRequest.call("", "reabastecimientocorrprev/obtiene_lista_transferencias2", formBody)


        posicionTransferencia = 0
        transferenciaReabastecimiento = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_lista_transferencias2) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {
            val transferenciaReabastecimientoArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until transferenciaReabastecimientoArray.length()) {
                val transferenciaReabastecimientoObject : JSONObject = transferenciaReabastecimientoArray.get(i) as JSONObject
                val tr = TransferenciaReabastecimientoCorrPrev()
                tr.codEmpresa = transferenciaReabastecimientoObject.get("COD_EMPRESA").toString()
                tr.tipComprobante = transferenciaReabastecimientoObject.get("TIP_COMPROBANTE").toString()
                tr.serComprobante = transferenciaReabastecimientoObject.get("SER_COMPROBANTE").toString()
                tr.nroComprobante = transferenciaReabastecimientoObject.get("NRO_COMPROBANTE").toString()
                tr.dirOrigen = transferenciaReabastecimientoObject.get("COD_DIRECCION").toString()
                tr.dirDestino = transferenciaReabastecimientoObject.get("COD_DIRECCION_DES").toString()
                tr.cantidad = transferenciaReabastecimientoObject.get("CANTIDAD").toString()
                tr.codArticulo = transferenciaReabastecimientoObject.get("COD_ARTICULO").toString()
                tr.descArticulo = transferenciaReabastecimientoObject.get("DESC_ARTICULO").toString()
                tr.unidadMedida = transferenciaReabastecimientoObject.get("COD_UNIDAD_MEDIDA").toString()
                tr.descUnidadMedida = transferenciaReabastecimientoObject.get("DESC_UN_MEDIDA").toString()
                tr.nroOrden = transferenciaReabastecimientoObject.get("NRO_ORDEN").toString()
                tr.estado = "P"
                tr.tipMov = transferenciaReabastecimientoObject.get("TIP_MOV").toString()
                tr.cantidadRes = transferenciaReabastecimientoObject.get("CANTIDAD_RES").toString()
                tr.codUnidMedRes = transferenciaReabastecimientoObject.get("COD_UNID_MED_RES").toString()
                tr.codDeposito = transferenciaReabastecimientoObject.get("COD_DEPOSITO").toString()
                tr.fecVencimiento = transferenciaReabastecimientoObject.get("FEC_VENCIMIENTO").toString()
                tr.fecVencim01 = transferenciaReabastecimientoObject.get("FEC_VENCIM_01").toString()
                tr.cantidadUbRes = transferenciaReabastecimientoObject.get("CANTIDAD_UB_RES").toString()
                tr.cantidadUb = transferenciaReabastecimientoObject.get("CANTIDAD_UB").toString()
                tr.relacion = transferenciaReabastecimientoObject.get("RELACION").toString()
                tr.nroLote = transferenciaReabastecimientoObject.get("NRO_LOTE").toString()

                transferenciaReabastecimiento.add(tr)


            }


            val gridLayoutManager = GridLayoutManager(context, 1)

            rvTransferencias.layoutManager = gridLayoutManager
            rvTransferencias.itemAnimator = DefaultItemAnimator()
            rvTransferencias.setHasFixedSize(true)


            // this creates a vertical layout Manager
            rvTransferencias.layoutManager = LinearLayoutManager(context)

            // This loop will create 20 Views containing
            // the image with the count of view
            // This will pass the ArrayList to our Adapter
            val adapter = AdapterTransferenciaReabastecimientoCorrPrev(
                context,
                transferenciaReabastecimiento,
                R.layout.card_view_confirmar_reabast_prev  )

            // Setting the Adapter with the recyclerview
            rvTransferencias.adapter = adapter


            etCodigoBarraList.requestFocus()
            if (validaTransferenciaRelacionada(relacion,articuloRelacionado,nroComprobante)) {
                abreCuadroDirDestinoRelacion()
            } else {
                relacion = ""
            }

        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaUnidadMedida)")

        }

    }


    fun validaTransferenciaRelacionada(dato: String, codArticulo: String, nroComprobante: String) : Boolean {

        var resu = false
        if (dato.trim().length === 0) {
            return false
        }

        transferenciaReabastecimiento.forEachIndexed { index, it ->

            if (it.relacion == dato && it.codArticulo == codArticulo
                && it.nroComprobante == nroComprobante && it.dirOrigen == direccion_origen_base) {

                posicionTransferencia = index
                resu = true

            }

        }


        return resu

    }

    fun abreCuadroDirDestinoRelacion() {

        try {
            dialogo_confirma.dismiss()
        } catch (e: Exception) {
        }

        dialogo_confirma = Dialog(context)
        dialogo_confirma.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_confirma.setContentView(R.layout.lector_codigo_pos_reb_corprev3)
        dialogo_confirma.setCancelable(false)

        dialogo_confirma.tvTituloRelacion.text = "Debe transferir el sobrante a otra direccion."

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var tip_mov_act = tr.tipMov
        var _cant_res = tr.cantidadRes.toInt()
        var _cant_act = tr.cantidad.toInt()

        var _saldo = 0
        _saldo = if(tr.tipMov == "C"){
            0
        }else{
            _cant_res - _cant_act
        }

        dialogo_confirma.tvDirDestino.text = tr.dirDestino
        if(tr.dirDestino.trim() == ""){
            dialogo_confirma.tvDirDestino.text = "Ingrese la direccion de destino"
        }
        dialogo_confirma.tvCant1.text = "${tr.cantidad}  ${tr.descUnidadMedida}"
        dialogo_confirma.tvCant2.text = "${_saldo}  ${tr.descUnidadMedida}"
        direccion_origen_base = tr.dirOrigen

        habilitarDestino2(false)

        dialogo_confirma.btnConfirmarList.setOnClickListener {

            var _destino1 = dialogo_confirma.etCodBarraDir.text.toString().trim()
            var _destino2 = dialogo_confirma.etCodBarraDir2.text.toString().trim()

            if(_destino1.isEmpty()){

                MainActivity.funciones.mensajeError(context, "Atencion", "Ingrese dirección destino 1!!")
                return@setOnClickListener

            }

            if(_destino2.isEmpty()){

                MainActivity.funciones.mensajeError(context, "Atencion", "Ingrese dirección destino 2!!")
                return@setOnClickListener
            }

            if(validaMismaDireccionOrigen(_destino1)){

                eliminarRelacion()
                dialogo_confirma.dismiss()
                finalizaCuadroDirDestino()
                try {
                    obtieneListaTransferencias()
                } catch (e: Exception) {

                }

            } else {

                if(!validaDireccionReabast(_destino1)){
                    return@setOnClickListener
                }

                if(tipo_operacion_reabast!=null){
                    if(!tipo_operacion_reabast.equals("PL_PL")){
                        return@setOnClickListener
                    }
                }else{
                    return@setOnClickListener
                }

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atencion!")
                    .setContentText("¿Está seguro en enviar el saldo a una nueva dirección?")
                    .setConfirmText("Si")
                    .setConfirmClickListener { sDialog ->

                        tratamientoReabastRelacion(_destino1)
                        sDialog.dismissWithAnimation()

                    }
                    .setCancelButton(
                        "No"
                    ) { sDialog -> sDialog.dismissWithAnimation() }
                    .show()

            }


        }


        dialogo_confirma.btnCancelarList.isEnabled = false

        dialogo_confirma.setCanceledOnTouchOutside(false)
        dialogo_confirma.show()



    }


    fun tratamientoReabastRelacion(direccion: String) : Boolean {

        dialogo_confirma.etCodBarraDir2.setText(dialogo_confirma.etCodBarraDir2.text.toString().replace("\n", ""))
        dialogo_confirma.etCodBarraDir2.setSelection(dialogo_confirma.etCodBarraDir2.text.toString().length)


        dialogo_confirma.dismiss()

        finalizaCuadroDirDestino()

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        try {
            confirmaReabascorprevPalmRelacion(direccion)
            obtieneListaTransferencias()
        } catch (e: Exception) {

            MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
            return false
        }



        return true

    }

    fun confirmaReabascorprevPalmRelacion(direccion: String) {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", tr.codDeposito)
            .add("TIP_COMPROBANTE", tr.tipComprobante)
            .add("SER_COMPROBANTE", tr.serComprobante)
            .add("NRO_COMPROBANTE", tr.nroComprobante)
            .add("NRO_ORDEN", tr.nroOrden)
            .add("COD_ARTICULO", tr.codArticulo)
            .add("COD_DIRECCION", tr.dirOrigen)
            .add("COD_DIRECCION_DES", direccion)
            .build()

        var result = HttpRequest.call("", "reabastecimientocorrprev/confirma_reabascorprev_palm_relacion", formBody)
        result = result

    }


    fun eliminarRelacion() {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", tr.codDeposito)
            .add("TIP_COMPROBANTE", tr.tipComprobante)
            .add("SER_COMPROBANTE", tr.serComprobante)
            .add("NRO_COMPROBANTE", tr.nroComprobante)
            .add("NRO_ORDEN", tr.nroOrden)
            .add("COD_ARTICULO", tr.codArticulo)
            .add("COD_DIRECCION", tr.dirOrigen)
            .build()

        var result = HttpRequest.call("", "reabastecimientocorrprev/eliminar_relacion", formBody)



    }



    fun tratamientoReabast() : Boolean {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var resu = false
        dialogo_confirma.etCodBarraDir2.setText(dialogo_confirma.etCodBarraDir2.text.toString().replace("\n", ""))
        dialogo_confirma.etCodBarraDir2.setSelection(dialogo_confirma.etCodBarraDir2.text.toString().length)

        var _destino2 = dialogo_confirma.etCodBarraDir2.text.toString().trim()

        dialogo_confirma.dismiss()

        finalizaCuadroDirDestino()

        try {
            if (confirmaReabascorprevPalm()) {

                resu = true
                if(tr.tipMov == "R"){

                    var _saldo  = tr.cantidadRes.toDouble() - tr.cantidad.toDouble()

                    if(_saldo>0){
                        if(validaMismaDireccionOrigen(_destino2)){
                            //  vacio  //
                            realizar_doble_proceso=false
                            resu = true
                        }else{
                            realizar_doble_proceso=true

                            resu = insertaReabastDet(_saldo.toString(), _destino2)
                        }
                    }
                }

            } else {

                resu = false
                MainActivity.funciones.mensajeError(context, "Atencion", "Error al confirmar, vuelva a confirmar")

            }

            etCodigoBarraList.selectAll()

        } catch (e: Exception) {

        }

        return resu

    }

    fun insertaReabastDet(ingresado: String, codDireccionDes: String) : Boolean {


        var ingre = 0

        try {
            ingre =  ingresado.toDouble().toInt()
        } catch (e: Exception) {

        }

        var resu = false

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        try {

            var mensaje = "REGISTRADO CON EXITO"

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_DEPOSITO", tr.codDeposito)
                .add("TIP_COMPROBANTE", tr.tipComprobante)
                .add("SER_COMPROBANTE", tr.serComprobante)
                .add("NRO_COMPROBANTE", tr.nroComprobante)
                .add("COD_DIRECCION", tr.dirOrigen)
                .add("COD_DIRECCION_DES", codDireccionDes)
                .add("COD_ARTICULO", tr.codArticulo)
                .add("COD_UNIDAD_MEDIDA", tr.unidadMedida)
                .add("CANTIDAD", ingre.toString())
                .add("FEC_VENCIMIENTO", tr.fecVencimiento)
                .build()

            var result = HttpRequest.call("", "reabastecimientocorrprev/inserta_reabast_det", formBody)


            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (inserta_reabast_det) Error ${e.message.toString()} !")
                return false
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

                resu = true

            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (insertaReabastDet)")

            }

        } catch (e: Exception) {

        }


        return resu



    }


    fun confirmaReabascorprevPalm() : Boolean {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var resu = false


        var dirOrigenAux = tr.dirOrigen
        if (tr.tipMov == "A") {
            dirOrigenAux = ""
        }

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", tr.tipComprobante)
            .add("SER_COMPROBANTE", tr.serComprobante)
            .add("NRO_COMPROBANTE", tr.nroComprobante)
            .add("NRO_ORDEN", tr.nroOrden)
            .add("COD_ARTICULO", tr.codArticulo)
            .add("COD_DIRECCION", dirOrigenAux)
            .add("COD_DIRECCION_DES", tr.dirDestino)
            .build()

        var result = HttpRequest.call("", "reabastecimientocorrprev/confirma_reabascorprev_palm2", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (confirma_reabascorprev_palm2) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            if(respuesta == "X"){
                MainActivity.funciones.mensajeError(context, "Atencion", "Error al finalizar Reabastecimiento")
            }else{

                SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("¡Atención!")
                    .setContentText("$respuesta")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        sDialog.dismissWithAnimation()

                    }
                    .show()
                resu = true
            }

        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (confirmaReabascorprevPalm)")


        }


        return resu

    }


    fun finalizaCuadroDirDestino() {

        try {
            dialogo_confirma.dismiss()
        } catch (e: java.lang.Exception) {
        }

        try {
            dialogo_lee_codigo.dismiss()
        } catch (e: java.lang.Exception) {
        }

    }


    fun validaDireccionReabast(direccionDes: String) : Boolean {

        var tr = transferenciaReabastecimiento[posicionTransferencia]

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", tr.codDeposito)
            .add("COD_DIRECCION_DES", direccionDes)
            .add("COD_ARTICULO", tr.codArticulo)
            .add("COD_DIRECCION_ORI", tr.dirOrigen)
            .build()

        var result = HttpRequest.call("", "reabastecimientocorrprev/valida_direccion_reabast", formBody)

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


            return if (m_resp[0].trim() == "S") {
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



}