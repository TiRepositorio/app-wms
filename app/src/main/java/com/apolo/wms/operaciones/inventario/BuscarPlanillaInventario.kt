package com.apolo.wms.operaciones.inventario

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.inventario.DireccionInventario
import com.apolo.wms.clases.inventario.PlanillaDetalleInventario
import com.apolo.wms.clases.inventario.PlanillaInventario
import com.apolo.wms.operaciones.inventario.adapter.AdapterPlanillaInventario
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.buscador_planilla_inventario.*
import kotlinx.android.synthetic.main.entrada_redireccion_ub.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat


class BuscarPlanillaInventario : AppCompatActivity() {


    companion object {
        lateinit var context : Context

        var planillaInventario = ArrayList<PlanillaInventario>()
        var posicionPlanilla = 0
        var planillaInventarioSeleccionada = PlanillaInventario()

        var planillaDetInventario = ArrayList<PlanillaDetalleInventario>()


        var direccionInventario = ArrayList<DireccionInventario>()
        var posicionDireccion = 0


        var _nro_calle = ""
        var _nro_lado = ""
        var _es_piking = ""
        var _es_controlado = "N"


        var regenerarCab = false

    }

    lateinit var m4_cod_calle: Array<String>
    lateinit var m4_cod_lado: Array<String>
    lateinit var m4_es_piking: Array<String>

