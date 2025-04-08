package com.apolo.wms.operaciones.cerrarsesion

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.cerrarsesion.ConsultaSesion
import com.apolo.wms.operaciones.cerrarsesion.adapter.AdapterSesion
import com.apolo.wms.operaciones.fraccionado.ConsultaJaula
import com.apolo.wms.operaciones.fraccionado.adapter.AdapterPlanillaFraccionado
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.activity_cerrar_sesion.*
import kotlinx.android.synthetic.main.consultar_jaula.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class CerrarSesion : AppCompatActivity() {

    companion object {

        lateinit var context : Context

        var usuarioSesion = ArrayList<ConsultaSesion>()
        var posicionUsuario = 0

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cerrar_sesion)

        inicializar()

    }


    fun inicializar() {

        context = this

        btnCerrarSesion.setOnClickListener { cerrarSesion(etUsuarios.text.toString()) }

        btnConsultar.setOnClickListener { buscar(etUsuarios.text.toString())  }

    }


    fun cerrarSesion(usuario: String) {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_USUARIO", usuario.toUpperCase())
            .build()

        var result = HttpRequest.call("", "cerrar_sesion/cerrar_sesion", formBody)

        MainActivity.funciones.mensajeExito(context, "Cerrado con éxito!", "Se cerró la sesion del usuario ${usuario.uppercase(
            Locale.getDefault()
        )}.")

        etUsuarios.setText("")
        buscar("")

    }


    fun buscar(usuario: String) {

        posicionUsuario = 0
        usuarioSesion = ArrayList()

        var metodo = ""

        if (etUsuarios.text.toString().uppercase(Locale.getDefault()) == "VERSION") {

            metodo = "cerrar_sesion/consulta1"

        } else if (etUsuarios.text.toString().uppercase(Locale.getDefault()) == "CDE") {

            metodo = "cerrar_sesion/consulta2"

        } else {

            metodo = "cerrar_sesion/consulta3"
        }


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_USUARIO", etUsuarios.text.toString().uppercase(Locale.getDefault()))
            .build()

        var result = HttpRequest.call("", metodo, formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscaDetalleArticulo) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {

            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filaObject : JSONObject = filaArray.get(i) as JSONObject

                val cs = ConsultaSesion()
                try {
                    cs.id = filaObject.get("ID").toString()
                } catch (e: Exception) {
                    cs.id = ""
                }
                try {
                    cs.codUsuario = filaObject.get("COD_USUARIO").toString()
                } catch (e: Exception) {
                    cs.codUsuario = ""
                }
                try {
                    cs.ip = filaObject.get("IP").toString()
                } catch (e: Exception) {
                    cs.ip = ""
                }
                try {
                    cs.estado = filaObject.get("ESTADO").toString()
                } catch (e: Exception) {
                    cs.estado = ""
                }
                try {
                    cs.inicio = filaObject.get("INICIO").toString()
                } catch (e: Exception) {
                    cs.inicio = ""
                }
                try {
                    cs.version = filaObject.get("VERSION").toString()
                } catch (e: Exception) {
                    cs.version = ""
                }
                try {
                    cs.imei = filaObject.get("IMEI").toString()
                } catch (e: Exception) {
                    cs.imei = ""
                }
                try {
                    cs.cierre = filaObject.get("CIERRE").toString()
                } catch (e: Exception) {
                    cs.cierre = ""
                }
                try {
                    cs.fecha = filaObject.get("FECHA").toString()
                } catch (e: Exception) {
                    cs.fecha = ""
                }


                usuarioSesion.add(cs)

            }

            //CARGAR
            val gridLayoutManager = GridLayoutManager(context, 1)

            rvUsuarios.layoutManager = gridLayoutManager
            rvUsuarios.itemAnimator = DefaultItemAnimator()
            rvUsuarios.setHasFixedSize(true)


            // this creates a vertical layout Manager
            rvUsuarios.layoutManager = LinearLayoutManager(context)


            val adapter = AdapterSesion(
                context,
                usuarioSesion,
                R.layout.card_view_list_sesiones  )

            // Setting the Adapter with the recyclerview

            rvUsuarios.adapter = adapter


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (buscarSesion)")

        }

    }




}