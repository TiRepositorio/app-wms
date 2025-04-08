package com.apolo.wms.operaciones.direccionamiento

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.operaciones.reabastecimientocorrprev.ReabastecimientoCorrPrev
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.entrada_direccionamiento_direcciones.*
import kotlinx.android.synthetic.main.lector_codigo_direccion_direccionamiento.*
import kotlinx.android.synthetic.main.lector_codigo_jaula_almacenamiento.*
import kotlinx.android.synthetic.main.lector_codigo_jaula_almacenamiento.btnAceptar
import kotlinx.android.synthetic.main.lector_codigo_jaula_almacenamiento.btn_volver
import kotlinx.android.synthetic.main.lector_codigo_jaula_almacenamiento.etCodigoBarra
import kotlinx.android.synthetic.main.lector_codigo_jaula_almacenamiento.tvTituloDireccion
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class DireccionamientoDirecciones : AppCompatActivity() {


    companion object {
        lateinit var context : Context
    }


    private lateinit var dialogo_lee_codigo_jaula: Dialog
    private lateinit var dialogo_lee_codigo_palet: Dialog
    private lateinit var dialogo_lee_codigo_direccion: Dialog

    var codDeposito = ""
    var nroOperacion = ""
    var dirDestino = ""
    var dirDestinoPropuesto = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entrada_direccionamiento_direcciones)

        inicializar()

    }


    fun inicializar() {

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_launcher5)

        title = "DIRECCIONAMIENTO DE DIRECCIONES"
        context = this

        leerNroOperacion()


        btnLeerPalet.setOnClickListener {
            leerNroOperacion()
        }

    }




    private fun leerJaula() {


        try {
            dialogo_lee_codigo_jaula.dismiss()
        } catch (e: Exception) {
        }
        dialogo_lee_codigo_jaula = Dialog(this)
        dialogo_lee_codigo_jaula.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_lee_codigo_jaula.setContentView(R.layout.lector_codigo_jaula_almacenamiento)


        dialogo_lee_codigo_jaula.tvTituloDireccion.text = "LEER JAULA"

        dialogo_lee_codigo_jaula.etCodigoBarra.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO) {

                //dialogo_lee_codigo.etDesvioFocus.requestFocus()
                validarJaula()
                //return true
            }
            true
        })


        dialogo_lee_codigo_jaula.etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n") > -1) {
                    //dialogo_lee_codigo.etDesvioFocus.requestFocus()
                    validarJaula()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        dialogo_lee_codigo_jaula.etCodigoBarra.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus){
                validarJaula()
            }
        }

        dialogo_lee_codigo_jaula.btnAceptar.setOnClickListener {

            if (MainActivity.usuarioLogin.codUsuario == "INV" ) {
                dialogo_lee_codigo_jaula.dismiss();
            }else{
                finish();
            }

        }

        dialogo_lee_codigo_jaula.btn_volver.setOnClickListener {
            finish();
        }

        dialogo_lee_codigo_jaula.setCanceledOnTouchOutside(false)
        dialogo_lee_codigo_jaula.show()


    }





    fun validarJaula() {
        var mensaje = "CODIGO DE JAULA NO CORRESPONDE!"

        dialogo_lee_codigo_jaula.etCodigoBarra.setText(dialogo_lee_codigo_jaula.etCodigoBarra.text.toString().replace("\n", ""))


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", dialogo_lee_codigo_jaula.etCodigoBarra.text.toString())
            .build()


        var result = HttpRequest.call("", "entrada/valida_jaula_direccionamiento", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (mensaje2) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val existeArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

            if (existeArray.length() > 0) {

                //existe jaula y ahora leer nro de operacion
                codDeposito = dialogo_lee_codigo_jaula.etCodigoBarra.text.toString()

                dialogo_lee_codigo_jaula.dismiss();

                leerNroOperacion()

            } else {


                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()

            }


        } else {

            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()

        }

    }



    private fun leerNroOperacion() {


        try {
            dialogo_lee_codigo_palet.dismiss()
        } catch (e: Exception) {
        }
        dialogo_lee_codigo_palet = Dialog(this)
        dialogo_lee_codigo_palet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_lee_codigo_palet.setContentView(R.layout.lector_codigo_jaula_almacenamiento)




        dialogo_lee_codigo_palet.tvTituloDireccion.text = "LEER PALET"

        dialogo_lee_codigo_palet.etCodigoBarra.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        dialogo_lee_codigo_palet.etCodigoBarra.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_NULL) {

                //dialogo_lee_codigo.etDesvioFocus.requestFocus()
                validarNroOperacion()
                //return true
            }
            true
        })


        dialogo_lee_codigo_palet.etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n") > -1) {
                    //dialogo_lee_codigo.etDesvioFocus.requestFocus()
                    validarNroOperacion()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        dialogo_lee_codigo_palet.etCodigoBarra.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus){
                validarNroOperacion()
            }
        }

        dialogo_lee_codigo_palet.btnAceptar.setOnClickListener {

            if (MainActivity.usuarioLogin.codUsuario == "INV" ) {
                dialogo_lee_codigo_palet.dismiss();
            }else{
                finish();
            }

        }

        dialogo_lee_codigo_palet.btn_volver.setOnClickListener {
            finish();
        }

        dialogo_lee_codigo_palet.setCanceledOnTouchOutside(false)
        dialogo_lee_codigo_palet.show()


    }



    fun validarNroOperacion() {
        var mensaje = "CODIGO DE OPERACION NO CORRESPONDE!"

        dialogo_lee_codigo_palet.etCodigoBarra.setText(dialogo_lee_codigo_palet.etCodigoBarra.text.toString().replace("\n", ""))


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", codDeposito)
            .add("NRO_OPERACION", dialogo_lee_codigo_palet.etCodigoBarra.text.toString())
            .build()


        var result = HttpRequest.call("", "entrada/valida_operacion_direccionamiento", formBody)

        dirDestinoPropuesto = ""

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (mensaje2) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val existeArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

            if (existeArray.length() > 0) {

                dirDestinoPropuesto = (existeArray.get(0) as JSONObject).get("COD_DIRECCION").toString()


                //existe jaula y ahora leer nro de operacion
                nroOperacion = dialogo_lee_codigo_palet.etCodigoBarra.text.toString()

                dialogo_lee_codigo_palet.dismiss();

                leerDireccion()
                //Toast.makeText(context, "existe nro de operacion", Toast.LENGTH_LONG).show()

            } else {

                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()

            }



        } else {

            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()


        }

    }





    private fun leerDireccion() {


        try {
            dialogo_lee_codigo_direccion.dismiss()
        } catch (e: Exception) {
        }
        dialogo_lee_codigo_direccion = Dialog(this)
        dialogo_lee_codigo_direccion.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_lee_codigo_direccion.setContentView(R.layout.lector_codigo_direccion_direccionamiento)




        dialogo_lee_codigo_direccion.tvTituloDireccion.text = "LEER DIRECCION"
        dialogo_lee_codigo_direccion.tvDirDestino.text = dirDestinoPropuesto


        dialogo_lee_codigo_direccion.etCodigoBarra.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        dialogo_lee_codigo_direccion.etCodigoBarra.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO) {

                //dialogo_lee_codigo.etDesvioFocus.requestFocus()
                validarDireccion()
                //return true
            }
            true
        })


        dialogo_lee_codigo_direccion.etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n") > -1) {
                    //dialogo_lee_codigo.etDesvioFocus.requestFocus()
                    validarDireccion()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        dialogo_lee_codigo_direccion.etCodigoBarra.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus){
                validarDireccion()
            }
        }

        dialogo_lee_codigo_direccion.btnAceptar.setOnClickListener {

            dialogo_lee_codigo_direccion.dismiss()

        }

        dialogo_lee_codigo_direccion.btn_volver.setOnClickListener {
            dialogo_lee_codigo_direccion.dismiss();
        }

        dialogo_lee_codigo_direccion.setCanceledOnTouchOutside(false)
        dialogo_lee_codigo_direccion.show()



    }





    fun validarDireccion() {
        var mensaje = "DIRECCION DESTINO NO CORRESPONDE!"

        dialogo_lee_codigo_direccion.etCodigoBarra.setText(dialogo_lee_codigo_direccion.etCodigoBarra.text.toString().replace("\n", ""))

        if (!dirDestinoPropuesto.equals(dialogo_lee_codigo_direccion.etCodigoBarra.text.toString())) {


            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¡Atención!")
                .setContentText("La direccion destino no coincide con la direccion escaneada")
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->

                    sDialog.dismissWithAnimation()
                    dialogo_lee_codigo_direccion.etCodigoBarra.requestFocus()
                    dialogo_lee_codigo_direccion.etCodigoBarra.setText("")

                }
                .show()
            return
        }


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", codDeposito)
            .add("NRO_OPERACION", nroOperacion)
            .add("COD_DIRECCION", dialogo_lee_codigo_direccion.etCodigoBarra.text.toString())
            .build()


        var result = HttpRequest.call("", "entrada/valida_direccion_direccionamiento", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (mensaje2) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val existeArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

            if (existeArray.length() > 0) {

                //existe jaula y ahora leer nro de operacion
                dirDestino = dialogo_lee_codigo_direccion.etCodigoBarra.text.toString()

                dialogo_lee_codigo_direccion.dismiss();
                asignarDireccionamiento()

            } else {

                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()

            }



        } else {

            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()


        }

    }




    fun asignarDireccionamiento() {
        var mensaje = "DIRECCION NO CORRESPONDE!"

        dialogo_lee_codigo_direccion.etCodigoBarra.setText(dialogo_lee_codigo_direccion.etCodigoBarra.text.toString().replace("\n", ""))


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", codDeposito)
            .add("NRO_OPERACION", nroOperacion)
            .add("COD_DIRECCION", dialogo_lee_codigo_direccion.etCodigoBarra.text.toString())
            .build()


        var result = HttpRequest.call("", "entrada/procesa_entrada_direccion2", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (mensaje2) Error ${e.message.toString()} !")

            return
        }

        dialogo_lee_codigo_direccion.dismiss();

        if (respuestaJson.has("respuesta")) {
            val respuesta = respuestaJson.get("respuesta").toString()


            Toast.makeText(context, respuesta, Toast.LENGTH_LONG).show()


        } else {



            Toast.makeText(context, result, Toast.LENGTH_LONG).show()

        }










    }







}