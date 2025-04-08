package com.apolo.wms.operaciones.movimiento
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.movimiento.MovimientoHV
import com.apolo.wms.operaciones.movimiento.adapter.AdapterMovimientoHorizontal
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.lector_codigo_articulo.*
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.*
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.btnConfirmarList
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.etCodBarraDir
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.etCodBarraDir2
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.etDesvioFocusDir
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.tvCant1
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.tvCant2
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.tvDirDestino2
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev2.tvTituloRelacion
import kotlinx.android.synthetic.main.lector_codigo_pos_reb_corprev3.*
import kotlinx.android.synthetic.main.list_movimiento_horizontal.btCancelarBusqueda
import kotlinx.android.synthetic.main.list_movimiento_horizontal.btnCancelarList
import kotlinx.android.synthetic.main.list_movimiento_horizontal.etBuscarArticulo
import kotlinx.android.synthetic.main.list_movimiento_horizontal.etCodigoBarraList
import kotlinx.android.synthetic.main.list_movimiento_horizontal.rvOperaciones
import kotlinx.coroutines.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.sql.SQLException
import java.util.ArrayList

class MovimientoHorizontal : AppCompatActivity() {
    companion object {

        lateinit var context : Context
        lateinit var rvOperacion : RecyclerView
        lateinit var progressBar: ProgressBar



        var operacionesPendientes = ArrayList<MovimientoHV>()
        var posicionOperacion = 0

        private var listaDireccionesDestino: MutableList<String> = mutableListOf()

        suspend fun obtieneListaOperaciones() {

            withContext(Dispatchers.Main){
                progressBar?.visibility = View.VISIBLE
            }
            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .build()

            var result = withContext(Dispatchers.IO) {
                HttpRequest.call("", "movimiento/buscar_movimiento_horizontal", formBody)
            }

            posicionOperacion = 0
            operacionesPendientes = ArrayList() // .clear()
            listaDireccionesDestino.clear()

            var respuestaJson= try {
                JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(
                    context,
                    "Atencion",
                    "Error en la respuesta del servidor. (buscar_movimiento_horizontal) Error ${e.message.toString()}!"
                )
                return
            }

            if (respuestaJson.has("rows")) {
                val operacionArray  = respuestaJson.getJSONArray("rows")

                for (i in 0 until operacionArray.length()) {
                    val operacionObject = operacionArray.getJSONObject(i)
                    val tr = MovimientoHV(). apply {
                        codEmpresa = operacionObject.getString("COD_EMPRESA")
                        tipComprobante = operacionObject.getString("TIP_COMPROBANTE")
                        serComprobante = operacionObject.getString("SER_COMPROBANTE")
                        nroComprobante = operacionObject.getString("NRO_COMPROBANTE")
                        dirOrigen = operacionObject.getString("COD_DIRECCION")
                        dirDestino = operacionObject.getString("COD_DIRECCION_DES")
                        cantidad = operacionObject.getString("CANTIDAD")
                        codArticulo = operacionObject.getString("COD_ARTICULO")
                        descArticulo = operacionObject.getString("DESC_ARTICULO")
                        unidadMedida = operacionObject.getString("COD_UNIDAD_MEDIDA")
                        descUnidadMedida = operacionObject.getString("DESC_UN_MEDIDA")
                        nroOrden = operacionObject.getString("NRO_ORDEN")
                        estado = operacionObject.getString("ESTADO")
                        tipMov = operacionObject.getString("TIP_MOV")
                        cantidadRes = operacionObject.getString("CANTIDAD_RES")
                        codUnidMedRes = operacionObject.getString("COD_UNID_MED_RES")
                        codDeposito = operacionObject.getString("COD_DEPOSITO")
                        codDepositoEnt = operacionObject.getString("COD_DEPOSITO_ENT")
                        fecVencimiento = operacionObject.getString("FEC_VENCIMIENTO")
                        fecVencim01 = operacionObject.getString("FEC_VENCIM_01")
                        cantidadUbRes = operacionObject.getString("CANTIDAD_UB_RES")
                        cantidadUb = operacionObject.getString("CANTIDAD_UB")
                        relacion = operacionObject.getString("RELACION")
                        nroLote = operacionObject.getString("NRO_LOTE")
                    }

                    operacionesPendientes.add(tr)
                    if (!listaDireccionesDestino.contains(tr.dirDestino)){
                        listaDireccionesDestino.add(tr.dirDestino)
                    }
                }
                withContext(Dispatchers.Main) {
                    rvOperacion.apply {
                        layoutManager = LinearLayoutManager(context)
                        itemAnimator = DefaultItemAnimator()
                        setHasFixedSize(true)
                        adapter = AdapterMovimientoHorizontal(
                            context,
                            operacionesPendientes,
                            R.layout.card_view_movimiento_horizontal
                        )
                    }
                    progressBar?.visibility = View.GONE
                }

            } else {
                withContext(Dispatchers.Main){
                    MainActivity.funciones.mensajeError(
                        context,
                        "Atencion",
                        "Ocurrio un error en la respuesta del servidor. (obtieneListaOperaciones)"
                    )
                    progressBar?.visibility = View.GONE
                }

            }

        }

        fun elimina_det(tr: MovimientoHV) : Boolean {
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
            var result = HttpRequest.call("", "movimiento/elimina_movimiento_det", formBody)
            var respuestaJson: JSONObject
            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (elimina_movimiento_det) Error ${e.message.toString()} !")
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
                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (elimina_movimiento_det)")
            }
            return resu
        }
    }

    var direccion_origen_base = ""
    var realizar_doble_proceso = false

    var tipo_operacion_reabast: String? = null

    private lateinit var dialogo_lee_codigo: Dialog

    private lateinit var dialogo_confirma: Dialog
    var relacion = ""
    var articuloRelacionado = ""
    var nroComprobante = ""
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_movimiento_horizontal)
        progressBar =findViewById(R.id.progressBar)
        inicializar()
    }
    private fun inicializar() {
        context = this
        title = "MOVIMIENTO HORIZONTAL"
        realizar_doble_proceso=false
        Companion.rvOperacion = rvOperaciones
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
                            if (operacionesPendientes[posicionOperacion].relacion == "0") {
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
                        //etCodigoBarraList.requestFocus()
                        if (!etCodigoBarraList.hasFocus()) {
                            etCodigoBarraList.requestFocus()
                        }
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
//                        GlobalScope.launch {
                        obtieneListaOperacionesArticulo()
                        //                        }

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
            GlobalScope.launch {
                try{
                    obtieneListaOperaciones()
                } catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
        GlobalScope.launch {
            obtieneListaOperaciones()
        }

    }
    fun valida_es_palet(direccion: String) : Boolean {
        var resu = false
        var cTexto = direccion
        if(cTexto.trim().isEmpty()){
            return false
        }
        etCodigoBarraList.setText(cTexto)
        etCodigoBarraList.setSelection(etCodigoBarraList.text.toString().length)
        operacionesPendientes.forEachIndexed { index, it ->
            if (it.dirOrigen == etCodigoBarraList.text.toString()) {
                if (it.tipMov.trim() == "A") {
                    posicionOperacion = index
                    resu = true
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
        rvOperacion.adapter!!.notifyDataSetChanged()
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
        var result = HttpRequest.call("", "movimiento/busca_detalle_articulo", formBody)
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
            var tr = operacionesPendientes[posicionOperacion]
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
        dialogo_confirma.setContentView(R.layout.lector_codigo_pos_reb_corprev1)

        var tr = operacionesPendientes[posicionOperacion]

        var DirDes : Spinner = dialogo_confirma.findViewById(R.id.tvDirDestino)
        var adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaDireccionesDestino)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        DirDes.adapter = adapter
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
//        dialogo_confirma.tvDirDestino.text = tr.dirDestino
//        dialogo_confirma.findViewById<TextView>(R.id.tvDirDestino) = DirDes.dirDestino
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
                if(!validaDireccionMovimientoHorizontal(_destino2)){
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
        var tr = operacionesPendientes[posicionOperacion]
        dialogo_confirma.etCodBarraDir2.setText(tr.dirOrigen)
        dialogo_confirma.etCodBarraDir2.isEnabled = estado
    }
    fun validaDireccionDestino1() : Boolean {
        var tr = operacionesPendientes[posicionOperacion]
        var resu = false
        dialogo_confirma.etCodBarraDir.setText(
            dialogo_confirma.etCodBarraDir.text.toString().replace("\n", "")
        )
        dialogo_confirma.etCodBarraDir.setSelection(dialogo_confirma.etCodBarraDir.text.toString().length)
        var direccion_destino = dialogo_confirma.etCodBarraDir.text.toString().trim()
        if (direccion_destino == tr.dirDestino) {
            resu = true
        }
        return resu
    }
    fun validaMismaDireccionOrigen(dirIngresado: String) : Boolean {
        var tr = operacionesPendientes[posicionOperacion]
        var resu = false
        if (dirIngresado == tr.dirOrigen) {
            resu = true
        }
        return resu
    }
    fun iniciaTratamiento() {
        var tr = operacionesPendientes[posicionOperacion]
        var resu = false
        relacion = tr.nroOrden
        articuloRelacionado = tr.codArticulo
        nroComprobante = tr.nroComprobante
        if (tratamientoReabast()) {
            try {
                resu=true
                if(realizar_doble_proceso){
                    //obtieneListaTransferenciasNew()
                    //resu = confirmaReabascorprevPalmNew()
                    GlobalScope.launch {
                        obtieneListaOperaciones()
                    }
                    //verificar si hay alguna relacion
                    if (validaTransferenciaRelacionada(relacion,articuloRelacionado,nroComprobante)) {
                        abreCuadroDirDestinoRelacion()
                    } else {
                        relacion = ""
                    }
                    resu = confirmaMovimientoHorizontal()
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
                    GlobalScope.launch {
                        obtieneListaOperaciones()
                    }
                } catch (e: Exception) {
                    //e.printStackTrace();
                }
            }
        }
    }
    fun validaTransferenciaRelacionada(dato: String, codArticulo: String, nroComprobante: String) : Boolean {
        var resu = false
        if (dato.trim().length === 0) {
            return false
        }
        operacionesPendientes.forEachIndexed { index, it ->
            if (it.relacion == dato && it.codArticulo == codArticulo
                && it.nroComprobante == nroComprobante && it.dirOrigen == direccion_origen_base) {
                posicionOperacion = index
                resu = true
            }
        }
        return resu
    }
    fun tratamientoReabast() : Boolean {
        var tr = operacionesPendientes[posicionOperacion]
        var resu = false
        dialogo_confirma.etCodBarraDir2.setText(dialogo_confirma.etCodBarraDir2.text.toString().replace("\n", ""))
        dialogo_confirma.etCodBarraDir2.setSelection(dialogo_confirma.etCodBarraDir2.text.toString().length)
        var _destino2 = dialogo_confirma.etCodBarraDir2.text.toString().trim()
        dialogo_confirma.dismiss()
        finalizaCuadroDirDestino()
        try {
            if (confirmaMovimientoHorizontal()) {
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
    fun confirmaMovimientoHorizontal() : Boolean {

        var tr = operacionesPendientes[posicionOperacion]
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
        var result = HttpRequest.call("", "movimiento/confirma_movimiento_horizontal", formBody)
        var respuestaJson: JSONObject
        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (confirma_movimiento_horizontal) Error ${e.message.toString()} !")
            return false
        }
        if (respuestaJson.has("respuesta")) {
            val respuesta = respuestaJson.get("respuesta").toString()
            if(respuesta == "X"){
                MainActivity.funciones.mensajeError(context, "Atencion", "Error al finalizar Movimiento Horizontal")
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
    fun insertaReabastDet(ingresado: String, codDireccionDes: String) : Boolean {

        var resu = false

        var tr = operacionesPendientes[posicionOperacion]

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
                .add("CANTIDAD", ingresado)
                .add("FEC_VENCIMIENTO", tr.fecVencimiento)
                .build()

            var result = HttpRequest.call("", "movimiento/inserta_reabast_det", formBody)


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
    fun validaDireccionMovimientoHorizontal(direccionDes: String) : Boolean {
        var tr = operacionesPendientes[posicionOperacion]
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
        var result = HttpRequest.call("", "movimiento/valida_direccion_reabast", formBody)
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
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaDireccionMovHorizontal)")
            return false
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
        operacionesPendientes.forEachIndexed { index, it ->

            if (it.dirOrigen == etCodigoBarraList.text.toString()) {

                if (it.relacion.trim() == "0") {
                    posicionOperacion = index
                    resu=true
                }

                if (!resu && it.relacion.trim() != "0") {
                    resu=true
                    posicionOperacion = index
                }
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
        var tr = operacionesPendientes[posicionOperacion]
        var tip_mov_act = tr.tipMov
        var _cant_res = tr.cantidadRes.toInt()
        var _cant_act = tr.cantidad.toInt()
        var _saldo = 0
        _saldo = if (tr.tipMov == "C") {
            0
        } else {
            _cant_res - _cant_act
        }
        dialogo_confirma.tvDireDestino.text = tr.dirDestino
        if (tr.dirDestino.trim() == "") {
            dialogo_confirma.tvDireDestino.text = "Ingrese la direccion de destino"
        }
        dialogo_confirma.tvCant1.text = "${tr.cantidad}  ${tr.descUnidadMedida}"
        dialogo_confirma.tvCant2.text = "${_saldo}  ${tr.descUnidadMedida}"
        direccion_origen_base = tr.dirOrigen
        habilitarDestino2(false)
        dialogo_confirma.btnConfirmarList.setOnClickListener {
            var _destino1 = dialogo_confirma.etCodBarraDir.text.toString().trim()
            var _destino2 = dialogo_confirma.etCodBarraDir2.text.toString().trim()
            if (_destino1.isEmpty()) {
                MainActivity.funciones.mensajeError(
                    context,
                    "Atencion",
                    "Ingrese dirección destino 1!!"
                )
                return@setOnClickListener
            }
            if (_destino2.isEmpty()) {

                MainActivity.funciones.mensajeError(
                    context,
                    "Atencion",
                    "Ingrese dirección destino 2!!"
                )
                return@setOnClickListener
            }
            if (validaMismaDireccionOrigen(_destino1)) {
                eliminarRelacion()
                dialogo_confirma.dismiss()
                finalizaCuadroDirDestino()
                try {
                    GlobalScope.launch {
                        obtieneListaOperaciones()
                    }

                } catch (e: Exception) {
                }
            } else {
                if (!validaDireccionMovimientoHorizontal(_destino1)) {
                    return@setOnClickListener
                }
                if (tipo_operacion_reabast != null) {
                    if (!tipo_operacion_reabast.equals("PL_PL")) {
                        return@setOnClickListener
                    }
                } else {
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
        var tr = operacionesPendientes[posicionOperacion]
        try {
            confirmaMovimientoHorizontalRelacion(direccion)
            GlobalScope.launch {
                obtieneListaOperaciones()
            }
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
            return false
        }
        return true
    }
    fun confirmaMovimientoHorizontalRelacion(direccion: String) {
        var tr = operacionesPendientes[posicionOperacion]
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", tr.codDeposito)
            .add("TIP_COMPROBANTE", tr.tipComprobante)
            .add("SER_COMPROBANTE", tr.serComprobante)
            .add("NRO_COMPROBANTE", tr.nroComprobante)
            .add("NRO_ORDEN", tr.dirOrigen)
            .add("COD_ARTICULO", tr.codArticulo)
            .add("COD_DIRECCION", tr.dirOrigen)
            .add("COD_DIRECCION_DES", direccion)
            .build()
        var result = HttpRequest.call("", "movimiento/confirma_movimiento_relacion", formBody)
    }
    fun eliminarRelacion() {
        var tr = operacionesPendientes[posicionOperacion]
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
        var result = HttpRequest.call("", "movimiento/eliminar_relacion", formBody)
    }
    fun obtieneListaOperacionesArticulo() {
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", etBuscarArticulo.text.toString().trim())
            .build()
        var result = HttpRequest.call("", "movimiento/buscar_movimiento_horizontal_articulo", formBody)
        posicionOperacion = 0
        operacionesPendientes = ArrayList()
        var respuestaJson: JSONObject
        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_lista_transferencias) Error ${e.message.toString()} !")
            return
        }
        if (respuestaJson.has("rows")) {
            val operacionArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until operacionArray.length()) {
                val operacionObject : JSONObject = operacionArray.get(i) as JSONObject
                val tr = MovimientoHV()
                tr.codEmpresa = operacionObject.get("COD_EMPRESA").toString()
                tr.tipComprobante = operacionObject.get("TIP_COMPROBANTE").toString()
                tr.serComprobante = operacionObject.get("SER_COMPROBANTE").toString()
                tr.nroComprobante = operacionObject.get("NRO_COMPROBANTE").toString()
                tr.dirOrigen = operacionObject.get("COD_DIRECCION").toString()
                tr.dirDestino = operacionObject.get("COD_DIRECCION_DES").toString()
                tr.cantidad = operacionObject.get("CANTIDAD").toString()
                tr.codArticulo = operacionObject.get("COD_ARTICULO").toString()
                tr.descArticulo = operacionObject.get("DESC_ARTICULO").toString()
                tr.unidadMedida = operacionObject.get("COD_UNIDAD_MEDIDA").toString()
                tr.descUnidadMedida = operacionObject.get("DESC_UN_MEDIDA").toString()
                tr.nroOrden = operacionObject.get("NRO_ORDEN").toString()
                tr.estado = operacionObject.get("ESTADO").toString()
                tr.tipMov = operacionObject.get("TIP_MOV").toString()
                tr.cantidadRes = operacionObject.get("CANTIDAD_RES").toString()
                tr.codUnidMedRes = operacionObject.get("COD_UNID_MED_RES").toString()
                tr.codDeposito = operacionObject.get("COD_DEPOSITO").toString()
                tr.codDepositoEnt = operacionObject.get("COD_DEPOSITO_ENT").toString()
                tr.fecVencimiento = operacionObject.get("FEC_VENCIMIENTO").toString()
                tr.fecVencim01 = operacionObject.get("FEC_VENCIM_01").toString()
                tr.cantidadUbRes = operacionObject.get("CANTIDAD_UB_RES").toString()
                tr.cantidadUb = operacionObject.get("CANTIDAD_UB").toString()
                tr.relacion = operacionObject.get("RELACION").toString()
                tr.nroLote = operacionObject.get("NRO_LOTE").toString()
                operacionesPendientes.add(tr)
            }
            val gridLayoutManager = GridLayoutManager(context, 1)
            rvOperacion.layoutManager = gridLayoutManager
            rvOperacion.itemAnimator = DefaultItemAnimator()
            rvOperacion.setHasFixedSize(true)
            rvOperacion.layoutManager = LinearLayoutManager(context)
            val adapter = AdapterMovimientoHorizontal(
                context,
                operacionesPendientes,
                R.layout.card_view_movimiento_horizontal  )
            rvOperacion.adapter = adapter
        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (obtieneListaOperacionesArticulo)")

        }
    }
}