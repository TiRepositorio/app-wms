package com.apolo.wms.operaciones.consulta

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.consulta.articulo.DetalleArticulo
import com.apolo.wms.clases.consulta.articulo.UnidadMedidaArticulo
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.consulta_articulo_det.*
import kotlinx.android.synthetic.main.consulta_articulo_det.btnCancelar
import kotlinx.android.synthetic.main.consulta_articulo_det.etCantidad
import kotlinx.android.synthetic.main.consulta_articulo_det.etDestino
import kotlinx.android.synthetic.main.consulta_articulo_det.etDesvioFocus
import kotlinx.android.synthetic.main.consulta_articulo_det.etLote
import kotlinx.android.synthetic.main.consulta_articulo_det.etVencimiento
import kotlinx.android.synthetic.main.consulta_articulo_det.spAnomalias
import kotlinx.android.synthetic.main.consulta_articulo_det.spDirecciones
import kotlinx.android.synthetic.main.consulta_articulo_det.tvDescArticulo
import kotlinx.android.synthetic.main.consulta_articulo_det.tvDireccion
import kotlinx.android.synthetic.main.consulta_articulo_det.tvDireccion2
import kotlinx.android.synthetic.main.consulta_articulo_det.tvNorma
import kotlinx.android.synthetic.main.consulta_direccion.*
import kotlinx.android.synthetic.main.lector_codigo_jaula_almacenamiento.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ConsultaArticulo : AppCompatActivity() {

    companion object {
        lateinit var context : Context

        var detalleArticulo = ArrayList<DetalleArticulo>()

        var umArticulo = ArrayList<UnidadMedidaArticulo>()
        var posicionUM = 0
        var posicionUMBasico = 0


        var insertarEnter = 0


    }


    var opCancelar = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consulta_articulo_det)

        inicializar()

    }

    fun inicializar() {

        context = this

        title = "Consulta dirección por artículo".uppercase(Locale.getDefault())

        etLote.visibility = View.GONE
        etVencimiento.visibility = View.GONE
        etCantidad.visibility = View.GONE
        spAnomalias.visibility = View.GONE


        btnCancelar.setOnClickListener { cancelar() }


        etDestino.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO) {
                buscaDetalleArticulo()
            }
            true
        })

        /*codigo de barra de producto*/
        etDestino.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().length >= 13 && insertarEnter == 0 && s.toString().indexOf("\n") <= -1) {
                    insertarEnter = 1
                }

                if (s.toString().indexOf("\n") > -1 || insertarEnter == 1) {
                    insertarEnter = -1
                    etDestino.setText(s.toString().replace("\n",""))
                    buscaDetalleArticulo()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(etDesvioFocus.windowToken, 0)
                    insertarEnter == 0
                    //etDesvioFocus.requestFocus()
                }

            }
        })
        etDestino.setOnFocusChangeListener { view, _ ->


            if (!view.hasFocus()) {

                if(etDestino.text.toString().trim().isNotEmpty()){
                    buscaDetalleArticulo()
                }

            }


        }

        etDesvioFocus.setOnFocusChangeListener { view, _ ->

            if (view.hasFocus()) {

                etDestino.requestFocus()

            }

        }

        etDestino.requestFocus()

    }


    fun cancelar() {

        opCancelar = 1
        tvCodArticulo.text = ""
        tvDescArticulo.text = ""
        tvNorma.text = ""
        etDestino.setText("")
        etCantidad.setText("")
        etVencimiento.setText("")
        etLote.setText("")
        etDestino.requestFocus()
        tvDireccion.text = ""
        tvDireccion2.text = ""
        spDirecciones.adapter = null

    }


    fun buscaDetalleArticulo() {


        detalleArticulo = ArrayList()

        etDestino.setText(etDestino.text.toString().replace("\n", ""))

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_BARRA", etDestino.text.toString())
            .build()

        var result = HttpRequest.call("", "consulta_articulo/busca_detalle_articulo", formBody)

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

                val da = DetalleArticulo()
                da.codArticulo = filaObject.get("COD_ARTICULO").toString()
                da.descArticulo = filaObject.get("DESCRIPCION").toString()
                da.indManejaVto = filaObject.get("IND_MANEJA_VTO").toString()
                da.unidad = filaObject.get("UNIDAD").toString()
                da.codDeposito = filaObject.get("COD_DEPOSITO").toString()
                da.codDireccion = filaObject.get("COD_DIRECCION").toString()
                da.codDireccionCaja = filaObject.get("COD_DIRECCION_CAJA").toString()

                detalleArticulo.add(da)

            }

            tvDescArticulo.text = ""

            if (detalleArticulo.size > 0) {

                var da = detalleArticulo[detalleArticulo.size - 1]

                etDestino.setText("")
                tvCodArticulo.text = da.codArticulo
                tvDescArticulo.text = da.descArticulo

                var dir_aux = da.codDireccion

                if(dir_aux.isNotEmpty()){
                    dir_aux = dir_aux.substring(0, 3)+"-"+dir_aux.substring(3, 6)+"-"+dir_aux.substring(6, 7)+"-"+dir_aux.subSequence(7, 9)
                }

                tvDireccion.text = "(COD:${da.codArticulo}) DIR(UND): ${dir_aux}"


                dir_aux = da.codDireccionCaja

                if(dir_aux.isNotEmpty()){
                    dir_aux = dir_aux.substring(0, 3)+"-"+dir_aux.substring(3, 6)+"-"+dir_aux.substring(6, 7)+"-"+dir_aux.subSequence(7, 9)
                }
                tvDireccion2.text = "                      DIR(CAJA): ${dir_aux}"


                cargarUM()

            } else {

                cancelar()
                MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ningun articulo con el codigo ingresado")

            }


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (buscaDetalleArticulo)")

        }

    }


    fun cargarUM() {

        var da = detalleArticulo[detalleArticulo.size - 1]


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", da.codArticulo)
            .build()

        var result = HttpRequest.call("", "consulta_articulo/consulta_unidad_medida", formBody)


        posicionUM = 0
        posicionUMBasico = 0
        umArticulo = ArrayList()

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
                val um = UnidadMedidaArticulo()
                um.codUnidadRel = filaObject.get("COD_UNIDAD_REL").toString()
                um.referencia = filaObject.get("REFERENCIA").toString()
                um.indBasico = filaObject.get("IND_BASICO").toString()
                um.lastro = filaObject.get("LASTRO").toString()
                um.capas = filaObject.get("CAPAS").toString()

                umArticulo.add(um)


                if (um.indBasico == "S") {
                    posicionUMBasico = i
                }

            }



            val spinnerAdapter : ArrayAdapter<UnidadMedidaArticulo> =
                ArrayAdapter(context, R.layout.spinner_adapter, umArticulo)
            spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            spDirecciones.adapter = spinnerAdapter


            spDirecciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    posicionUM = position

                    var um = umArticulo[posicionUM]

                    if(um.lastro !== ""){
                        tvNorma.text = "NORMA  LASTRO:${um.lastro} X ALTO:${um.capas}"
                    }else{
                        tvNorma.text = " - "
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }

            if (umArticulo.size != 0) {
                spDirecciones.setSelection(posicionUMBasico)
            }


            var c = 0
            if (etDestino.text.toString().indexOf(" ") > -1) {
                c = 1
                etDestino.setText(etDestino.text.toString().replace(" ", ""))
                etDestino.setSelection(etDestino.text.toString().length)
            }

            if (c == 1) {
                if (da.codArticulo == "") {
                    MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ningun articulo con el codigo ingresado")
                } else {
                    spDirecciones.requestFocus()
                    etLote.isEnabled = da.indManejaVto.equals("S")
                }

            } else {
                etLote.setText("")
                etVencimiento.setText("")
                if (da.indManejaVto == "S") {
                    etLote.isEnabled = true
                    etVencimiento.isEnabled = true
                } else {
                    etLote.isEnabled = false
                    etVencimiento.isEnabled = false
                    etLote.setText(MainActivity.lote_defecto)
                    etVencimiento.setText(MainActivity.fec_vencimiento_defecto)
                }

            }


            if(tvDescArticulo.text.toString().trim() == "" && opCancelar == 0){

                SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("Atencion")
                    .setContentText("ARTÍCULO NO LOCALIZADO!!")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        etDestino.requestFocus()
                        sDialog.dismissWithAnimation()

                    }
                    .show()

            }else{
                opCancelar = 0
            }


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargarUM)")


        }

    }



}