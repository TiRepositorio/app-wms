package com.apolo.wms.operaciones.entrada

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.entrada.*
import com.apolo.wms.operaciones.entrada.adapter.AdapterEntradaConferido
import com.apolo.wms.operaciones.inventario.DetallePlanillaInventario
import com.apolo.wms.utilidades.*
import com.apolo.wms.utilidades.Adapter
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.entrada_conferencia.*
import kotlinx.android.synthetic.main.entrada_conferencia.btnCancelar
import kotlinx.android.synthetic.main.entrada_conferencia.btnConfirmar
import kotlinx.android.synthetic.main.entrada_conferencia.etCantidad
import kotlinx.android.synthetic.main.entrada_conferencia.etDestino
import kotlinx.android.synthetic.main.entrada_conferencia.etDesvioFocus
import kotlinx.android.synthetic.main.entrada_conferencia.etLote
import kotlinx.android.synthetic.main.entrada_conferencia.etVencimiento
import kotlinx.android.synthetic.main.entrada_conferencia.spAnomalias
import kotlinx.android.synthetic.main.entrada_conferencia.spDirecciones
import kotlinx.android.synthetic.main.entrada_conferencia.tvDescArticulo
import kotlinx.android.synthetic.main.entrada_conferencia.tvNorma
import kotlinx.android.synthetic.main.entrada_conferido.*
import kotlinx.android.synthetic.main.entrada_redireccion_ub.*
import kotlinx.android.synthetic.main.entrada_view_pager.*
import kotlinx.android.synthetic.main.lector_codigo_trasnferencia.*
import kotlinx.android.synthetic.main.lista_anomalias_articulos.*
import kotlinx.android.synthetic.main.lista_reconferencias.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat


class EntradaMercaderia : AppCompatActivity() {

