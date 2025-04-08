package com.apolo.wms.operaciones.fraccionado

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.fraccionado.PlanillaFraccionado
import com.apolo.wms.operaciones.fraccionado.adapter.AdapterPlanillaFraccionado
import com.apolo.wms.operaciones.separacion.ConfirmaSeparacion
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.consultar_jaula.*
import kotlinx.android.synthetic.main.consultar_jaula.btnCancelar
import kotlinx.android.synthetic.main.consultar_jaula.etCodigoBarra
import kotlinx.android.synthetic.main.fraccionado_elige_jaula.*
import kotlinx.android.synthetic.main.fraccionado_elige_jaula.view.*
import kotlinx.android.synthetic.main.lector_codigo_fraccionado.*
//import kotlinx.android.synthetic.main.lector_codigo_fraccionado.btnAceptar
import kotlinx.android.synthetic.main.lector_codigo_fraccionado.btn_volver
import kotlinx.android.synthetic.main.separacion_conferencia.*
import kotlinx.coroutines.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class ConsultaJaula : AppCompatActivity() {

    companion object {
        lateinit var context : Context

        var planillaFraccionado = ArrayList<PlanillaFraccionado>()
        var posicionPlanilla = 0

        var planillaFraccionadoOriginal = ArrayList<PlanillaFraccionado>()
//        var numero_planilla = String

    }

    private lateinit var leeCodigo: Dialog
    private lateinit var dialogo_selecciona_planilla: Dialog
//    private lateinit var  progressBar: ProgressBar

    var posicionSpinnerPlanilla = 0


    var codigoJaula = "0"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consultar_jaula)

