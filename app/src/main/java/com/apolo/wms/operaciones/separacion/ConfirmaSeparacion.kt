package com.apolo.wms.operaciones.separacion

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.separacion.ArticuloConferidoPlanillaSeparacion
import com.apolo.wms.clases.separacion.DetalleArticuloSeparacion
import com.apolo.wms.clases.separacion.UnidadMedidaArticuloSeparacion
import com.apolo.wms.operaciones.separacion.adapter.AdapterSeparacionConferido
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.entrada_conferencia.*
import kotlinx.android.synthetic.main.entrada_redireccion_ub.*
import kotlinx.android.synthetic.main.separacion_conferencia.*
import kotlinx.android.synthetic.main.separacion_conferencia.btnCancelar
import kotlinx.android.synthetic.main.separacion_conferencia.btnConfirmar
import kotlinx.android.synthetic.main.separacion_conferencia.etCantidad
import kotlinx.android.synthetic.main.separacion_conferencia.etDestino
import kotlinx.android.synthetic.main.separacion_conferencia.etDesvioFocus
import kotlinx.android.synthetic.main.separacion_conferencia.etVencimiento
import kotlinx.android.synthetic.main.separacion_conferencia.spDirecciones
import kotlinx.android.synthetic.main.separacion_conferencia.tvDescArticulo
import kotlinx.android.synthetic.main.separacion_conferencia.tvDescripcionTitulo
import kotlinx.android.synthetic.main.separacion_conferido.*
import kotlinx.android.synthetic.main.separacion_confirmar.*
import kotlinx.android.synthetic.main.separacion_confirmar.lTabConferencia
import kotlinx.android.synthetic.main.separacion_confirmar.lTabConferidos
import kotlinx.android.synthetic.main.separacion_confirmar.tabConferencia
import kotlinx.android.synthetic.main.separacion_confirmar.tabConferido
import kotlinx.android.synthetic.main.separacion_confirmar.tvNroInventario
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ConfirmaSeparacion : AppCompatActivity() {

    companion object {
        lateinit var context : Context

        lateinit var rvConferidos : RecyclerView


        var articuloConferidoPlanillaSeparacion = ArrayList<ArticuloConferidoPlanillaSeparacion>()
        var posicionArticuloConferido = 0

        var unidadMedidaArticuloSeparacion = ArrayList<UnidadMedidaArticuloSeparacion>()
        var posicionUM = 0

        var detalleArticuloSeparacion = ArrayList<DetalleArticuloSeparacion>()

        fun obtieneArticulosConferidosPlanilla() {

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
                .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
                .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
                .add("PALET_NRO", BuscaGrupoSeparacion.grupoSeparacionSeleccionada.paletNro)
                .build()


            var result = HttpRequest.call("", "separacion/obtiene_articulos_conferidos_planilla", formBody)


            posicionArticuloConferido = 0
            articuloConferidoPlanillaSeparacion = java.util.ArrayList()

            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtieneArticulosConferidosPlanilla) Error ${e.message.toString()} !")
                return
            }

            if (respuestaJson.has("rows")) {
                val articuloConferidoSeparacionArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


                for (i in 0 until articuloConferidoSeparacionArray.length()) {
                    val articuloConferidoSeparacionObject : JSONObject = articuloConferidoSeparacionArray.get(i) as JSONObject
                    val conf = ArticuloConferidoPlanillaSeparacion()
                    conf.paletNro = articuloConferidoSeparacionObject.get("PALET_NRO").toString()
                    conf.articulo = articuloConferidoSeparacionObject.get("ARTICULO").toString()
                    conf.referencia = articuloConferidoSeparacionObject.get("REFERENCIA").toString()
                    conf.cantidad = articuloConferidoSeparacionObject.get("CANTIDAD").toString()
                    conf.nroOrden = articuloConferidoSeparacionObject.get("NRO_ORDEN").toString()
                    articuloConferidoPlanillaSeparacion.add(conf)
                }


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
                val adapter = AdapterSeparacionConferido(
                    context,
                    articuloConferidoPlanillaSeparacion, R.layout.card_view_separacion_conferido  )

                // Setting the Adapter with the recyclerview
                rvConferidos.adapter = adapter




            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaUnidadMedida)")


            }

        }


        fun eliminarConferido(item: ArticuloConferidoPlanillaSeparacion) {

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
                .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
                .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
                .add("PALET_NRO", BuscaGrupoSeparacion.grupoSeparacionSeleccionada.paletNro)
                .add("NRO_ORDEN", item.nroOrden)
                .build()

            var result = HttpRequest.call("", "separacion/elimina_conf_sep_caja", formBody)


            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (elimina_conf_sep_caja) Error ${e.message.toString()} !")
                return
            }

            if (respuestaJson.has("respuesta")) {

                val respuesta = respuestaJson.get("respuesta").toString()


                SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Atencion")
                    .setContentText("CONFERENCIA ANULADA!")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        sDialog.dismissWithAnimation()

                    }
                    .show()


                if (respuesta.indexOf("EXITO") > -1) {
                    obtieneArticulosConferidosPlanilla()
                }



            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (insertaReabastDet)")

            }

        }


    }


    var nro_orden_actual = ""
    var tipo_conferencia = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.separacion_confirmar)

        inicializar()

    }

    fun inicializar() {

        Companion.rvConferidos = rvConferidos

        mostrarContenido(tabConferencia)
        context = this

        title = "CONFERENCIA SEPARACION".uppercase(Locale.getDefault()).toString()

        tvNroInventario.text = "Nro.Sep.:${BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla}"


        //CONFERENCIA
        etDestino.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {


                if(tipo_conferencia.length == 0){
                    tipo_conferencia =  s.toString()
                }
                if (s.toString().indexOf("\n") > -1) {

                    etDestino.setText(etDestino.text.toString().replace("\n", ""))
                    tipo_conferencia =  s.toString()
                    etDesvioFocus.requestFocus()

                }
 
            }
        })
        etDestino.setOnFocusChangeListener { view, _ ->

            if (layoutSeparacionConferencia.visibility == View.VISIBLE) {

                if (!view.hasFocus()) {
                    buscaDetalleArticulo()

                }

            }

        }


        btnConfirmar.setOnClickListener{ confirmarSeparacion() }
        btnCancelar.setOnClickListener{ cancelarSeparacion() }

        btnCerrarConferencia.setOnClickListener { cerrarConferencia() }

        FuncionesUtiles.limitarDecimales(MainActivity.maximoDecimales, etCantidad)

    }


    fun cerrarConferencia() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
            .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
            .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
            .add("PALET_NRO", BuscaGrupoSeparacion.grupoSeparacionSeleccionada.paletNro)
            .build()

        var result = HttpRequest.call("", "separacion/cierre_conferencia_caja", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cierre_conferencia_caja) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Atencion")
                .setContentText(respuesta)
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->

                    finish()
                    sDialog.dismissWithAnimation()

                }
                .show()


        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cerrarConferencia)")
        }


    }


    fun buscaDetalleArticulo() {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_BARRA", etDestino.text.toString())
            .build()


        var result = HttpRequest.call("", "separacion/busca_detalle_articulo", formBody)

        tvDescArticulo.text = ""
        etCantidad.inputType = InputType.TYPE_CLASS_NUMBER

        detalleArticuloSeparacion = ArrayList()


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (busca_detalle_articulo) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {
            val detalleArticuloSeparacionArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

            var direccion = ""
            for (i in 0 until detalleArticuloSeparacionArray.length()) {
                val detalleArticuloSeparacionObject : JSONObject = detalleArticuloSeparacionArray.get(i) as JSONObject
                val ds = DetalleArticuloSeparacion()
                ds.codArticulo = detalleArticuloSeparacionObject.get("COD_ARTICULO").toString()
                ds.descArticulo = detalleArticuloSeparacionObject.get("DESCRIPCION").toString()
                ds.indManejaVto = detalleArticuloSeparacionObject.get("IND_MANEJA_VTO").toString()
                ds.codDireccion = detalleArticuloSeparacionObject.get("COD_DIRECCION").toString()
                ds.unidad = detalleArticuloSeparacionObject.get("UNIDAD").toString()
                ds.codDeposito = detalleArticuloSeparacionObject.get("COD_DEPOSITO").toString()
                ds.esPesable = detalleArticuloSeparacionObject.get("ES_PESABLE").toString()
                detalleArticuloSeparacion.add(ds)


                if (ds.codDireccion.isNotEmpty()) {
                    direccion = ds.codDireccion.substring(0, 3)+"-"+ds.codDireccion.substring(3, 6)+"-"+ds.codDireccion.substring(6, 7)+"-"+ds.codDireccion.subSequence(7, 9)
                }

                tvDescripcionTitulo.text = "Descripcion: ${ds.descArticulo} (COD:${ds.codArticulo}) DIR: $direccion"

            }



            if (detalleArticuloSeparacionArray.length() > 0) {


                var ar = detalleArticuloSeparacion[detalleArticuloSeparacion.size - 1]

                if (ar.esPesable == "S") {
                    etCantidad.inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                }

                unidadMedidaArticuloSeparacion = ArrayList()
                posicionUM = 0

                var formBody: RequestBody = FormBody.Builder()
                    .add("USER", MainActivity.usuarioLogin.codUsuario)
                    .add("PASS", MainActivity.usuarioLogin.password)
                    .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                    .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                    .add("COD_ARTICULO", ar.codArticulo)
                    .build()


                var result = HttpRequest.call("", "separacion/obtiene_unidad_medida_articulo", formBody)

                var respuestaJson: JSONObject

                try {
                    respuestaJson = JSONObject(result)
                } catch (e: Exception) {
                    MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_unidad_medida_articulo) Error ${e.message.toString()} !")
                    return
                }

                if (respuestaJson.has("rows")) {
                    val umArticuloSeparacionArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

                    var direccion = ""
                    for (i in 0 until umArticuloSeparacionArray.length()) {
                        val umArticuloSeparacionObject : JSONObject = umArticuloSeparacionArray.get(i) as JSONObject
                        val um = UnidadMedidaArticuloSeparacion()
                        um.codUnidadRel = umArticuloSeparacionObject.get("COD_UNIDAD_REL").toString()
                        um.referencia = umArticuloSeparacionObject.get("REFERENCIA").toString()
                        um.indBasico = umArticuloSeparacionObject.get("IND_BASICO").toString()
                        um.lastro = umArticuloSeparacionObject.get("LASTRO").toString()
                        um.capas = umArticuloSeparacionObject.get("CAPAS").toString()
                        unidadMedidaArticuloSeparacion.add(um)

                        if (um.codUnidadRel == ar.unidad) {
                            posicionUM = i
                        }

                    }

                }


                val spinnerAdapter : ArrayAdapter<UnidadMedidaArticuloSeparacion> =
                    ArrayAdapter(context, R.layout.spinner_adapter, unidadMedidaArticuloSeparacion)
                spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                spDirecciones.adapter = spinnerAdapter

                spDirecciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        posicionUM = position
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) { }
                }

                if (unidadMedidaArticuloSeparacion.size > 0) {
                    spDirecciones.setSelection(posicionUM)
                }


                etVencimiento.setText("")
                etVencimiento.isEnabled = false
                etVencimiento.setText(MainActivity.fec_vencimiento_defecto)


            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ningun articulo con el codigo ingresado")


            }


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (buscaDetalleArticulo)")


        }




    }


    fun confirmarSeparacion() {

        var ar = detalleArticuloSeparacion[detalleArticuloSeparacion.size - 1]

        if(!validaGrupoSepCaj()){
            return
        }

        if (ar.codArticulo == "") {
            MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ningun articulo con el codigo ingresado")
            tvDescArticulo.text = ""
            return
        }

        if (unidadMedidaArticuloSeparacion.size == 0) {
            MainActivity.funciones.mensajeError(context, "Atencion", "No existe ninguna unidad de medida para este articulo")
            return
        }

        var cantidadConferencia = 0.0
        try {
            //cantidadConferencia = Integer.parseInt(etCantidad.text.toString())
            cantidadConferencia = etCantidad.text.toString().toDouble()
        } catch (e: Exception) {
        }

        if (cantidadConferencia <= 0) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Debe ingresar una cantidad mayor que 0")
            return
        }

        var fec = etVencimiento.text.toString()

        if(fec.length == 6){
            try {
                var dfDate = SimpleDateFormat("dd/MM/yyyy")
                dfDate.isLenient = false
                fec = fec.substring(0,2)+"/"+fec.substring(2,4)+"/20"+fec.substring(4, 6)
                dfDate.parse(fec)
                etVencimiento.setText(fec)
            } catch (e: Exception) {
                var err = e.message

                SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
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
            if(ar.indManejaVto == "S"){


                try {
                    var dfDate = SimpleDateFormat("dd/MM/yyyy")
                    dfDate.isLenient = false
                    dfDate.parse(fec)
                    etVencimiento.setText(fec)
                } catch (e: Exception) {
                    var err = e.message

                    SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
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
        }

        procesoInsertaConferencia(cantidadConferencia, fec)


    }

    fun procesoInsertaConferencia(cantidadConferencia: Double, fec: String) {

        var ar = detalleArticuloSeparacion[detalleArticuloSeparacion.size - 1]

        if(validaExisArticuloSepCaj(ar.codArticulo)){
            /*inserta_conf_sep_caja(s_cod_empresa    , s_cod_sucursal  , s_tip_comprobante, s_ser_comprobante  ,
                                  s_nro_comprobante, s_cod_conferidor, s_palet_nro      , cod_articulo_actual,
                                  unidades[ind_unidad_medida], Integer.toString(cantidadConferencia),
                                  fec, tipo_conferencia);*/

            insertaConfSepCaja(cantidadConferencia.toString(), fec)

            obtieneArticulosConferidosPlanilla()
            cancelarSeparacion()
        }




    }

    fun insertaConfSepCaja(cantidadConferencia: String, fec: String) {

        var ar = detalleArticuloSeparacion[detalleArticuloSeparacion.size - 1]

        var um = unidadMedidaArticuloSeparacion[posicionUM]

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
            .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
            .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
            //.add("COD_CONFERIDOR", BuscaGrupoSeparacion.grupoSeparacionSeleccionada.codSeparador)
            .add("COD_CONFERIDOR", MainActivity.usuarioLogin.codPersona)
            .add("PALET_NRO", BuscaGrupoSeparacion.grupoSeparacionSeleccionada.paletNro)
            .add("COD_ARTICULO", ar.codArticulo)
            .add("COD_UNIDAD_MEDIDA", um.codUnidadRel)
            .add("CANTIDAD", cantidadConferencia.replace(".", ","))
            .add("FEC_VENCIMIENTO", fec)
            .add("TIPO_CONFERENCIA", tipo_conferencia)
            .build()

        var result = HttpRequest.call("", "separacion/inserta_conf_sep_caja", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (inserta_conf_sep_caja) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Atencion")
                .setContentText(respuesta)
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->

                    sDialog.dismissWithAnimation()

                }
                .show()


        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaGrupoSepCaj)")
        }

    }

    fun validaExisArticuloSepCaj(codArticulo: String) : Boolean {



        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
            .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
            .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
            .add("PALET_NRO", BuscaGrupoSeparacion.grupoSeparacionSeleccionada.paletNro)
            .add("COD_ARTICULO", codArticulo)
            .build()

        var result = HttpRequest.call("", "separacion/valida_exis_articulo_sep_caj", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_exis_articulo_sep_caj) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            return if (respuesta == "S") {
                true
            } else {


                MainActivity.funciones.mensajeError(context, "Atencion", "Articulo no se encuentra para separar")
                false


            }


        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaExisArticuloSepCaj)")
            return false
        }

    }


    fun validaGrupoSepCaj() : Boolean {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
            .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
            .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
            .add("PALET_NRO", BuscaGrupoSeparacion.grupoSeparacionSeleccionada.paletNro)
            .build()

        var result = HttpRequest.call("", "separacion/valida_grupo_sep_caj", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_grupo_sep_caj) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            if (respuesta == "S") {
                return true
            } else {
                if (respuesta.trim() == "1") {
                    MainActivity.funciones.mensajeError(context, "Atencion", "Usuario incorrecto")
                    return false

                } else {

                    if (respuesta.trim() == "2") {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Seleccionar separador")
                        return false

                    } else {

                        if (respuesta.trim() == "3") {
                            MainActivity.funciones.mensajeError(context, "Atencion", "Usuario con Conferencia pendiente")
                            return false

                        } else {

                            MainActivity.funciones.mensajeError(context, "Atencion", "Error en busca. ${respuesta}")
                            return false

                        }

                    }


                }

            }


        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaGrupoSepCaj)")
            return false
        }


    }


    fun cancelarSeparacion() {

        nro_orden_actual = ""

        etDestino.setText("")
        tvDescArticulo.text = ""
        tvDescripcionTitulo.text = ""
        etCantidad.setText("")
        etVencimiento.setText("")
        tipo_conferencia = ""

        val dataAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item, ArrayList<String>()
        )
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        spDirecciones.adapter = dataAdapter

        etDestino.requestFocus()


    }



    fun mostrarContenido(view: View) {

        tabConferencia.setBackgroundColor(Color.parseColor("#474747"))
        tabConferido.setBackgroundColor(Color.parseColor("#474747"))
        lTabConferencia.setBackgroundColor(Color.parseColor("#474747"))
        lTabConferidos.setBackgroundColor(Color.parseColor("#474747"))
        layoutSeparacionConferencia.visibility = View.GONE
        layoutSeparacionConferido.visibility = View.GONE
        view.setBackgroundColor(Color.parseColor("#116600"))
        if (view.id == tabConferencia.id){
            layoutSeparacionConferencia.visibility = View.VISIBLE
            lTabConferencia.setBackgroundColor(Color.parseColor("#116600"))

            //etDestino.requestFocus()

        }
        if (view.id == tabConferido.id){

            layoutSeparacionConferido.visibility = View.VISIBLE
            lTabConferidos.setBackgroundColor(Color.parseColor("#116600"))

            obtieneArticulosConferidosPlanilla()

        }

    }
    
    




}