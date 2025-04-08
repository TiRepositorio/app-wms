package com.apolo.wms.operaciones.entrada

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.entrada.PlanillaEntrada
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.lector_codigo_jaula_almacenamiento.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject


class BuscarPlanillaEntrada : AppCompatActivity() {


    companion object {
        lateinit var context : Context
        var planillaEntrada = ArrayList<PlanillaEntrada>()
        var planillaEntradaSeleccionada = PlanillaEntrada()
    }


    private lateinit var dialogo_lee_codigo: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buscador_planilla_entrada)

        inicializar()

    }

    fun inicializar() {

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_launcher5)

        title = "ENTRADA"
        context = this

        leerJaula()

    }


    private fun leerJaula() {


        try {
            dialogo_lee_codigo.dismiss()
        } catch (e: Exception) {
        }
        dialogo_lee_codigo = Dialog(this)
        dialogo_lee_codigo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_lee_codigo.setContentView(R.layout.lector_codigo_jaula_almacenamiento)


        dialogo_lee_codigo.tvTituloDireccion.text = "REGISTRAR JAULA"

        dialogo_lee_codigo.etCodigoBarra.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_NULL) {

                //dialogo_lee_codigo.etDesvioFocus.requestFocus()
                mensaje2()
                //return true
            }
            true
        })


        dialogo_lee_codigo.etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n") > -1) {
                    //dialogo_lee_codigo.etDesvioFocus.requestFocus()
                    mensaje2()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        dialogo_lee_codigo.etCodigoBarra.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus){
            //    mensaje2()
            }
        }

        dialogo_lee_codigo.btnAceptar.setOnClickListener {

            if (MainActivity.usuarioLogin.codUsuario == "INV" ) {
                dialogo_lee_codigo.dismiss();
            }else{
                finish();
            }

        }

        dialogo_lee_codigo.btn_volver.setOnClickListener {
                finish();
        }

        dialogo_lee_codigo.setCanceledOnTouchOutside(false)
        dialogo_lee_codigo.show()



    }


    fun mensaje2() {
        var mensaje = "CODIGO DE JAULA NO CORRESPONDE!"

        dialogo_lee_codigo.etCodigoBarra.setText(dialogo_lee_codigo.etCodigoBarra.text.toString().replace("\n", ""))


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_BARRA", dialogo_lee_codigo.etCodigoBarra.text.toString())
            .build()


        var result = HttpRequest.call("", "entrada/buscar_planilla_entrada", formBody)


        planillaEntrada = ArrayList()
        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (mensaje2) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val planillasEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until planillasEntradaArray.length()) {
                val planillaEntradaObject : JSONObject = planillasEntradaArray.get(i) as JSONObject
                val pe = PlanillaEntrada()
                pe.codEmpresa = planillaEntradaObject.get("COD_EMPRESA").toString()
                pe.codSucursal = planillaEntradaObject.get("COD_SUCURSAL").toString()
                pe.descSucursal = planillaEntradaObject.get("DESC_SUCURSAL").toString()
                pe.codDeposito = planillaEntradaObject.get("COD_DEPOSITO").toString()
                pe.descDeposito = planillaEntradaObject.get("DESC_DEPOSITO").toString()
                pe.tipComprobante = planillaEntradaObject.get("TIP_COMPROBANTE").toString()
                pe.serComprobante = planillaEntradaObject.get("SER_COMPROBANTE").toString()
                pe.nroComprobante = planillaEntradaObject.get("NRO_COMPROBANTE").toString()
                pe.codBarra = planillaEntradaObject.get("COD_BARRA").toString()
                pe.tipoCarga = planillaEntradaObject.get("TIPO_CARGA").toString()
                planillaEntrada.add(pe)
            }
        }

        if (planillaEntrada.size > 0) {

            if (dialogo_lee_codigo.etCodigoBarra.text.toString() == planillaEntrada[0].codBarra) {

                dialogo_lee_codigo.dismiss()

                mensaje = "JAULA IDENTIFICADA"

                val i = Intent(this@BuscarPlanillaEntrada, EntradaMercaderia::class.java)
                planillaEntradaSeleccionada = planillaEntrada[0]
                startActivity(i)

            }

        }

        Toast.makeText(this@BuscarPlanillaEntrada, mensaje, Toast.LENGTH_LONG).show()




    }


}