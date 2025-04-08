package com.apolo.wms.operaciones.consulta

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.consulta.fraccionado.ConsultaFraccionado
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.activity_buscar_fraccionado.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class BuscarFraccionado : AppCompatActivity() {

    companion object {
        lateinit var context : Context


        var planillaFraccionado = ArrayList<ConsultaFraccionado>()
        var posicionPlanilla = 0

    }


    var planilla: ArrayList<String> = ArrayList()
    var jaula: ArrayList<String> = ArrayList()
    var numero: ArrayList<String> = ArrayList()
    var cliente: ArrayList<String> = ArrayList()
    var estado: ArrayList<String> = ArrayList()
    var usuario: ArrayList<String> = ArrayList()
    var fecha: ArrayList<String> = ArrayList()

    var contador = 0
    var indice = 0

    var codigo = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_fraccionado)
        val etVolumen = findViewById<EditText>(R.id.etVolumen)
        inicializar()

    }


    fun inicializar() {


        context = this

        title = "BUSCAR VOLUMEN"

        btnAnterior.isEnabled = false
        btnSiguiente.isEnabled = false

        btnBuscar.setOnClickListener {
            buscar(etVolumen.text.toString().trim().replace("\n", ""))
//            tvCodigoVolumenBD.text = ""
        }


        btnCancelar.setOnClickListener {
            limpiar()
        }
//agruegar limpiar adit text volumen
        btnAnterior.setOnClickListener {
            anterior(indice)
            mostrar(codigo, planilla, jaula, numero, cliente, estado, usuario, fecha)
        }

        btnSiguiente.setOnClickListener {
            siguiente(indice)
            mostrar(codigo, planilla, jaula, numero, cliente, estado, usuario, fecha)
        }


    }


    fun mostrar(codigo: String,
                planilla: List<String>,
                jaula: List<String>,
                numero: List<String>,
                cliente: List<String>,
                estado: List<String>,
                usuario: List<String>,
                fecha: List<String>) {


        tvCodigoVolumenBD.text = codigo
        tvPlanillaBD.text = planilla[contador]
        tvJaulaBD.text = jaula[contador]
        tvNroVolumenBD.text = numero[contador]
        tvClienteBD.text = cliente[contador]
        tvUsuarioBD.text = usuario[contador]
        tvFechaBD.text = fecha[contador]
        if (estado[contador].toString().trim().uppercase(Locale.getDefault()) == "S") {
            cbEstadoS.isChecked = true
            cbEstadoN.isChecked = false
        }else if (estado[contador].toString().trim().uppercase(Locale.getDefault()) == "N") {
            cbEstadoN.isChecked = true
            cbEstadoS.isChecked = false
        }else{
            cbEstadoS.isChecked = false
            cbEstadoN.isChecked = false
        }

    }


    fun siguiente(indice: Int) {

        contador++
        if (contador < indice - 1 && contador > 0) {
            btnAnterior.isEnabled = true
        }else{
            btnSiguiente.isEnabled = false
            btnAnterior.isEnabled = true
        }

    }


    fun anterior(indice: Int){
        contador--
        if (contador==0) {
            btnAnterior.isEnabled = false
            btnSiguiente.isEnabled = true
        }
        if (contador > 0 && contador < indice) {
            btnSiguiente.isEnabled = true
        }

    }


    fun limpiar() {

        tvCodigoVolumenBD.text = ""
        tvJaulaBD.text = ""
        tvNroVolumenBD.text = ""
        tvPlanillaBD.text = ""
        tvFechaBD.text = ""
        tvUsuarioBD.text = ""
        cbEstadoN.isChecked = false
        cbEstadoS.isChecked = false
        tvClienteBD.text = ""
        limpiarLista(jaula)
        limpiarLista(numero)
        limpiarLista(planilla)
        limpiarLista(fecha)
        limpiarLista(usuario)
        limpiarLista(cliente)
        btnAnterior.isEnabled = false
        btnSiguiente.isEnabled = false

    }

    fun limpiarLista(lista: ArrayList<String>) {
        while (lista.isNotEmpty()) {
            lista.removeAt(0)
        }
    }


    fun buscar(codigo: String) {

        this.codigo = codigo

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("CODIGO", codigo)
            .build()

        var result = HttpRequest.call("", "fraccionado/buscar_fraccionado", formBody)

        planilla = ArrayList()
        jaula = ArrayList()
        numero = ArrayList()
        cliente = ArrayList()
        estado = ArrayList()
        usuario = ArrayList()
        fecha = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscar) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filaObject : JSONObject = filaArray.get(i) as JSONObject

                val cf = ConsultaFraccionado()
                cf.codDeposito = filaObject.get("COD_DEPOSITO").toString()
                cf.codBarraVol = filaObject.get("COD_BARRA_VOL").toString()
                cf.nroPlanilla = filaObject.get("NRO_PLANILLA").toString()
                cf.codGrupo = filaObject.get("COD_GRUPO").toString()
                cf.codEmpresa = filaObject.get("COD_EMPRESA").toString()
                cf.codSucursal = filaObject.get("COD_SUCURSAL").toString()
                cf.serPlanilla = filaObject.get("SER_PLANILLA").toString()
                cf.tipPlanilla = filaObject.get("TIP_PLANILLA").toString()
                cf.descripcion = filaObject.get("DESCRIPCION").toString()
                cf.nroVolumen = filaObject.get("NRO_VOLUMEN").toString()
                cf.codCliente = filaObject.get("COD_CLIENTE").toString()
                cf.codSubcliete = filaObject.get("COD_SUBCLIETE").toString()
                cf.codSubcliente = filaObject.get("COD_SUBCLIENTE").toString()
                cf.estado = filaObject.get("ESTADO").toString()
                cf.cantidadVolumenes = filaObject.get("CANTIDAD_VOLUMENES").toString()
                cf.codUsuario = filaObject.get("COD_USUARIO").toString()
                cf.fecPlanilla = filaObject.get("FEC_PLANILLA").toString()


                planillaFraccionado.add(cf)


                planilla.add(filaObject.get("NRO_PLANILLA").toString())
                jaula.add(filaObject.get("COD_DEPOSITO").toString())
                numero.add("${filaObject.get("NRO_VOLUMEN")}/${filaObject.get("CANTIDAD_VOLUMENES")}")
                cliente.add("Cod. Cliente:\t ${filaObject.get("COD_CLIENTE")}"
                        + "\nCod. Subcliente:\t ${filaObject.get("COD_SUBCLIENTE")}"
                        + "\nDescripción:\t ${filaObject.get("DESCRIPCION")}")
                estado.add(filaObject.get("ESTADO").toString())
                usuario.add(filaObject.get("COD_USUARIO").toString())
                fecha.add(filaObject.get("FEC_PLANILLA").toString())


            }


            indice = planilla.size

            if (planilla.isEmpty()) {
                limpiar()
                MainActivity.funciones.mensajeError(context, "No se encontro el volumen", "El volumen no fue registrado en las ultimas 24hs.")
            }else{
                contador = 0;
                mostrar(codigo, planilla, jaula, numero, cliente, estado, usuario, fecha);
                if (indice>1) {
                    btnSiguiente.setEnabled(true);
                }
            }





        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (buscarFraccionado)")


        }



    }



}