//        progressBar  = findViewById(R.id.progressBar)

        inicializar()

    }


    fun inicializar() {


        context = this

        title = "CONFIRMACIÓN DE FRACCIONADO - ${MainActivity.usuarioLogin.codUsuario}"

        /*codigo de barra de producto*/
        etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // DETECTA EL CODIGO DE BARRA AL ESCANEAR EL CODIGO DE BARRA VOLUMEN
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (!etCodigoBarra.text.toString().isEmpty()) {
                    try {
                        verificarEstado()
                    } catch (e: InterruptedException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                }

            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etCodigoBarra.setOnFocusChangeListener { view, _ ->


            if (!view.hasFocus()) {
                try {
                    verificarEstado()
                } catch (e: InterruptedException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }

            }


        }


        leerJaula()


        tvTitulo2.setOnClickListener {

            leerJaula()

        }


        btnCancelar.setOnClickListener { cancelar() }


    }

    fun cancelar() {

        tvTitulo2.text = ""
        cargarPlanilla("", "")
        etCodigoBarra.setText("")
        etPlanilla.setText("")
        leerJaula()

    }




    fun cargarPlanilla(codigo: String, planillas: String) {

        //Mostrar el progressbar

//        progressBar.visibility = View.VISIBLE
//        GlobalScope.launch(Dispatchers.IO){

            posicionPlanilla = 0
            planillaFraccionado = ArrayList()


//        }



        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", codigo)
            .add("NRO_PLANILLA", planillas)
            .build()

        var result = HttpRequest.call("", "fraccionado/cargar_planilla", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cargar_planilla) Error ${e.message.toString()} !")

            return
        }


        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filaObject : JSONObject = filaArray.get(i) as JSONObject

                val pf = PlanillaFraccionado()
                pf.codDeposito = filaObject.get("COD_DEPOSITO").toString()
                pf.codBarraVol = filaObject.get("COD_BARRA_VOL").toString()
                pf.nroPlanilla = filaObject.get("NRO_PLANILLA").toString()
                pf.codGrupo = filaObject.get("COD_GRUPO").toString()
                pf.codEmpresa = filaObject.get("COD_EMPRESA").toString()
                pf.codSucursal = filaObject.get("COD_SUCURSAL").toString()
                pf.serPlanilla = filaObject.get("SER_PLANILLA").toString()
                pf.tipPlanilla = filaObject.get("TIP_PLANILLA").toString()
                pf.descripcion = filaObject.get("DESCRIPCION").toString()
                pf.nroVolumen = filaObject.get("NRO_VOLUMEN").toString()
                pf.codCliente = filaObject.get("COD_CLIENTE").toString()
                pf.codSubcliete = filaObject.get("COD_SUBCLIETE").toString()
                pf.codSubcliente = filaObject.get("COD_SUBCLIENTE").toString()
                pf.cantidadVolumenes = filaObject.get("CANTIDAD_VOLUMENES").toString()
                pf.estado = "N"


                planillaFraccionado.add(pf)

            }

            planillaFraccionadoOriginal = planillaFraccionado

            val gridLayoutManager = GridLayoutManager(context, 1)

            rvListaFraccionado.layoutManager = gridLayoutManager
            rvListaFraccionado.itemAnimator = DefaultItemAnimator()
            rvListaFraccionado.setHasFixedSize(true)


            // this creates a vertical layout Manager
            rvListaFraccionado.layoutManager = LinearLayoutManager(context)


            val adapter = AdapterPlanillaFraccionado(
                context,
                planillaFraccionado,
                R.layout.card_view_planilla_fraccionado  )

            // Setting the Adapter with the recyclerview

            rvListaFraccionado.adapter = adapter


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargarPlanilla)")


        }




    }



    fun leerJaula() {

        leeCodigo = Dialog(context)
        leeCodigo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        leeCodigo.setContentView(R.layout.lector_codigo_fraccionado)


        /*codigo de barra de producto*/
        leeCodigo.etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                // TODO Auto-generated method stub
                if (leeCodigo.etCodigoBarra.text.length == 1) {

                    var title2 = "Error!"
                    var message = "Debe ingresar el código a través del lector de códigos."
                    var button = "OK"
                    MainActivity.funciones.mensajeError(context, title2, message)

                    leeCodigo.etCodigoBarra.setText("")
                    leeCodigo.etCodigoBarra.requestFocus()
                }
                tvTitulo2.text = leeCodigo.etCodigoBarra.text.toString().replace("\n", "")
                codigoJaula = leeCodigo.etCodigoBarra.text.toString().replace("\n", "")
                codigoJaula = leeCodigo.etCodigoBarra.text.toString().replace("\n", "").replace("A", "").replace("a", "")
                if (codigoJaula.length >3) {
                    var cod1 = codigoJaula.substring(0,3)
                    var cod2 = codigoJaula.substring(3)
                    if (codigoJaula.substring(0,3).equals(codigoJaula.substring(3))) {
                        codigoJaula = codigoJaula.substring(3)
                        tvTitulo2.text = codigoJaula
                    }
                }
                if (codigoJaula.length == 3) {
                    codigoJaula = codigoJaula.substring(0,1)+codigoJaula.substring(1,2)+codigoJaula.substring(2,3).replace("\n", "")
                    tvTitulo2.text = codigoJaula
                }

            }
            override fun afterTextChanged(s: Editable?) {}
        })

        leeCodigo.etCodigoBarra.setOnKeyListener { view, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                var codigoProcesado = leeCodigo.etCodigoBarra.text.toString()
                    .replace("\n", "")
                    .replace("A", "")
                    .replace("a", "")

                // Lógica de procesamiento del código
                if (codigoProcesado.length > 3) {
                    val cod1 = codigoProcesado.substring(0, 3)
                    val cod2 = codigoProcesado.substring(3)
                    if (cod1 == cod2) {
                        codigoProcesado = codigoProcesado.substring(3)
                    }
                }

                if (codigoProcesado.length == 3) {
                    codigoProcesado = codigoProcesado.substring(0, 1) +
                            codigoProcesado.substring(1, 2) +
                            codigoProcesado.substring(2, 3)
                }

                if (validaJaula(codigoProcesado)) {
                    if (validaPendienteJaula(codigoProcesado)) {
                        leeCodigo.dismiss()
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atención", "La jaula no tiene planillas pendientes")
                        leeCodigo.etCodigoBarra.setText("")
                    }
                } else {
                    MainActivity.funciones.mensajeError(context, "Atención", "La jaula no tiene planillas pendientes")
                    leeCodigo.etCodigoBarra.setText("")
                }
                true
            } else {
                false
            }
        }


