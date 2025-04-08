package com.apolo.wms.operaciones.consulta

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.consulta.stock.DepositoStock
import com.apolo.wms.clases.consulta.stock.DetalleArticuloStock
import com.apolo.wms.operaciones.consulta.adapter.AdapterDepositoStock
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.consulta_stock.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ConsultaStock : AppCompatActivity() {

    companion object {
        lateinit var context : Context

        var depositoStock = ArrayList<DepositoStock>()
        var posicionDeposito = 0


        var detalleArticuloStock = ArrayList<DetalleArticuloStock>()
        var posicionDetalleArticulo = 0


    }


    var opCancelar = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consulta_stock)

        inicializar()

    }

    fun inicializar() {

        context = this

        title = "Consulta Stock".uppercase(Locale.getDefault())



        btnCancelar.setOnClickListener { cancelar() }
        btnBuscarCampo.setOnClickListener { buscaDetalleArticuloReal() }


        /*codigo de barra de producto*/
        etDestino.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().indexOf("\n") > -1) {
                    etDesvioFocus.requestFocus()
                }

            }
        })
        etDestino.setOnFocusChangeListener { view, _ ->

            if (!view.hasFocus()) {
                buscaDetalleArticuloReal()
            }

        }

        if(recuperaUsuarioBloqueado(MainActivity.usuarioLogin.codUsuario)){
            finish()
        }


        if(inventarioEnProceso()){

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


            /*
            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¡Atención!")
                .setContentText("Mientras exista inventario en proceso \n no está permitido consulta de artículos")
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog ->

                    finish()
                    sDialog.dismissWithAnimation()

                }
                .show()
*/

        }



    }


    fun cancelar() {

        opCancelar = 1
        tvDescArticulo.text = ""
        etDestino.setText("")
        etDestino.requestFocus()
        cargaDeposito("")

    }


    fun buscaDetalleArticuloReal() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("CODIGO", etDestino.text.toString())
            .build()

        var result = HttpRequest.call("", "consulta_stock/busca_detalle_articulo", formBody)


        posicionDetalleArticulo = 0
        detalleArticuloStock = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscaDetalleArticuloReal) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filaObject : JSONObject = filaArray.get(i) as JSONObject
                val ds = DetalleArticuloStock()
                ds.codArticulo = filaObject.get("COD_ARTICULO").toString()
                ds.descArticulo = filaObject.get("DESCRIPCION").toString()
                ds.indManejaVto = filaObject.get("IND_MANEJA_VTO").toString()
                ds.unidad = filaObject.get("UNIDAD").toString()
                ds.codDireccion = filaObject.get("COD_DIRECCION").toString()
                ds.codDireccionCaja = filaObject.get("COD_DIRECCION_CAJA").toString()
                ds.artAdicional = filaObject.get("ART_ADICIONAL").toString()

                detalleArticuloStock.add(ds)

            }

            if (detalleArticuloStock.size > 0) {

                var ds = detalleArticuloStock[detalleArticuloStock.size - 1]

                tvDescArticulo.text = " ${ds.codArticulo} - ${ds.descArticulo}"

                if (ds.artAdicional == "S") {

                    SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("¡Atención!")
                        .setContentText("Verifique las partes del artículo. \nEl artículo seleccionado tiene más de un volumen.")
                        .setConfirmText("OK")
                        .setConfirmClickListener { sDialog ->

                            sDialog.dismissWithAnimation()

                        }
                        .show()

                }

                cargaDeposito(ds.codArticulo)

            }


            if (etDestino.text.toString().indexOf(" ") > -1) {
                etDestino.setText(etDestino.text.toString().replace(" ", ""))
                etDestino.setSelection(etDestino.text.toString().length)
            }


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (buscaDetalleArticuloReal)")


        }


    }


    fun cargaDeposito(codArticulo: String) {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", codArticulo)
            .build()

        var result = HttpRequest.call("", "consulta_stock/carga_deposito", formBody)


        posicionDeposito = 0
        depositoStock = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cargaDeposito) Error ${e.message.toString()} !")

            return
        }

        if (respuestaJson.has("rows")) {
            val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filaArray.length()) {
                val filaObject : JSONObject = filaArray.get(i) as JSONObject
                val ds = DepositoStock()
                ds.deposito = filaObject.get("DEPOSITO").toString()
                ds.referencia = filaObject.get("REFERENCIA").toString()
                ds.cantBasica = filaObject.get("CANT_BASICA").toString()

                depositoStock.add(ds)

            }


            val gridLayoutManager = GridLayoutManager(context, 1)

            rvStock.layoutManager = gridLayoutManager
            rvStock.itemAnimator = DefaultItemAnimator()
            rvStock.setHasFixedSize(true)


            // this creates a vertical layout Manager
            rvStock.layoutManager = LinearLayoutManager(context)

            // This loop will create 20 Views containing
            // the image with the count of view
            // This will pass the ArrayList to our Adapter
            val adapter = AdapterDepositoStock(
                context,
                depositoStock,
                R.layout.card_view_stock  )

            // Setting the Adapter with the recyclerview
            rvStock.adapter = adapter


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaDeposito)")


        }


    }


    fun inventarioEnProceso() : Boolean {


        var resu = false

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        var result = HttpRequest.call("", "consulta_stock/consulta_existe_inventario", formBody)

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

    }


    fun recuperaUsuarioBloqueado(usuario: String) : Boolean {

        var resu = false

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_CALLE", "001")
            .build()

        var result = HttpRequest.call("", "consulta_stock/valida_calle_bloqueada", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (recuperaUsuarioBloqueado) Error ${e.message.toString()} !")

            return false
        }

        if (respuestaJson.has("respuesta")) {


            val respuesta = respuestaJson.get("respuesta").toString()

            val result: List<String> = respuesta.split("\\|\\|")

            return if (result.size > 2) {
                val usuarios: List<String> = result[2].split(",")
                for (s in usuarios) {
                    if (s.trim().indexOf(usuario.trim()) > -1) {
                        return true
                    }
                }
                false
            } else {
                false
            }


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (recuperaUsuarioBloqueado)")
            resu =  false

        }



        return resu


    }



}