    companion object {

        lateinit var context : Context
        lateinit var tabConferido: TextView
        
        var reconferenciaEntrada = ArrayList<ReconferenciaEntrada>()
        var articuloConferidoPlanillaEntrada = ArrayList<ArticuloConferidoPlanillaEntrada>()
        var anomaliaEntrada = ArrayList<AnomaliaEntrada>()
        var posicionAnomalia = 0
        var depositoEntrada = ArrayList<DepositoEntrada>()
        var posicionDeposito = 0
        var detalleArticuloEntrada = ArrayList<DetalleArticuloEntrada>()
        var codDepositoRem = ""
        var codMotivoRem = ""
        var unidadMedidaArticuloEntrada = ArrayList<UnidadMedidaArticuloEntrada>()
        var posicionUM = 0
        var anomaliaArticuloEntrada = ArrayList<AnomaliaArticuloEntrada>()
        var posicionAnomaliaArticulo = 0
        var tipoAlmacenamientoEntrada = ArrayList<TipoAlmacenamientoEntrada>()
        var posicionTipoAlmacenamiento = 0

        fun eliminarRecepcionMercaderia(articulo: ArticuloConferidoPlanillaEntrada) {

            val executorRunner = ExecutorRunner()
            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_DEPOSITO", BuscarPlanillaEntrada.planillaEntradaSeleccionada.codDeposito)
                .add("TIP_PLANILLA", BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante)
                .add("SER_PLANILLA", BuscarPlanillaEntrada.planillaEntradaSeleccionada.serComprobante)
                .add("NRO_PLANILLA", BuscarPlanillaEntrada.planillaEntradaSeleccionada.nroComprobante)
                .add("COD_ARTICULO", articulo.codArticulo)
                .add("COD_UNIDAD_MEDIDA", articulo.codUnidadMedida)
                .add("NRO_ORDEN", articulo.nroOrden)
                .add("NRO_LOTE", articulo.nroLote)
                .add("FEC_VENCIMIENTO", articulo.fecVencimiento)
                .build()

            executorRunner.execute(
                CallableWS("entrada/elimina_recepcion_mercaderia", formBody),
                object : ExecutorRunner.Callback<String> {
                    override fun onComplete(result: String) { // handle the result obtained from the asynchronous task


                        var respuestaJson: JSONObject

                        try {
                            respuestaJson = JSONObject(result)
                        } catch (e: Exception) {
                            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (elimina_recepcion_mercaderia) Error ${e.message.toString()} !")

                            return
                        }

                        if (respuestaJson.has("respuesta")) {
                            val respuesta = respuestaJson.get("respuesta").toString()

                            if (respuesta.indexOf("EXITO") > -1) {

                                tabConferido.callOnClick()
                                Toast.makeText(context, "ELIMINADO CON EXITO!!", Toast.LENGTH_SHORT).show()

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


    }


    private lateinit var dialog_buscar_reconferencias: Dialog
    private lateinit var dialog_anomalias_articulos: Dialog
    private lateinit var dialog_valida_preetiqueta: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entrada_view_pager)


        inicializar()

    }



    fun mostrarContenido(view: View) {
        tabConferencia.setBackgroundColor(Color.parseColor("#474747"))
        tabConferido.setBackgroundColor(Color.parseColor("#474747"))
        lTabConferencia.setBackgroundColor(Color.parseColor("#474747"))
        lTabConferidos.setBackgroundColor(Color.parseColor("#474747"))
        layoutEntradaConferencia.visibility = View.GONE
        layoutEntradaConferido.visibility = View.GONE
        view.setBackgroundColor(Color.parseColor("#116600"))
        if (view.id == tabConferencia.id){
            layoutEntradaConferencia.visibility = View.VISIBLE
            lTabConferencia.setBackgroundColor(Color.parseColor("#116600"))

            etDestino.requestFocus()


            if (BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante == "ENT") {
                tvDeposito.visibility = View.GONE
                spDeposito.visibility = View.GONE

                tvFabricacion.visibility = View.VISIBLE
                etFabricacion.visibility = View.VISIBLE

            } else {

                tvFabricacion.visibility = View.GONE
                etFabricacion.visibility = View.GONE

            }

        }
        if (view.id == tabConferido.id){

            layoutEntradaConferido.visibility = View.VISIBLE
            lTabConferidos.setBackgroundColor(Color.parseColor("#116600"))

            obtieneArticulosConferidosPlanilla()


        }


    }



    private fun obtieneArticulosConferidosPlanilla() {


        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante)
            .add("SER_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.serComprobante)
            .add("NRO_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.nroComprobante)
            .build()

        executorRunner.execute(
            CallableWS("entrada/obtiene_articulos_conferidos_planilla", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    articuloConferidoPlanillaEntrada = ArrayList()
                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_articulos_conferidos_planilla) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("rows")) {
                        val conferidoEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
                        for (i in 0 until conferidoEntradaArray.length()) {
                            val conferidoEntradaObject : JSONObject = conferidoEntradaArray[i] as JSONObject
                            val conf = ArticuloConferidoPlanillaEntrada()
                            conf.codArticulo = conferidoEntradaObject.get("COD_ARTICULO").toString()
                            conf.descArticulo = conferidoEntradaObject.get("DESC_ARTICULO").toString()
                            conf.codUnidadMedida = conferidoEntradaObject.get("COD_UNIDAD_MEDIDA").toString()
                            conf.descUnidadMedida = conferidoEntradaObject.get("DESC_UNIDAD_MEDIDA").toString()
                            conf.cantidad = conferidoEntradaObject.get("CANTIDAD").toString()
                            conf.nroLote = conferidoEntradaObject.get("NRO_LOTE").toString()
                            conf.fecVencimiento = conferidoEntradaObject.get("FEC_VENCIMIENTO").toString()
                            conf.anomalia = conferidoEntradaObject.get("ANOMALIA").toString()
                            conf.nroOrden = conferidoEntradaObject.get("NRO_ORDEN").toString()
                            conf.deposito = conferidoEntradaObject.get("DEPOSITO").toString()
                            articuloConferidoPlanillaEntrada.add(conf)
                        }
                    }



                    /*lvConferidos.adapter =
                        Adapter.AdapterGenericoCabecera(context, articuloConferidoPlanillaEntrada,
                            R.layout.list_text_entrada_conferencia,
                            intArrayOf( R.id.tvCodArticulo	   , R.id.tvDescArticulo, R.id.tvCodUnidadMedida,
                                R.id.tvDescUnidadMedida, R.id.tvCantidadDisp, R.id.tvAnomalia,
                                R.id.tvVencimiento     , R.id.tvDeposito),
                            arrayOf("COD_ARTICULO"	  	, "DESC_ARTICULO", "COD_UNIDAD_MEDIDA",
                                "DESC_UNIDAD_MEDIDA", "CANTIDAD"     , "ANOMALIA"         ,
                                "VENCIMIENTO"       , "DEPOSITO") )



                    lvConferidos.setOnItemClickListener { _, _, position, _ ->
                        FuncionesUtiles.posicionCabecera = position
                        lvConferidos.invalidateViews()
                    }*/

                    val gridLayoutManager = GridLayoutManager(context, 1)

                    rvConferidos.layoutManager = gridLayoutManager
                    rvConferidos.itemAnimator = DefaultItemAnimator()
                    rvConferidos.setHasFixedSize(true)


                    // this creates a vertical layout Manager
                    rvConferidos.layoutManager = LinearLayoutManager(
                        context
                    )

                    // This loop will create 20 Views containing
                    // the image with the count of view
                    // This will pass the ArrayList to our Adapter
                    val adapter = AdapterEntradaConferido(
                        context,
                        articuloConferidoPlanillaEntrada, R.layout.card_view_entrada_conferido  )

                    // Setting the Adapter with the recyclerview
                    rvConferidos.adapter = adapter


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


    fun inicializar() {


        context = this
        Companion.tabConferido = tabConferido

        ibtnBuscarPlanilla.setOnClickListener { buscar_reconferencias() }
        btnCancelar.setOnClickListener{ cancelar_conferencia() }
        btnConfirmar.setOnClickListener{ confirmar_conferencia() }
        btnCerrarConferencia.setOnClickListener { cerrar_conferencia() }

        tvNroComprobante.text = BuscarPlanillaEntrada.planillaEntradaSeleccionada.nroComprobante

        //TAB DE CONFERIDOS
        obtieneAnomalia()
        obtieneDeposito()
        obtieneTipoAlmacenamiento()




        //etLote.visibility = View.GONE


        /*codigo de barra de producto*/
        etDestino.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (layoutEntradaConferencia.visibility == View.VISIBLE) {
                    if (s.toString().indexOf("\n") > -1) {
                        etDesvioFocus.requestFocus()
                    }
                }
            }
        })
        etDestino.setOnFocusChangeListener { view, _ ->

            if (layoutEntradaConferencia.visibility == View.VISIBLE) {

                if (!view.hasFocus()) {
                    buscaDetalleArticulo()


                }

            }

        }

        mostrarContenido(tabConferencia)

        FuncionesUtiles.limitarDecimales(MainActivity.maximoDecimales, etCantidad)






    }

    private fun cerrar_conferencia() {

        SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Atencion")
            .setContentText("¿Desea Cerrar Conferencia?")
            .setConfirmText("Si")
            .setConfirmClickListener { sDialog ->

                val executorRunner = ExecutorRunner()
                var formBody: RequestBody = FormBody.Builder()
                    .add("USER", MainActivity.usuarioLogin.codUsuario)
                    .add("PASS", MainActivity.usuarioLogin.password)
                    .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                    .add("TIP_PLANILLA", BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante)
                    .add("SER_PLANILLA", BuscarPlanillaEntrada.planillaEntradaSeleccionada.serComprobante)
                    .add("NRO_PLANILLA", BuscarPlanillaEntrada.planillaEntradaSeleccionada.nroComprobante)
                    .build()

                executorRunner.execute(
                    CallableWS("entrada/cierre_conferencia_entrada", formBody),
                    object : ExecutorRunner.Callback<String> {
                        override fun onComplete(result: String) { // handle the result obtained from the asynchronous task


                            var respuestaJson: JSONObject

                            try {
                                respuestaJson = JSONObject(result)
                            } catch (e: Exception) {
                                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cierre_conferencia_entrada) Error ${e.message.toString()} !")

                                return
                            }

                            if (respuestaJson.has("respuesta")) {
                                val respuesta = respuestaJson.get("respuesta").toString()

                                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Atencion")
                                    .setContentText(respuesta)
                                    .setConfirmText("Si")
                                    .setConfirmClickListener { sDialog ->

                                        finish()
                                        sDialog.dismissWithAnimation()

                                    }
                                    .show()
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


                sDialog.dismissWithAnimation()

            }
            .setCancelButton(
                "No"
            ) { sDialog -> sDialog.dismissWithAnimation() }
            .show()



    }

    private fun buscaDetalleArticulo() {
        etDestino.setText(etDestino.text.toString().replace("\n", ""))

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_BARRA", etDestino.text.toString())
            .build()


        tvDescArticulo.text = ""
        etCantidad.inputType = InputType.TYPE_CLASS_NUMBER

        detalleArticuloEntrada = ArrayList()

        var result = HttpRequest.call("", "entrada/busca_detalle_articulo", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (busca_detalle_articulo) Error ${e.message.toString()} !")

            return
        }



        if (respuestaJson.has("rows")) {
            val detalleArticuloEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
            var direccion = ""

            for (i in 0 until detalleArticuloEntradaArray.length()) {
                val detalleArticuloEntradaObject : JSONObject = detalleArticuloEntradaArray.get(i) as JSONObject
                val da = DetalleArticuloEntrada()
                da.codArticulo = detalleArticuloEntradaObject.get("COD_ARTICULO").toString()
                da.descArticulo = detalleArticuloEntradaObject.get("DESCRIPCION").toString()
                da.indManejaVto = detalleArticuloEntradaObject.get("IND_MANEJA_VTO").toString()
                da.codDireccion = detalleArticuloEntradaObject.get("COD_DIRECCION").toString()
                da.unidad = detalleArticuloEntradaObject.get("UNIDAD").toString()
                da.codDeposito = detalleArticuloEntradaObject.get("COD_DEPOSITO").toString()
                da.artAdicional = detalleArticuloEntradaObject.get("ART_ADICIONAL").toString()
                da.esPesable = detalleArticuloEntradaObject.get("ES_PESABLE").toString()
                da.indManejaLotes = detalleArticuloEntradaObject.get("IND_MANEJA_LOTES").toString()
                da.indManejaFabricacion = detalleArticuloEntradaObject.get("IND_MANEJA_FABRICACION").toString()

                detalleArticuloEntrada.add(da)

                if (da.codDireccion.isNotEmpty()) {
                    direccion = da.codDireccion.substring(0, 3)+"-"+da.codDireccion.substring(3, 6)+"-"+da.codDireccion.substring(6, 7)+"-"+da.codDireccion.subSequence(7, 9)
                }

                tvDescripcionTitulo.text = "Descripcion: (COD:${da.codArticulo}) DIR: $direccion"
                tvDescArticulo.text = da.descArticulo


            }


        }




        if (detalleArticuloEntrada.size > 0) {

            val dal = detalleArticuloEntrada[detalleArticuloEntrada.size - 1]

            codDepositoRem = ""
            codMotivoRem = ""

            if (BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante == "REM" && dal.codArticulo.isNotEmpty()) {

                consulta_motivo_transferencia()

            }


            consulta_unidad_medida_articulo(dal)

            if (dal.artAdicional == "S") {
                MainActivity.funciones.mensajeError(context,
                    "Atencion",
                    "Verifique las partes del artículo. \nEl artículo seleccionado tiene más de un volumen.")
            }

            if (dal.esPesable == "S") {
                etCantidad.inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
            }

        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "ARTÍCULO NO LOCALIZADO!!")

        }







/*

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_BARRA", etDestino.text.toString())
            .build()

        executorRunner.execute(
            CallableWS("entrada/busca_detalle_articulo", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    tvDescArticulo.text = ""
                    etCantidad.inputType = InputType.TYPE_CLASS_NUMBER

                    detalleArticuloEntrada = ArrayList()
                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (busca_detalle_articulo) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("rows")) {
                        val detalleArticuloEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
                        var direccion = ""

                        for (i in 0 until detalleArticuloEntradaArray.length()) {
                            val detalleArticuloEntradaObject : JSONObject = detalleArticuloEntradaArray.get(i) as JSONObject
                            val da = DetalleArticuloEntrada()
                            da.codArticulo = detalleArticuloEntradaObject.get("COD_ARTICULO").toString()
                            da.descArticulo = detalleArticuloEntradaObject.get("DESCRIPCION").toString()
                            da.indManejaVto = detalleArticuloEntradaObject.get("IND_MANEJA_VTO").toString()
                            da.codDireccion = detalleArticuloEntradaObject.get("COD_DIRECCION").toString()
                            da.unidad = detalleArticuloEntradaObject.get("UNIDAD").toString()
                            da.codDeposito = detalleArticuloEntradaObject.get("COD_DEPOSITO").toString()
                            da.artAdicional = detalleArticuloEntradaObject.get("ART_ADICIONAL").toString()
                            da.esPesable = detalleArticuloEntradaObject.get("ES_PESABLE").toString()
                            da.indManejaLotes = detalleArticuloEntradaObject.get("IND_MANEJA_LOTES").toString()

                            detalleArticuloEntrada.add(da)

                            if (da.codDireccion.isNotEmpty()) {
                                direccion = da.codDireccion.substring(0, 3)+"-"+da.codDireccion.substring(3, 6)+"-"+da.codDireccion.substring(6, 7)+"-"+da.codDireccion.subSequence(7, 9)
                            }

                            tvDescripcionTitulo.text = "Descripcion: (COD:${da.codArticulo}) DIR: $direccion"
                            tvDescArticulo.text = da.descArticulo


                        }


                    }




                    if (detalleArticuloEntrada.size > 0) {

                        val dal = detalleArticuloEntrada[detalleArticuloEntrada.size - 1]

                        codDepositoRem = ""
                        codMotivoRem = ""

                        if (BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante == "REM" && dal.codArticulo.isNotEmpty()) {

                            consulta_motivo_transferencia()

                        }


                        consulta_unidad_medida_articulo(dal)

                        if (dal.artAdicional == "S") {
                            MainActivity.funciones.mensajeError(context,
                                "Atencion",
                                "Verifique las partes del artículo. \nEl artículo seleccionado tiene más de un volumen.")
                        }

                        if (dal.esPesable == "S") {
                            etCantidad.inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                        }

                    } else {

                        MainActivity.funciones.mensajeError(context, "Atencion", "ARTÍCULO NO LOCALIZADO!!")

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


            */


    }


    private fun consulta_motivo_transferencia() {




        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("TIP_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante)
            .add("SER_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.serComprobante)
            .add("NRO_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.nroComprobante)
            .build()


        var result = HttpRequest.call("", "entrada/consulta_motivo_transferencia", formBody)




        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_motivo_transferencia) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val motivoTransferenciaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until motivoTransferenciaArray.length()) {
                val motivoTransferenciaObject : JSONObject = motivoTransferenciaArray.get(i) as JSONObject
                codDepositoRem = motivoTransferenciaObject.get("COD_DEPOSITO").toString()
                codMotivoRem = motivoTransferenciaObject.get("COD_MOTIVO").toString()

            }


        }


        obtieneAnomalia()
        obtieneDeposito()





/*


        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("TIP_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante)
            .add("SER_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.serComprobante)
            .add("NRO_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.nroComprobante)
            .build()

        executorRunner.execute(
            CallableWS("entrada/consulta_motivo_transferencia", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task


                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_motivo_transferencia) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("rows")) {
                        val motivoTransferenciaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


                        for (i in 0 until motivoTransferenciaArray.length()) {
                            val motivoTransferenciaObject : JSONObject = motivoTransferenciaArray.get(i) as JSONObject
                            codDepositoRem = motivoTransferenciaObject.get("COD_DEPOSITO").toString()
                            codMotivoRem = motivoTransferenciaObject.get("COD_MOTIVO").toString()

                        }


                    }


                    obtieneAnomalia()
                    obtieneDeposito()



                }

                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })


 */

    }

    private fun consulta_unidad_medida_articulo(articulo : DetalleArticuloEntrada) {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", articulo.codArticulo)
            .build()

        executorRunner.execute(
            CallableWS("entrada/obtiene_unidad_medida_articulo", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    posicionUM = 0
                    unidadMedidaArticuloEntrada = ArrayList()
                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_unidad_medida_articulo) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("rows")) {
                        val umArticuloEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

                        for (i in 0 until umArticuloEntradaArray.length()) {
                            val umArticuloEntradaObject : JSONObject = umArticuloEntradaArray.get(i) as JSONObject
                            val um = UnidadMedidaArticuloEntrada()
                            um.codUnidadRel = umArticuloEntradaObject.get("COD_UNIDAD_REL").toString()
                            um.referencia = umArticuloEntradaObject.get("REFERENCIA").toString()
                            um.indBasico = umArticuloEntradaObject.get("IND_BASICO").toString()
                            um.lastro = umArticuloEntradaObject.get("LASTRO").toString()
                            um.capas = umArticuloEntradaObject.get("CAPAS").toString()

                            unidadMedidaArticuloEntrada.add(um)


                            if (BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante == "REP") {

                                if (um.codUnidadRel == "01") {
                                    posicionUM = i
                                }

                            } else {

                                if (um.codUnidadRel == articulo.unidad) {
                                    posicionUM = i

                                }

                            }


                        }


                    }


                    val spinnerAdapter : ArrayAdapter<UnidadMedidaArticuloEntrada> =
                        ArrayAdapter(context, R.layout.spinner_adapter, unidadMedidaArticuloEntrada)
                    spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    spDirecciones.adapter = spinnerAdapter

                    spDirecciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                            if(unidadMedidaArticuloEntrada[position].lastro != ""){
                                tvNorma.text = "NORMA  LASTRO:${unidadMedidaArticuloEntrada[position].lastro} X ALTO:${unidadMedidaArticuloEntrada[position].capas}"
                            }else{
                                tvNorma.text = " - "
                            }

                            posicionUM = position
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) { }
                    }

                    if (unidadMedidaArticuloEntrada.size > 0) {
                        spDirecciones.setSelection(posicionUM)
                    }


                    var c = 0
                    if(etDestino.text.toString().indexOf(" ") > -1) {

                        c = 1
                        etDestino.setText(etDestino.text.toString().replace(" ", ""))
                        etDestino.setSelection(etDestino.text.toString().length)

                    }


                    if(articulo.codArticulo.trim().isNotEmpty()){
                        if(BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante == "REP"){
                            buscaAnomaliasArticulos(articulo)
                        }
                    }


                    if (c == 1) {
                        if (articulo.codArticulo == "") {
                            Toast.makeText(context, "No se encontro ningun articulo con el codigo ingresado", Toast.LENGTH_LONG).show()
                        } else {
                            etCantidad.requestFocus()
                            etLote.isEnabled = articulo.indManejaVto == "S"
                        }

                    } else {
                        etLote.setText("")
                        etVencimiento.setText("")
                        etFabricacion.setText("")

                        if (articulo.indManejaVto == "S") {
                            etVencimiento.isEnabled = true
                            //etLote.isEnabled = true
                        } else {
                            etVencimiento.isEnabled = false
                            etVencimiento.setText(MainActivity.fec_vencimiento_defecto)
                            //etLote.isEnabled = false
                            //etLote.setText(MainActivity.lote_defecto)
                        }


                        if (articulo.indManejaLotes == "S") {
                            etLote.isEnabled = true
                        } else {
                            etLote.isEnabled = false
                            etLote.setText(MainActivity.lote_defecto)
                        }


                        if (articulo.indManejaFabricacion == "S" && tvFabricacion.visibility == View.VISIBLE) {
                            etFabricacion.isEnabled = true
                        } else {
                            etFabricacion.isEnabled = false
                            etFabricacion.setText("")
                        }




                    }


                    if (articulo.codArticulo.trim() == "") {

                        MainActivity.funciones.mensajeError(context, "Atencion", "ARTÍCULO NO LOCALIZADO!!")


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

    private fun buscaAnomaliasArticulos(articulo: DetalleArticuloEntrada) {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", articulo.codArticulo)
            .build()

        executorRunner.execute(
            CallableWS("entrada/busca_anomalia_articulo", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    posicionAnomaliaArticulo = 0
                    anomaliaArticuloEntrada = ArrayList()
                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (busca_anomalia_articulo) Error ${e.message.toString()} !")

                        return
                    }


                    if (respuestaJson.has("rows")) {
                        val anomaliaArticuloEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

                        for (i in 0 until anomaliaArticuloEntradaArray.length()) {
                            val anomaliaArticuloEntradaObject : JSONObject = anomaliaArticuloEntradaArray.get(i) as JSONObject
                            val aa = AnomaliaArticuloEntrada()
                            aa.codMotivo = anomaliaArticuloEntradaObject.get("COD_MOTIVO").toString()
                            aa.descMotivo = anomaliaArticuloEntradaObject.get("DESCRIPCION").toString()
                            aa.tipo = anomaliaArticuloEntradaObject.get("TIPO").toString()

                            anomaliaArticuloEntrada.add(aa)

                        }

                    }

                    try{
                        dialog_anomalias_articulos.dismiss()
                    }catch(e: Exception){
                    }

                    dialog_anomalias_articulos = Dialog(context)
                    dialog_anomalias_articulos.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog_anomalias_articulos.setContentView(R.layout.lista_anomalias_articulos)


                    dialog_anomalias_articulos.lvAnomaliasArticulo.adapter =
                        Adapter.AdapterGenericoCabecera(context, anomaliaArticuloEntrada,
                            R.layout.list_text_anomalia_articulos,
                            intArrayOf(R.id.tvDescripcion  , R.id.tvTipo),
                            arrayOf("DESCRIPCION"  , "TIPO") )


                    dialog_anomalias_articulos.lvAnomaliasArticulo.setOnItemClickListener { _, _, position, _ ->
                        FuncionesUtiles.posicionCabecera = position
                        dialog_anomalias_articulos.lvAnomaliasArticulo.invalidateViews()
                    }

                    dialog_anomalias_articulos.btnAcetarAnomalias.setOnClickListener{
                        dialog_anomalias_articulos.dismiss()
                    }


                    dialog_anomalias_articulos.show()



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

    private fun confirmar_conferencia() {

        if (detalleArticuloEntrada.size == 0) {

            MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ningun articulo con el codigo ingresado")
            tvDescArticulo.text = ""
            return
        }

        if (unidadMedidaArticuloEntrada.size == 0) {

            MainActivity.funciones.mensajeError(context, "Atencion", "No existe ninguna unidad de medida para este articulo")
            return
        }



        val dal = detalleArticuloEntrada[detalleArticuloEntrada.size - 1]


        var cantidadConferencia = 0.0
        try {
            cantidadConferencia = etCantidad.text.toString().toDouble()
        } catch (e: java.lang.Exception) {
        }

        if (cantidadConferencia <= 0) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Debe ingresar una cantidad mayor que 0")
            return
        }

        if(posicionAnomalia == -1){
            MainActivity.funciones.mensajeError(context, "Atencion", "Debe seleccionar una anomalia")
            return
        }


        var fec: String = etVencimiento.text.toString()
        if (fec.length == 6) {
            try {
                val dfDate = SimpleDateFormat("dd/MM/yyyy")
                dfDate.isLenient = false
                fec = fec.substring(0, 2) + "/" + fec.substring(2, 4) + "/20" + fec.substring(4, 6)
                dfDate.parse(fec)
                etVencimiento.setText(fec)
            } catch (e: java.lang.Exception) {
                val err = e.message //
                MainActivity.funciones.mensajeError(context, "Atencion", "FECHA INGRESADA INCORRECTA")
                etVencimiento.requestFocus()
                return
            }
        } else {
            if (dal.indManejaVto == "S") {
                MainActivity.funciones.mensajeError(context, "Atencion", "FECHA INGRESADA INCORRECTA")
                etVencimiento.requestFocus()
                return
            }
        }


        var fecFab: String = etFabricacion.text.toString()
        if (fecFab.length == 6) {
            try {
                val dfDate = SimpleDateFormat("dd/MM/yyyy")
                dfDate.isLenient = false
                fecFab = fecFab.substring(0, 2) + "/" + fecFab.substring(2, 4) + "/20" + fecFab.substring(4, 6)
                dfDate.parse(fecFab)
                etFabricacion.setText(fecFab)
            } catch (e: java.lang.Exception) {
                val err = e.message //
                MainActivity.funciones.mensajeError(context, "Atencion", "FECHA INGRESADA INCORRECTA")
                etFabricacion.requestFocus()
                return
            }
        } else {
            if (dal.indManejaFabricacion == "S"  && tvFabricacion.visibility == View.VISIBLE) {
                MainActivity.funciones.mensajeError(context, "Atencion", "FECHA INGRESADA INCORRECTA")
                etFabricacion.requestFocus()
                return
            }
        }

        if (posicionAnomalia > anomaliaEntrada.size) {

            MainActivity.funciones.mensajeError(context, "Atencion", "DEBE SELECCIONAR UNA ANOMALIA")
            return
        }


        if (BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipoCarga == "C") {
            proceso_inserta_conferencia(
                cantidadConferencia,
                fec,
                anomaliaEntrada[posicionAnomalia].codMotivo,
                null,
                dal,
                fecFab
            )
        } else {
            validaPreetiqueta(cantidadConferencia, fec, dal, fecFab)

        }
        etDestino.requestFocus()

    }


    private fun validaPreetiqueta(cantidad : Double,
                                  fecVenc  : String,
                                  dal      : DetalleArticuloEntrada,
                                  fecFab   : String) {




        try {
            dialog_valida_preetiqueta.dismiss()
        } catch (e: Exception) {
        }
        dialog_valida_preetiqueta = Dialog(context)
        dialog_valida_preetiqueta.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog_valida_preetiqueta.setContentView(R.layout.lector_codigo_trasnferencia)



        dialog_valida_preetiqueta.etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                try{
                    if (s.toString().indexOf("\n") > -1) {

                        dialog_valida_preetiqueta.etCodigoBarra.setText(dialog_valida_preetiqueta.etCodigoBarra.text.toString().replace("\n", ""))
                        dialog_valida_preetiqueta.etCodigoBarra.setSelection(dialog_valida_preetiqueta.etCodigoBarra.text.toString().length)

                        validaPreetiqueta(dialog_valida_preetiqueta.etCodigoBarra.text.toString(), cantidad, fecVenc, dal, fecFab)


                    }

                }catch(e: Exception){

                }


            }
        })



        dialog_valida_preetiqueta.btnAceptar.text = "ATRAS"
        dialog_valida_preetiqueta.btnAceptar.setOnClickListener {
            dialog_valida_preetiqueta.dismiss()
        }

        dialog_valida_preetiqueta.btn_volver.setOnClickListener {
            dialog_valida_preetiqueta.dismiss()
        }

        dialog_valida_preetiqueta.setCancelable(false)
        dialog_valida_preetiqueta.show()




    }

    private fun validaPreetiqueta(direccion: String,
                                  cantidad : Double,
                                  fecVenc  : String,
                                  dal      : DetalleArticuloEntrada,
                                  fecFab   : String) {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", direccion)
            .build()

        executorRunner.execute(
            CallableWS("entrada/valida_direccion_apm", formBody),
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

                            proceso_inserta_conferencia(
                                cantidad,
                                fecVenc,
                                anomaliaEntrada[posicionAnomalia].codMotivo,
                                null,
                                dal,
                                fecFab
                            )

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


    private fun proceso_inserta_conferencia(cantidad       : Double,
                                            fecVenc        : String,
                                            codAnomalia    : String,
                                            cod_preetiqueta: String?,
                                            articulo       : DetalleArticuloEntrada,
                                            fecFab         : String) {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", BuscarPlanillaEntrada.planillaEntradaSeleccionada.codDeposito)
            .add("TIP_PLANILLA", BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante)
            .add("SER_PLANILLA", BuscarPlanillaEntrada.planillaEntradaSeleccionada.serComprobante)
            .add("NRO_PLANILLA", BuscarPlanillaEntrada.planillaEntradaSeleccionada.nroComprobante)
            .add("COD_ARTICULO", articulo.codArticulo)
            .add("COD_UNIDAD_MEDIDA", unidadMedidaArticuloEntrada[posicionUM].codUnidadRel)
            .add("CANTIDAD", cantidad.toString().replace(".",","))
            .add("NRO_ORDEN", "")
            .add("NRO_LOTE", etLote.text.toString())
            //.add("NRO_LOTE", "0")
            .add("FEC_VENCIMIENTO", fecVenc)
            //.add("IND_MANEJA_LOTE", "S")
            .add("IND_MANEJA_LOTE", articulo.indManejaLotes)
            .add("COD_CONFERIDOR", MainActivity.usuarioLogin.codEmpleado)
            .add("COD_ANOMALIA", codAnomalia)
            .add("COD_PREETIQUETA", cod_preetiqueta ?: "")
            .add("COD_DEPOSITO_ENT", depositoEntrada[posicionDeposito].codDeposito)
            .add("AFECTA_STOCK", anomaliaEntrada[posicionAnomalia].afectaStock)
            .add("TIPO_ALM", tipoAlmacenamientoEntrada[posicionTipoAlmacenamiento].codTipoAlmacenamiento)
            .add("FEC_FABRICACION", fecFab)
            .build()

        executorRunner.execute(
            CallableWS("entrada/inserta_recepcion_mercaderia3", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task


                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (inserta_recepcion_mercaderia) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("respuesta")) {
                        val respuesta = respuestaJson.get("respuesta").toString()

                        if (respuesta.indexOf("EXITO") > -1) {
                            obtieneArticulosConferidosPlanilla()

                        }



                        cancelar_conferencia()


                        obtieneDeposito()
                        obtieneAnomalia()



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

    private fun cancelar_conferencia() {

        //opCancelar = 1;
        posicionAnomalia = 0
        posicionDeposito = 0

        spDeposito.setSelection(posicionDeposito)
        spAnomalias.setSelection(posicionAnomalia)
        spDirecciones.adapter = null

        tvDescArticulo.text = ""
        tvNorma.text = ""
        tvDescripcionTitulo.text = "Descripción: "

        etDestino.setText("")
        etCantidad.setText("")
        etVencimiento.setText("")
        etFabricacion.setText("")
        etLote.setText("")
        etDestino.requestFocus()


        articuloConferidoPlanillaEntrada = ArrayList<ArticuloConferidoPlanillaEntrada>()
        anomaliaEntrada = ArrayList<AnomaliaEntrada>()
        posicionAnomalia = 0
        depositoEntrada = ArrayList<DepositoEntrada>()
        posicionDeposito = 0
        detalleArticuloEntrada = ArrayList<DetalleArticuloEntrada>()
        codDepositoRem = ""
        codMotivoRem = ""
        unidadMedidaArticuloEntrada = ArrayList<UnidadMedidaArticuloEntrada>()
        posicionUM = 0
        anomaliaArticuloEntrada = ArrayList<AnomaliaArticuloEntrada>()
        posicionAnomaliaArticulo = 0


        obtieneAnomalia()
        obtieneDeposito()


    }

    private fun obtieneTipoAlmacenamiento() {

        tipoAlmacenamientoEntrada = ArrayList()

        var alm = TipoAlmacenamientoEntrada()
        alm.codTipoAlmacenamiento = "A"
        alm.descTipoAlmacenamiento = "Area General"

        tipoAlmacenamientoEntrada.add(alm)

        var alm2 = TipoAlmacenamientoEntrada()
        alm2.codTipoAlmacenamiento = "D"
        alm2.descTipoAlmacenamiento = "Direccion Paletizada"

        tipoAlmacenamientoEntrada.add(alm2)




        val spinnerAdapter : ArrayAdapter<TipoAlmacenamientoEntrada> =
            ArrayAdapter(context, R.layout.spinner_adapter, tipoAlmacenamientoEntrada)
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spTipoAlmacenamiento.adapter = spinnerAdapter

        spTipoAlmacenamiento.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                posicionTipoAlmacenamiento = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        if (tipoAlmacenamientoEntrada.size > 0) {
            spTipoAlmacenamiento.setSelection(posicionTipoAlmacenamiento)
        }



    }

    private fun obtieneAnomalia() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .build()





        val an = AnomaliaEntrada()
        an.codEmpresa = MainActivity.usuarioLogin.codEmpresa
        an.codMotivo = ""
        an.descMotivo = "NINGUNA"
        an.afectaStock = ""
        anomaliaEntrada.add(an)


        var result = HttpRequest.call("", "entrada/obtiene_anomalia", formBody)

        var respuestaJson: JSONObject


        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_anomalia) Error ${e.message.toString()} !")
            finish()
            return
        }

        if (respuestaJson.has("rows")) {
            val anomaliaEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            posicionAnomalia = 0
            anomaliaEntrada = ArrayList()



            for (i in 0 until anomaliaEntradaArray.length()) {
                val anomaliaEntradaObject : JSONObject = anomaliaEntradaArray.get(i) as JSONObject
                val an = AnomaliaEntrada()
                an.codEmpresa = anomaliaEntradaObject.get("COD_EMPRESA").toString()
                an.codMotivo = anomaliaEntradaObject.get("COD_MOTIVO").toString()
                an.descMotivo = anomaliaEntradaObject.get("DESCRIPCION").toString()
                an.afectaStock = anomaliaEntradaObject.get("AFECTA_STOCK").toString()

                anomaliaEntrada.add(an)

                if (BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante == "REM" && codMotivoRem != "") {

                    if (an.codMotivo == codMotivoRem) {
                        posicionAnomalia = i
                    }

                } else {

                    if (BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante == "ENT" || (codDepositoRem == "01")) {

                        if (an.codMotivo == "07") {
                            posicionAnomalia = i
                        }

                    }

                }

            }
        }


        val spinnerAdapter : ArrayAdapter<AnomaliaEntrada> =
            ArrayAdapter(context, R.layout.spinner_adapter, anomaliaEntrada)
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spAnomalias.adapter = spinnerAdapter

        spAnomalias.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                posicionAnomalia = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        if (anomaliaEntrada.size > 0) {
            spAnomalias.setSelection(posicionAnomalia)

        }




    }


    private fun obtieneDeposito() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()


        var result = HttpRequest.call("", "entrada/obtiene_deposito", formBody)


        posicionDeposito = 0

        depositoEntrada = ArrayList()
        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_deposito) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val depositoEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until depositoEntradaArray.length()) {
                val depositoEntradaObject : JSONObject = depositoEntradaArray.get(i) as JSONObject
                val de = DepositoEntrada()
                de.codDeposito = depositoEntradaObject.get("COD_DEPOSITO").toString()
                de.descDeposito = depositoEntradaObject.get("DESCRIPCION").toString()
                de.indDefecto = depositoEntradaObject.get("IND_DEFECTO").toString()

                depositoEntrada.add(de)

                if (BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante == "REM" && codDepositoRem != "") {

                    if (de.codDeposito == codDepositoRem) {
                        posicionDeposito = i
                    }

                } else {

                    if (de.indDefecto == "S") {
                        posicionDeposito = i
                    }

                }

            }
        }


        val spinnerAdapter : ArrayAdapter<DepositoEntrada> =
            ArrayAdapter(context, R.layout.spinner_adapter, depositoEntrada)
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spDeposito.adapter = spinnerAdapter

        spDeposito.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                posicionDeposito = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        if (depositoEntrada.size > 0) {
            spDeposito.setSelection(posicionDeposito)
        }





    }


    private fun buscar_reconferencias() {



        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante)
            .add("SER_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.serComprobante)
            .add("NRO_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.nroComprobante)
            .build()

        executorRunner.execute(
            CallableWS("entrada/buscar_reconferencia_entrada", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    reconferenciaEntrada = ArrayList()
                    var respuestaJson: JSONObject

                    try {
                        respuestaJson = JSONObject(result)
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscar_reconferencia_entrada) Error ${e.message.toString()} !")

                        return
                    }

                    if (respuestaJson.has("rows")) {
                        val recoferenciaEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
                        for (i in 0 until recoferenciaEntradaArray.length()) {
                            val reconferenciaEntradaObject : JSONObject = recoferenciaEntradaArray.get(i) as JSONObject
                            val re = ReconferenciaEntrada()
                            re.codArticulo = reconferenciaEntradaObject.get("COD_ARTICULO").toString()
                            re.descArticulo = reconferenciaEntradaObject.get("DESC_ARTICULO").toString()
                            re.codUnidadMedida = reconferenciaEntradaObject.get("COD_UNIDAD_MEDIDA").toString()
                            re.descUnidadMedida = reconferenciaEntradaObject.get("DESC_UNIDAD_MEDIDA").toString()
                            re.cantidad = reconferenciaEntradaObject.get("CANTIDAD").toString()
                            re.nroLote = reconferenciaEntradaObject.get("NRO_LOTE").toString()
                            re.fecVencimiento = reconferenciaEntradaObject.get("FEC_VENCIMIENTO").toString()
                            re.anomalia = reconferenciaEntradaObject.get("ANOMALIA").toString()
                            re.orden = reconferenciaEntradaObject.get("ORDEN").toString()
                            reconferenciaEntrada.add(re)
                        }
                    }


                    try {
                        dialog_buscar_reconferencias.dismiss()
                    } catch (e: Exception) {
                    }
                    dialog_buscar_reconferencias = Dialog(context)
                    dialog_buscar_reconferencias.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog_buscar_reconferencias.setContentView(R.layout.lista_reconferencias)


                    dialog_buscar_reconferencias.lvReconferencias.adapter =
                        Adapter.AdapterGenericoCabecera(context, reconferenciaEntrada,
                            R.layout.list_text_reconferencia,
                            intArrayOf(R.id.tv1, R.id.tv2, R.id.tv3),
                            arrayOf("COD_ARTICULO" , "DESC_ARTICULO"  , "DESC_UNIDAD_MEDIDA") )


                    dialog_buscar_reconferencias.lvReconferencias.setOnItemClickListener { _, _, position, _ ->
                        FuncionesUtiles.posicionCabecera = position
                        dialog_buscar_reconferencias.lvReconferencias.invalidateViews()
                    }




                    dialog_buscar_reconferencias.btnCerrarReconferencias.setOnClickListener {

                        dialog_buscar_reconferencias.dismiss()

                    }

                    dialog_buscar_reconferencias.setCanceledOnTouchOutside(false)
                    dialog_buscar_reconferencias.show()


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


}