//        leeCodigo.etCodigoBarra.setOnFocusChangeListener { view, _ ->
//
//
//            if (!view.hasFocus()) {
//                var codigoProcesado = ""
//                codigoProcesado = leeCodigo.etCodigoBarra.text.toString().replace("\n", "").replace("A", "").replace("a", "")
//                if (codigoProcesado.length == 3) {
//                    codigoProcesado = codigoProcesado.substring(0,1)+codigoProcesado.substring(1,2)+codigoProcesado.substring(2,3)
//                }
//                if (codigoProcesado.length >3) {
//                    var cod1 = codigoProcesado.substring(0,3)
//                    var cod2 = codigoProcesado.substring(3,6)
//                    if (codigoProcesado.substring(0,3).equals(codigoProcesado.substring(3))) {
//                        codigoProcesado = codigoProcesado.substring(3)
//                    }
//                }
//                if (validaJaula(codigoJaula)) {
//                    if (validaPendienteJaula(codigoJaula)) {
//                        leeCodigo.dismiss()
//                    } else {
//                        MainActivity.funciones.mensajeError(context, "Atencion", "La jaula no tiene planillas pendientes")
//                        leeCodigo.etCodigoBarra.setText("")
//                    }
//                }
//                else {
//                    MainActivity.funciones.mensajeError(context, "Atencion", "La jaula no está registrada")
//                    leeCodigo.etCodigoBarra.setText("")
//                }
//            }
//
//
//        }