    private var posicion_spCodCalle = 0
    private val posicion_spCodLado = 0
    private var posicion_spEsPiking = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buscador_planilla_inventario)

        inicializar()

    }

    fun inicializar() {


        context = this

        title = "INVENTARIO"

        regenerarCab	= false
        posicionPlanilla = -1


        btnSeleccionar.setOnClickListener { seleccionar() }
        ibtnBuscarPlanilla.setOnClickListener { buscar() }

    }


    fun seleccionar() {

        if(posicionPlanilla == -1){
            MainActivity.funciones.mensajeError(context, "Atencion", "Debes indicar el registro para seleccionar")
            return
        }

        var pi = planillaInventario[posicionPlanilla]


        if(pi.tipInventario == "A" || pi.tipInventario == "P"){

            planillaInventarioSeleccionada = pi
            context.startActivity(Intent(context, DetallePlanillaInventario::class.java))

        }else{
            if(pi.tipInventario == "G"){
                llamaCuadroNcr()
            }
        }


    }



    fun buscar() {


        posicionPlanilla = -1
        buscarPlanilla(etBuscador.text.toString())

    }


    fun buscarPlanilla(filtro: String) {

        posicionPlanilla = 0
        planillaInventario = ArrayList()

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("FILTRO", filtro)
            .build()

        var result = HttpRequest.call("", "inventario/buscar_planilla", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscar_planilla) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filasObject : JSONObject = filaArray.get(i) as JSONObject

                val pi = PlanillaInventario()
                pi.codEmpresa = filasObject.get("COD_EMPRESA").toString()
                pi.tipComprobante = filasObject.get("TIP_COMPROBANTE").toString()
                pi.serComprobante = filasObject.get("SER_COMPROBANTE").toString()
                pi.nroComprobante = filasObject.get("NRO_COMPROBANTE").toString()
                pi.fecInventario = filasObject.get("FEC_INVENTARIO").toString()
                pi.codUsuarioAlta = filasObject.get("COD_USUARIO_ALTA").toString()
                pi.codDeposito = filasObject.get("COD_DEPOSITO").toString()
                pi.tipInventario = filasObject.get("TIP_INVENTARIO").toString()
                pi.invVerificado = "N"

                planillaInventario.add(pi)

            }



            planillaDetInventario = ArrayList()

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .build()

            var result = HttpRequest.call("", "inventario/buscar_planilla_detalle", formBody)

            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscar_planilla_detalle) Error ${e.message.toString()} !")
                return
            }

            if (respuestaJson.has("rows")) {
                val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


                for (i in 0 until filaArray.length()) {
                    val filasObject : JSONObject = filaArray.get(i) as JSONObject

                    val pdi = PlanillaDetalleInventario()
                    pdi.tipComprobante = filasObject.get("TIP_COMPROBANTE").toString()
                    pdi.serComprobante = filasObject.get("SER_COMPROBANTE").toString()
                    pdi.nroComprobante = filasObject.get("NRO_COMPROBANTE").toString()


                    planillaDetInventario.add(pdi)

                }

                planillaInventario.forEach {

                    var claveA = "${it.tipComprobante}|${it.serComprobante}|${it.nroComprobante}"

                    planillaDetInventario.forEach { it2 ->

                        var claveB = "${it2.tipComprobante}|${it2.serComprobante}|${it2.nroComprobante}"

                        if (claveA == claveB) {
                            it.invVerificado = "S"
                        }

                    }

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
                val adapter = AdapterPlanillaInventario (
                    context,
                    planillaInventario,
                    R.layout.card_view_planilla_inventario  )

                // Setting the Adapter with the recyclerview
                rvPlanillaInv.adapter = adapter


            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (buscarPlanillaDet)")
                rvPlanillaInv.adapter!!.notifyDataSetChanged()

            }


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (buscarPlanilla)")
            rvPlanillaInv.adapter!!.notifyDataSetChanged()

        }

    }


    fun llamaCuadroNcr() {

        posicionDireccion = 0
        direccionInventario = ArrayList()

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        var result = HttpRequest.call("", "inventario/consulta_direcciones", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_direcciones) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filasObject : JSONObject = filaArray.get(i) as JSONObject

                val di = DireccionInventario()
                di.codEmpresa = filasObject.get("COD_EMPRESA").toString()
                di.codSucursal = filasObject.get("COD_SUCURSAL").toString()
                di.codCalle = filasObject.get("COD_CALLE").toString()
                di.nroOrden = filasObject.get("NRO_ORDEN").toString()
                di.codTipo = filasObject.get("COD_TIPO").toString()


                direccionInventario.add(di)

            }


            val alert_motivos = AlertDialog.Builder(this)
            alert_motivos.setTitle("      Inventario ")

            val subTitulo = "ESPECIFIQUE: "
            alert_motivos.setMessage(subTitulo)



            val cLado: String
            val cEsPiking: String
            cLado = "Derecho||Izquierdo"
            cEsPiking = "Si||No"
            m4_cod_lado = cLado.split("\\|\\|".toRegex()).toTypedArray()
            m4_es_piking = cEsPiking.split("\\|\\|".toRegex()).toTypedArray()

            val met = resources.displayMetrics

            val spCodCalle = Spinner(context)
            val spCodLado = Spinner(context)
            val spEsPiking = Spinner(context)

            val lblSubTitulo = TextView(context)
            val lblCalle = TextView(context)
            val lblLado = TextView(context)
            val lblPicking = TextView(context)


            val nf: NumberFormat = NumberFormat.getInstance()
            nf.minimumFractionDigits = "0".toInt()
            nf.maximumFractionDigits = "0".toInt()


            // Diseño para el número de teléfono
            // Diseño para el número de teléfono
            val llEspacios = LinearLayout(context)
            llEspacios.layoutParams = ViewGroup.LayoutParams(20, 40)

            lblCalle.text = "Calle:"
            lblLado.text = "Lado:"
            lblPicking.text = "Picking:"

            //---------------------------------------------------------------//
            //---------------------------------------------------------------//
            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL


            llEspacios.isBaselineAligned = true
            llEspacios.orientation = LinearLayout.HORIZONTAL

            //calle

            //calle
            val llPortaCalle = LinearLayout(context)
            llPortaCalle.layoutParams = ViewGroup.LayoutParams(500, 120)
            llPortaCalle.addView(lblCalle)
            llPortaCalle.isBaselineAligned = true
            llPortaCalle.orientation = LinearLayout.VERTICAL
            llPortaCalle.addView(spCodCalle)


            //lado


            //lado
            val llPortaLado = LinearLayout(context)
            llPortaLado.layoutParams = ViewGroup.LayoutParams(500, 120)
            llPortaLado.addView(lblLado)
            llPortaLado.isBaselineAligned = true
            llPortaLado.orientation = LinearLayout.VERTICAL
            llPortaLado.addView(spCodLado)

            //piking o pulmón

            //piking o pulmón
            val llPortaPiking = LinearLayout(context)
            llPortaPiking.layoutParams = ViewGroup.LayoutParams(500, 120)
            llPortaPiking.addView(lblPicking)
            llPortaPiking.isBaselineAligned = true
            llPortaPiking.orientation = LinearLayout.VERTICAL
            llPortaPiking.addView(spEsPiking)


            llPortaCalle.setPadding(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5f, met).toInt(),
                0,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5f, met).toInt(),
                0
            )

            llPortaLado.setPadding(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5f, met).toInt(),
                0,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5f, met).toInt(),
                0
            )

            llPortaPiking.setPadding(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5f, met).toInt(),
                0,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5f, met).toInt(),
                0
            )

            layout.addView(llPortaCalle)
            layout.addView(llPortaLado)
            layout.addView(llPortaPiking)
            layout.addView(llEspacios)

            alert_motivos.setView(layout)


            //---------------------------------------------//
            //---------------------------------------------//
            val adapterCalle = ArrayAdapter(
                context,
                R.layout.simple_spinner_item_textsize_12, m4_cod_calle
            )
            adapterCalle.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_textsize_12)
            spCodCalle.adapter = adapterCalle
            spCodCalle.setSelection(0)
            //---------------------------------------------//
            //---------------------------------------------//
            val adapterLado = ArrayAdapter(
                context,
                R.layout.simple_spinner_item_textsize_12, m4_cod_lado
            )
            adapterLado.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_textsize_12)
            spCodLado.adapter = adapterLado
            spCodLado.setSelection(0)
            //---------------------------------------------//
            //---------------------------------------------//
            val adapterEsPiking = ArrayAdapter(
                context,
                R.layout.simple_spinner_item_textsize_12, m4_es_piking
            )
            adapterEsPiking.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_textsize_12)
            spEsPiking.adapter = adapterEsPiking
            spEsPiking.setSelection(0)



            spCodCalle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    posicion_spCodCalle = position
                    _nro_calle = m4_cod_calle[position]
                    //------------------------------------//
                    if (direccionInventario[position].codTipo == "1") {
                        spCodLado.isEnabled = false
                        spEsPiking.isEnabled = false
                        _es_controlado = "N"
                    } else {
                        spCodLado.isEnabled = true
                        spEsPiking.isEnabled = true
                        _es_controlado = "S"
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }


            //---------------------------------------------//
            //---------------------------------------------//
            spCodLado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    posicion_spCodCalle = position
                    if (m4_cod_lado[position] == "Derecho") {
                        _nro_lado = "P"
                    } else {
                        _nro_lado = "I"
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }


            //---------------------------------------------//
            //---------------------------------------------//
            spEsPiking.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    posicion_spEsPiking = position
                    if (m4_es_piking[position] == "Si") {
                        _es_piking = "S"
                    } else {
                        _es_piking = "N"
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }


            alert_motivos.setPositiveButton(
                "Aceptar"
            ) { dialog, whichButton ->

                if (posicionPlanilla !== -1) {

                    context.startActivity(Intent(context, DetallePlanillaInventarioG::class.java))

                }


            }
            //---------------------------------------------//
            //---------------------------------------------//
            alert_motivos.setNegativeButton(
                "Cancelar"
            ) { dialog, whichButton -> dialog.cancel() }
            //---------------------------------------------//
            //---------------------------------------------//
            val motivos = alert_motivos.create()
            motivos.show()




        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (llamaCuadroNcr)")

        }


    }


    override fun onResume() {

        super.onResume()
        if (regenerarCab) {
            buscarPlanilla(etBuscador.text.toString())
            regenerarCab = false
        }
        etBuscador.setText("")
        etBuscador.requestFocus()
    }


}