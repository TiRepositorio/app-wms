package com.apolo.wms.operaciones.separacion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.separacion.PlanillaSeparacion
import com.apolo.wms.operaciones.reabastecimientocorrprev.ReabastecimientoCorrPrev
import com.apolo.wms.operaciones.separacion.adapter.AdapterPlanillaSeparacion
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.buscador_planilla_separacion.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class BuscarPlanillaSeparacion : AppCompatActivity() {



    companion object {
        lateinit var context : Context


        var planillaSeparacion = ArrayList<PlanillaSeparacion>()
        var planillaSeparacionSeleccionada = PlanillaSeparacion()
        var posicionPlanilla = -1

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buscador_planilla_separacion)

        inicializar()

    }

    fun inicializar() {

        context = this

        title = "SELECCIONAR PLANILLA SEPARACIÓN".uppercase(Locale.getDefault())


        btnSeleccionar.setOnClickListener { seleccionarPlanilla() }
        ibtnBuscarPlanilla.setOnClickListener { buscarPlanilla() }


    }


    fun seleccionarPlanilla() {

        if (posicionPlanilla !== -1) {

            val i = Intent(context, BuscaGrupoSeparacion::class.java)
            planillaSeparacionSeleccionada = planillaSeparacion[posicionPlanilla]
            startActivity(i)

        }

    }


    fun buscarPlanilla() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        var result = HttpRequest.call("", "separacion/buscar_planilla", formBody)

        posicionPlanilla = 0
        planillaSeparacion = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscar_planilla) Error ${e.message.toString()} !")
            return
        }


        if (respuestaJson.has("rows")) {

            val planillaSeparacionArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until planillaSeparacionArray.length()) {
                val planillaSeparacionObject : JSONObject = planillaSeparacionArray.get(i) as JSONObject
                val ps = PlanillaSeparacion()
                ps.codEmpresa = planillaSeparacionObject.get("COD_EMPRESA").toString()
                ps.tipPlanilla = planillaSeparacionObject.get("TIP_PLANILLA").toString()
                ps.serPlanilla = planillaSeparacionObject.get("SER_PLANILLA").toString()
                ps.nroPlanilla = planillaSeparacionObject.get("NRO_PLANILLA").toString()
                ps.fecha = planillaSeparacionObject.get("FECHA").toString()
                ps.codUsuario = planillaSeparacionObject.get("COD_USUARIO").toString()
                ps.codJaula = planillaSeparacionObject.get("COD_JAULA").toString()

                planillaSeparacion.add(ps)

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
            val adapter = AdapterPlanillaSeparacion(context,
                                                    planillaSeparacion,
                                                    R.layout.card_view_planilla_separacion)

            // Setting the Adapter with the recyclerview
            rvPlanillaInv.adapter = adapter


        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (verificaReabastecimientoBase)")
        }



    }

}