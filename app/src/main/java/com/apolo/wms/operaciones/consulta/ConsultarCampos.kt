package com.apolo.wms.operaciones.consulta

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.consulta.ConsultaCampo
import com.apolo.wms.operaciones.consulta.adapter.AdapterConsultaCampo
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.consultar_campos.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class ConsultarCampos : AppCompatActivity() {

    companion object {
        lateinit var context : Context

        var campoConsulta = ArrayList<ConsultaCampo>()
        var posicionConsultaCampo = 0

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consultar_campos)

        inicializar()

    }


    fun inicializar() {


        context = this

        title = "CONSULTAR CAMPOS DE TABLAS"

        btnBuscarCampo.setOnClickListener { buscarCampos(etNombreTabla.text.toString().trim()) }


    }


    fun buscarCampos(tabla: String) {

        posicionConsultaCampo = 0
        campoConsulta = ArrayList()



        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("NOM_TABLA", tabla)
            .build()

        var result = HttpRequest.call("", "otros/buscar_campos", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscarCampos) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filaObject : JSONObject = filaArray.get(i) as JSONObject

                val cs = ConsultaCampo()
                cs.columnName = filaObject.get("COLUMN_NAME").toString()


                campoConsulta.add(cs)

            }

            val gridLayoutManager = GridLayoutManager(context, 1)

            rvCampos.layoutManager = gridLayoutManager
            rvCampos.itemAnimator = DefaultItemAnimator()
            rvCampos.setHasFixedSize(true)


            // this creates a vertical layout Manager
            rvCampos.layoutManager = LinearLayoutManager(context)


            val adapter = AdapterConsultaCampo(
                context,
                campoConsulta,
                R.layout.card_view_lista_campos  )

            // Setting the Adapter with the recyclerview

            rvCampos.adapter = adapter


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (consultaCampo)")


        }



    }



}