//        leeCodigo.btnAceptar.setOnClickListener{
//            leeCodigo.cancel()
//            if (MainActivity.usuarioLogin.codUsuario.uppercase(Locale.getDefault()) == "INV") {
//            }else{
//                finish()
//            }
//        }

        leeCodigo.btn_volver.setOnClickListener{
            finish()
        }


        leeCodigo.setCancelable(false)
        leeCodigo.setCanceledOnTouchOutside(false)
        leeCodigo.show()

    }

    fun validaJaula(codJaula: String) : Boolean {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_JAULA", codJaula)
            .build()

        var result = HttpRequest.call("", "fraccionado/consulta_jaula", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
//            numero_planilla = respuestaJson
//            println("Datos de consulta_jaula: $respuestaJson")
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (actualiza_estado) Error ${e.message.toString()} !")

            return false
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
            return filaArray.length() > 0

        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaJaula)")
            return false
        }

    }


    fun validaPendienteJaula(codJaula: String) : Boolean {

        var validacion = false

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_JAULA", codJaula)
            .build()

        var result = HttpRequest.call("", "fraccionado/valida_pendiente_jaula", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_pendiente_jaula) Error ${e.message.toString()} !")

            return false
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            var planillas = ArrayList<String>()


            for (i in 0 until filaArray.length()) {
                validacion = true
                val filaObject : JSONObject = filaArray.get(i) as JSONObject
                planillas.add(filaObject.get("NRO_PLANILLA").toString())
            }

            var planillaSeleccionada = ""

            if (planillas.size >0) {
                if (planillas.size >1) {
                    seleccionaPlanilla(planillas)
                }
                else{
                    planillaSeleccionada = planillas[0]
                    etPlanilla.setText(planillaSeleccionada)
                    buscarPlanilla()
                    validacion = true
                    etPlanilla.setText("Planilla Nro.: " + planillaSeleccionada)
                    etPlanilla.isEnabled = false

                    etCodigoBarra.requestFocus()
                }
            }else{
                validacion = false
            }

        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaPendienteJaula)")
            return false
        }

        return validacion

    }


    fun seleccionaPlanilla(planillas: List<String>) {

        try {
            dialogo_selecciona_planilla.dismiss()
        } catch (e: Exception) {
        }
        dialogo_selecciona_planilla = Dialog(context)
        dialogo_selecciona_planilla.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_selecciona_planilla.setContentView(R.layout.fraccionado_elige_jaula)

        val spPlanillas = dialogo_selecciona_planilla.findViewById<Spinner>(R.id.spPlanillas);
//        val btnAceptar  = dialogo_selecciona_planilla.findViewById<Button>(R.id.btnAceptar);
//        val btnVover    = dialogo_selecciona_planilla.findViewById<Button>(R.id.etVolumen);


        val spinnerAdapter : ArrayAdapter<String> =
            ArrayAdapter(context, R.layout.spinner_adapter, planillas)
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        dialogo_selecciona_planilla.spPlanillas.adapter = spinnerAdapter

        spPlanillas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                posicionSpinnerPlanilla = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }


        dialogo_selecciona_planilla.btnAceptar.setOnClickListener {

            etPlanilla.setText(spPlanillas.selectedItem.toString().trim())
            buscarPlanilla()
            etPlanilla.setText("Planilla Nro.: " + spPlanillas.selectedItem.toString().trim())
            println("Esta es la planilla $etPlanilla",)
            etPlanilla.isEnabled = false
            etCodigoBarra.requestFocus()
            dialogo_selecciona_planilla.dismiss()

        }


        dialogo_selecciona_planilla.btn_volver.setOnClickListener {
            dialogo_selecciona_planilla.dismiss()
//            finish()
        }

        dialogo_selecciona_planilla.setCancelable(false)
        dialogo_selecciona_planilla.show()



    }


    fun buscarPlanilla() {


        cargarPlanilla(codigoJaula, etPlanilla.text.toString())


    }



    fun verificarEstado() {

        var verificador = false
//        for (i in 0 until planillaFraccionado.size) {
//            MainActivity.funciones.mensajeExito(context, "", planillaFraccionado[i].codBarraVol)
//        }

        if (etCodigoBarra.text.length === 1) {
            val title: String = "Atención"
            val message: String = "El código de barras debe ser ingresado por medio del lector de código."
            MainActivity.funciones.mensajeError(context, title, message)
            etCodigoBarra.setText("")
        } else {
            var vtCodigoBarra = etCodigoBarra.text.toString().trim();
            if (!vtCodigoBarra.equals("")) {

                if(vtCodigoBarra.equals("\n")) vtCodigoBarra = etCodigoBarra.text.toString().trim().replace("\n", "")

                if (!vtCodigoBarra.equals("")) {
                    verificador = false
                    var i = 0
                    while (i < planillaFraccionado.size) {
                        if (planillaFraccionado[i].codBarraVol.equals(etCodigoBarra.text.toString().replace("\n", ""), ignoreCase = true)
                        ) {

                            var temp = planillaFraccionado[i]

                            rvListaFraccionado.adapter!!.notifyDataSetChanged()
                            verificador = true

                            var formBody: RequestBody = FormBody.Builder()
                                .add("USER", MainActivity.usuarioLogin.codUsuario)
                                .add("PASS", MainActivity.usuarioLogin.password)
                                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                                .add("COD_DEPOSITO", codigoJaula)
                                .add( "NRO_PLANILLA", temp.nroPlanilla)
                                .add("COD_BARRA", temp.codBarraVol)
                                .build()

                            var result = HttpRequest.call("", "fraccionado/actualiza_estado", formBody)

                            var respuestaJson: JSONObject

                            try {
                                respuestaJson = JSONObject(result)
                            } catch (e: Exception) {
                                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (actualiza_estado) Error ${e.message.toString()} !")
                                return
                            }

                            if (respuestaJson.has("result")) {
                                val respuesta = respuestaJson.get("result").toString()

                                if(respuesta == "S"){
                                    if (planillaFraccionado.size === 1) {
                                        planillaFraccionado.removeAt(0)
                                        MainActivity.funciones.mensajeExito(context, "Atencion!", "Correcto, ya no quedan cargas de esta jaula.")
                                    } else {
    //                                    planillaFraccionado.removeAt(i)
                                        val listaMutable = planillaFraccionado.toMutableList()
                                        val listaFiltrada = listaMutable.filter { it.codBarraVol == temp.codBarraVol }
                                        planillaFraccionado.removeAll(listaFiltrada)
                                        i = planillaFraccionado.size
                                        MainActivity.funciones.mensajeExito(context, "Atencion!", "Correcto, quedan ${planillaFraccionado.size} cargas de esta jaula.")
                                    }
                                }else{
                                    MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (actualiza_estado) !")
                                    return
                                }

                            } else {
                                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (actualiza_estado)")

                            }


                            tvDatos1.text = "Ultima confirmación"
                            tvDatos2.text = "Planilla: ${temp.nroPlanilla}"
                            tvDatos3.text = "Volumen: ${temp.codBarraVol}"
                        }
                        i++
                    }
                    var volumenConfirmado = true
                    if (verificador == false) {
                        for (i in 0 until planillaFraccionadoOriginal.size) {
                            if (planillaFraccionadoOriginal[i].codBarraVol.equals(etCodigoBarra.text.toString().replace("\n", ""), ignoreCase = true)
                            ) {
                                volumenConfirmado = false
                                var title: String = "Atención!"
                                var message: String = "El volumen ya fue confirmado."
                                MainActivity.funciones.mensajeError(context, title, message)
                            }
                        }
                        if (volumenConfirmado) {
                            MainActivity.funciones.mensajeError(context, "Atencion!", "El volumen no corresponde a esta planilla.")
                        }
                    }
                    etCodigoBarra.setText("")
                } else {
                    MainActivity.funciones.mensajeError(context, "Atencion!", "Ingrese el código de barras por medio del lector.")
                }
            }
        }

    }



}