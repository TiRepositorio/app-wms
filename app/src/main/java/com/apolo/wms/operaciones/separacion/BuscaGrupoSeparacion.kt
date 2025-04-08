package com.apolo.wms.operaciones.separacion

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.Operaciones
import com.apolo.wms.clases.separacion.GrupoSeparacion
import com.apolo.wms.clases.separacion.SeparadorSeparacion
import com.apolo.wms.operaciones.reabastecimientocorrprev.ReabastecimientoCorrPrev
import com.apolo.wms.operaciones.separacion.adapter.AdapterGrupoSeparacion
import com.apolo.wms.operaciones.separacion.adapter.AdapterSeparadorSeparacion
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.buscador_grupo_separacion.btnSeleccionar
import kotlinx.android.synthetic.main.buscador_grupo_separacion.ibtnBuscarPlanilla
import kotlinx.android.synthetic.main.buscador_grupo_separacion.rvPlanillaInv
import kotlinx.android.synthetic.main.lista_separadores.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class BuscaGrupoSeparacion : AppCompatActivity() {


    companion object {
        lateinit var context : Context

        var grupoSeparacion = ArrayList<GrupoSeparacion>()
        var grupoSeparacionSeleccionada = GrupoSeparacion()
        var posicionGrupo = -1


        var separadorSeparacion = ArrayList<SeparadorSeparacion>()
        var posicionSeparador = -1
    }


    private lateinit var dialog_asigna_separador: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buscador_grupo_separacion)

        inicializar()

    }

    fun inicializar() {

        context = this

        title = "SELECCIONAR GRUPO".uppercase(Locale.getDefault())

        btnSeleccionar.setOnClickListener{ seleccionarPlanilla() }
        ibtnBuscarPlanilla.setOnClickListener { buscar() }

        buscarPlanilla("")

    }


    fun buscarPlanilla(filtro: String) {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
            .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
            .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
            .build()

        var result = HttpRequest.call("", "separacion/buscar_grupo", formBody)

        posicionGrupo = -1
        grupoSeparacion = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscar_grupo) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {

            val grupoSeparacionArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until grupoSeparacionArray.length()) {
                val grupoSeparacionObject : JSONObject = grupoSeparacionArray.get(i) as JSONObject
                val gs = GrupoSeparacion()
                gs.codEmpresa = grupoSeparacionObject.get("COD_EMPRESA").toString()
                gs.tipPlanilla = grupoSeparacionObject.get("TIP_PLANILLA").toString()
                gs.serPlanilla = grupoSeparacionObject.get("SER_PLANILLA").toString()
                gs.nroPlanilla = grupoSeparacionObject.get("NRO_PLANILLA").toString()
                gs.paletNro = grupoSeparacionObject.get("PALET_NRO").toString()
                gs.separador = grupoSeparacionObject.get("SEPARADOR").toString()
                gs.codSeparador = grupoSeparacionObject.get("COD_SEPARADOR").toString()
                gs.codUsuario = grupoSeparacionObject.get("COD_USUARIO").toString()
                gs.estado = grupoSeparacionObject.get("ESTADO").toString()

                grupoSeparacion.add(gs)

            }

            val gridLayoutManager = GridLayoutManager(context, 1)

            rvPlanillaInv.layoutManager = gridLayoutManager
            rvPlanillaInv.itemAnimator = DefaultItemAnimator()
            rvPlanillaInv.setHasFixedSize(true)


            // this creates a vertical layout Manager
            rvPlanillaInv.layoutManager = LinearLayoutManager(context)

            // This loop will create 20 Views containing
            // the image with the count of view
            // This will pass the ArrayList to our Adapter
            val adapter = AdapterGrupoSeparacion(
                context,
                grupoSeparacion,
                R.layout.card_view_grupo_separacion)

            // Setting the Adapter with the recyclerview
            rvPlanillaInv.adapter = adapter


        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (verificaReabastecimientoBase)")
        }


    }

    fun seleccionarPlanilla() {

        if (posicionGrupo !== -1) {

            if (grupoSeparacion[posicionGrupo].estado == "E") {

                return

            }

            if(validaGrupoSepCaj()){

                val i = Intent(context, ConfirmaSeparacion::class.java)
                grupoSeparacionSeleccionada = grupoSeparacion[posicionGrupo]
                startActivity(i)

            }

        } else{
            MainActivity.funciones.mensajeError(context, "Atencion", "Seleccionar Grupo")

        }


    }

    fun validaGrupoSepCaj() : Boolean {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
            .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
            .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
            .add("PALET_NRO", grupoSeparacion[posicionGrupo].paletNro)
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

    fun buscar() {


        if (posicionGrupo !== -1) {

            if(validaSeparador()){
                abreAsignaSeparador()
            }

        } else{
            MainActivity.funciones.mensajeError(context, "Atencion", "Seleccionar Grupo")

        }



    }

    fun validaSeparador() : Boolean {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
            .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
            .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
            .add("PALET_NRO", grupoSeparacion[posicionGrupo].paletNro)
            .build()

        var result = HttpRequest.call("", "separacion/valida_grupo_sep_caj", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (validaSeparador) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            return if(respuesta.trim() == "2"){
                true
            }else {
                MainActivity.funciones.mensajeError(context, "Atencion", "Separador no se puede seleccionar")
                false
            }


        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaSeparador)")
            return false
        }

    }



    fun abreAsignaSeparador() {

        posicionSeparador = -1

        try {
            dialog_asigna_separador.dismiss()
        } catch (e: Exception) {
        }


        dialog_asigna_separador = Dialog(context)
        dialog_asigna_separador.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog_asigna_separador.setContentView(R.layout.lista_separadores)


        dialog_asigna_separador.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                cargaSeparadores(dialog_asigna_separador.etBuscar.text.toString())

            }
        })


        dialog_asigna_separador.btnCancelar.setOnClickListener {
            dialog_asigna_separador.dismiss()
        }

        dialog_asigna_separador.btnConfirmarSeparador.setOnClickListener{

            if(posicionSeparador != -1){

                var formBody: RequestBody = FormBody.Builder()
                    .add("USER", MainActivity.usuarioLogin.codUsuario)
                    .add("PASS", MainActivity.usuarioLogin.password)
                    .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                    .add("TIP_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.tipPlanilla)
                    .add("SER_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.serPlanilla)
                    .add("NRO_COMPROBANTE", BuscarPlanillaSeparacion.planillaSeparacionSeleccionada.nroPlanilla)
                    .add("PALET_NRO", grupoSeparacion[posicionGrupo].paletNro)
                    .add("COD_SEPARADOR", separadorSeparacion[posicionSeparador].codSeparador)
                    .build()

                var result = HttpRequest.call("", "separacion/asigna_separador_grupo", formBody)

                var respuestaJson: JSONObject

                try {
                    respuestaJson = JSONObject(result)
                } catch (e: Exception) {
                    MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (asigna_separador_grupo) Error ${e.message.toString()} !")
                    return@setOnClickListener
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
                    MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (asigna_separador_grupo)")

                }


                buscarPlanilla("")
            }else{
                MainActivity.funciones.mensajeError(context, "Atencion", "Seleccionar Separador")
            }

            dialog_asigna_separador.dismiss()

        }



        dialog_asigna_separador.show()



    }


    fun cargaSeparadores(filtro: String) {

        /*

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("FILTRO", filtro.toString().trim())
            .build()

        var result = HttpRequest.call("", "separacion/busca_separador", formBody)

        posicionSeparador = -1
        separadorSeparacion = ArrayList()

        var respuestaJson = JSONObject(result)
        if (respuestaJson.has("rows")) {

            val separadorSeparacionArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until separadorSeparacionArray.length()) {
                val separadorSeparacionObject : JSONObject = separadorSeparacionArray.get(i) as JSONObject
                val ss = SeparadorSeparacion()
                ss.codSeparador = separadorSeparacionObject.get("COD_SEPARADOR").toString()
                ss.nombre = separadorSeparacionObject.get("NOMBRE").toString()


                separadorSeparacion.add(ss)

            }

            val gridLayoutManager = GridLayoutManager(context, 1)

            dialog_asigna_separador.rvSeparadores.layoutManager = gridLayoutManager
            dialog_asigna_separador.rvSeparadores.itemAnimator = DefaultItemAnimator()
            dialog_asigna_separador.rvSeparadores.setHasFixedSize(true)


            // this creates a vertical layout Manager
            dialog_asigna_separador.rvSeparadores.layoutManager = LinearLayoutManager(context)

            // This loop will create 20 Views containing
            // the image with the count of view
            // This will pass the ArrayList to our Adapter
            val adapter = AdapterSeparadorSeparacion(
                context,
                separadorSeparacion,
                R.layout.card_view_separadores)

            // Setting the Adapter with the recyclerview
            dialog_asigna_separador.rvSeparadores.adapter = adapter


        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaSeparadores)")
        }


         */

        if (Operaciones.separadorSeparacion.size == 0) {
            Operaciones.cargaSeparadores()
        }

        posicionSeparador = -1
        separadorSeparacion = ArrayList()

        Operaciones.separadorSeparacion.forEach {

            if (it.codSeparador.uppercase(Locale.getDefault())
                    .contains(filtro.uppercase(Locale.getDefault())) ||
                it.nombre.uppercase(Locale.getDefault()).
                    contains(filtro.uppercase(Locale.getDefault()))) {

                separadorSeparacion.add(it)

            }

        }


        val gridLayoutManager = GridLayoutManager(context, 1)

        dialog_asigna_separador.rvSeparadores.layoutManager = gridLayoutManager
        dialog_asigna_separador.rvSeparadores.itemAnimator = DefaultItemAnimator()
        dialog_asigna_separador.rvSeparadores.setHasFixedSize(true)


        // this creates a vertical layout Manager
        dialog_asigna_separador.rvSeparadores.layoutManager = LinearLayoutManager(context)

        // This loop will create 20 Views containing
        // the image with the count of view
        // This will pass the ArrayList to our Adapter
        val adapter = AdapterSeparadorSeparacion(
            context,
            separadorSeparacion,
            R.layout.card_view_separadores)

        // Setting the Adapter with the recyclerview
        dialog_asigna_separador.rvSeparadores.adapter = adapter


    }


    override fun onResume() {
        super.onResume()
        buscarPlanilla("")
    }

}