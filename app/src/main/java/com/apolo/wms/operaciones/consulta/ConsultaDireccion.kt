package com.apolo.wms.operaciones.consulta

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.consulta.articulo.DetalleArticulo
import com.apolo.wms.clases.consulta.articulo.UnidadMedidaArticulo
import com.apolo.wms.clases.consulta.direccion.DetalleDireccion
import com.apolo.wms.clases.consulta.direccion.UnidadMedidaDireccion
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.consulta_articulo_det.*
import kotlinx.android.synthetic.main.consulta_direccion.*
import kotlinx.android.synthetic.main.consulta_direccion.btnCancelar
import kotlinx.android.synthetic.main.consulta_direccion.etDestino
import kotlinx.android.synthetic.main.consulta_direccion.etDesvioFocus
import kotlinx.android.synthetic.main.consulta_direccion.tvDescArticulo
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ConsultaDireccion : AppCompatActivity() {


    companion object {
        lateinit var context : Context

        var detalleDireccion = ArrayList<DetalleDireccion>()

        var umDireccion = ArrayList<UnidadMedidaDireccion>()
        var posicionUM = 0
        var posicionUMBasico = 0

        var insertarEnter = 0

    }


    var opCancelar = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consulta_direccion)

        inicializar()

    }


    fun inicializar() {

        context = this

        title = "Consulta artículo por dirección".uppercase(Locale.getDefault())

        btnCancelar.setOnClickListener{ cancelar() }
        btnBuscarCampo.setOnClickListener { buscar() }


        /*codigo de barra de producto*/
        etDestino.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().length >= 9 && insertarEnter == 0 && s.toString().indexOf("\n") <= -1) {
                    insertarEnter = 1
                }

                if (s.toString().indexOf("\n") > -1 || insertarEnter == 1) {
                    insertarEnter = -1
                    etDestino.setText(s.toString().replace("\n",""))

                    if(etDestino.text.toString().trim().isNotEmpty()){
                        consultaDireccion()
                        etDesvioFocus.requestFocus()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(etDesvioFocus.windowToken, 0)

                    }

                    insertarEnter = 0

                    //etDesvioFocus.requestFocus()
                }

            }
        })
        etDestino.setOnFocusChangeListener { view, _ ->


            if (!view.hasFocus()) {

                if(etDestino.text.toString().trim().isNotEmpty()){
                    consultaDireccion()
                    etDesvioFocus.requestFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(etDesvioFocus.windowToken, 0)

                }

            }


        }

        etDesvioFocus.setOnFocusChangeListener { view, _ ->

            if (view.hasFocus()) {

                etDestino.requestFocus()

            }

        }

        if(inventarioEnProceso()){

            //MainActivity.funciones.mensajeError(context,
            //    "Atencion", "Mientras exista inventario en proceso \n no está permitido consulta de artículos")



            var a =
            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¡Atención!")
                .setContentText("Mientras exista inventario en proceso \n no está permitido consulta de artículos")
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->

                    finish()
                    sDialog.dismissWithAnimation()

                }

            a.setCancelable(false)
            a.setCanceledOnTouchOutside(false)
            a.show()

        }

        etDestino.requestFocus()

    }

    fun cancelar() {

        opCancelar = 1
        tvCodDireccion.text = ""
        tvDescArticulo.text = ""
        tvCantidadDisp.text = ""
        tvVencimiento.text = ""
        tvCapacidad.text = ""
        etDestino.setText("")
        etDestino.requestFocus()

    }


    fun buscar() {

        if(etDestino.text.toString().trim().isNotEmpty()){
            consultaDireccion()
        }

    }


    fun consultaDireccion() {


        detalleDireccion = ArrayList()

        etDestino.setText(etDestino.text.toString().replace("\n", ""))

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", etDestino.text.toString())
            .build()

        var result = HttpRequest.call("", "consulta_direccion/consulta_direccion", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consultaDireccion) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filaObject : JSONObject = filaArray.get(i) as JSONObject

                val dd = DetalleDireccion()
                dd.codDireccion = filaObject.get("COD_DIRECCION").toString()
                dd.codPiso = filaObject.get("COD_PISO").toString()
                dd.codArticulo = filaObject.get("COD_ARTICULO").toString()
                dd.descArticulo = filaObject.get("DESCRIPCION").toString()
                dd.cantidad = filaObject.get("CANTIDAD").toString()
                dd.cantResto = filaObject.get("CANT_RESTO").toString()
                dd.cantCaja = filaObject.get("CANT_CAJA").toString()
                dd.fecVencimiento = filaObject.get("FEC_VENCIMIENTO").toString()
                dd.capacidad = filaObject.get("CAPACIDAD").toString()

                detalleDireccion.add(dd)

            }

            if (detalleDireccion.size > 0) {

                var dd = detalleDireccion[detalleDireccion.size - 1]

                var dir_aux = dd.codDireccion
                if(dir_aux.isNotEmpty()){
                    dir_aux = dir_aux.substring(0, 3)+"-"+dir_aux.substring(3, 6)+"-"+dir_aux.substring(6, 7)+"-"+dir_aux.subSequence(7, 9)
                }

                tvCodDireccion.text = dir_aux
                etDestino.setText("")
                tvDescArticulo.text = "${dd.codArticulo}-${dd.descArticulo}"
                tvCantidadDisp.text = "CANTIDAD: ${dd.cantidad} (${dd.cantCaja})Ca, ${dd.cantResto}Uni)"

                tvVencimiento.text = "FEC. VENC.: ${dd.fecVencimiento}"
                tvCapacidad.text = "Capacidad: ${dd.capacidad}"


                cargarUM()

            } else {

                cancelar()
                MainActivity.funciones.mensajeError(context, "Atencion", "DIRECCION NO LOCALIZADA!!")


            }





        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (consultaDireccion)")

        }


    }


    fun cargarUM() {

        var da = detalleDireccion[detalleDireccion.size - 1]


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", da.codArticulo)
            .build()

        var result = HttpRequest.call("", "consulta_direccion/consulta_unidad_medida", formBody)


        posicionUM = 0
        posicionUMBasico = 0
        umDireccion = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cargarUM) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filaObject : JSONObject = filaArray.get(i) as JSONObject
                val um = UnidadMedidaDireccion()
                um.codUnidadRel = filaObject.get("COD_UNIDAD_REL").toString()
                um.referencia = filaObject.get("REFERENCIA").toString()
                um.mult = filaObject.get("MULT").toString()
                um.indBasico = filaObject.get("IND_BASICO").toString()
                um.lastro = filaObject.get("LASTRO").toString()
                um.capas = filaObject.get("CAPAS").toString()

                umDireccion.add(um)


            }

            var dd = detalleDireccion[detalleDireccion.size - 1]

            var unidad1 = ""
            var unidad2 = ""

            if (umDireccion.size > 0) {
                unidad1 = umDireccion[0].referencia.substring(0, 3)
            }

            if (umDireccion.size > 1) {
                unidad2 = umDireccion[1].referencia.substring(0, 3)
            }


            tvCantidadDisp.text = "CANTIDAD: ${dd.cantidad} (${dd.cantCaja}-${unidad2}, ${dd.cantResto}-${unidad1})"

            var c = 0
            if (etDestino.text.toString().indexOf(" ") > -1) {
                c = 1
                etDestino.setText(etDestino.text.toString().replace(" ", ""))
                etDestino.setSelection(etDestino.text.toString().length)
            }

            if(tvDescArticulo.text.toString().trim().isEmpty()){

                SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("Atencion")
                    .setContentText("DIRECCION NO LOCALIZADA!!")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        etDestino.requestFocus()
                        sDialog.dismissWithAnimation()

                    }
                    .show()

            } else {
                opCancelar = 0
            }

        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargarUM)")


        }

    }


    fun inventarioEnProceso() : Boolean {
/*

        var resu = false

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        var result = HttpRequest.call("", "consulta_direccion/consulta_existe_inventario", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (inventarioEnProceso) Error ${e.message.toString()} !")

            return false
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            if(filaArray.length() > 0){
                resu = true
            }


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (inventarioEnProceso)")
            resu =  false

        }

        return resu


 */

        return false
    }

}