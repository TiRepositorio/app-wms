package com.apolo.wms.operaciones.inventario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.inventario.DetalleInventario
import com.apolo.wms.operaciones.inventario.adapter.AdapterDetalleInventario
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.list_detalle_planilla_inventario.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject


class DetallePlanillaInventario : AppCompatActivity() {


    companion object {
        lateinit var context : Context

        var detallePlanillaInventario = ArrayList<DetalleInventario>()
        var posicionDetalle = 0


        var _cod_direccion = ""

        var regenerarDet = false


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_detalle_planilla_inventario)

        inicializar()

    }


    fun inicializar() {

        context = this

        title = "DETALLE INVENTARIO"

        posicionDetalle = -1
        regenerarDet = false


        /*codigo de barra de producto*/
        etCodigoBarraLDPI.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().indexOf("\n") > -1) {
                    etDesvioFocusLDPI.requestFocus()
                }

            }
        })
        etCodigoBarraLDPI.setOnFocusChangeListener { view, _ ->


            if (!view.hasFocus()) {

                if(detallePlanillaInventario.size == 0){
                    return@setOnFocusChangeListener
                }

                var _direccion = etCodigoBarraLDPI.text.toString().trim()
                var nreg = _direccion.length
                if(nreg == 0){
                    return@setOnFocusChangeListener
                }

                if(validaCodDireccion()){
                    try {
                        buscaDetalle()
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    }
                }else{
                    var _mensaje = "No existe Dirección en la planilla"
                    MainActivity.funciones.mensajeError(context, "Atencion", _mensaje)
                }


            }


        }


        btnCancelarLDPI.setOnClickListener{ cancelar() }

        cargaListaInventario()


    }



    fun cancelar() {

        etCodigoBarraLDPI.setText("")
        etCodigoBarraLDPI.requestFocus()

    }


    fun cargaListaInventario() {

        posicionDetalle = 0
        detallePlanillaInventario = ArrayList()

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", BuscarPlanillaInventario.planillaInventarioSeleccionada.tipComprobante)
            .add("SER_COMPROBANTE", BuscarPlanillaInventario.planillaInventarioSeleccionada.serComprobante)
            .add("NRO_COMPROBANTE", BuscarPlanillaInventario.planillaInventarioSeleccionada.nroComprobante)
            .build()

        var result = HttpRequest.call("", "inventario/consulta_detalle_inventario", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_detalle_inventario) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas.get(i) as JSONObject
                val di = DetalleInventario()
                di.codEmpresa = filaObject.get("COD_EMPRESA").toString()
                di.tipComprobante = filaObject.get("TIP_COMPROBANTE").toString()
                di.serComprobante = filaObject.get("SER_COMPROBANTE").toString()
                di.nroComprobante = filaObject.get("NRO_COMPROBANTE").toString()
                di.codArticulo01 = filaObject.get("COD_ARTICULO_01").toString()
                di.codUnidad01 = filaObject.get("COD_UNIDAD_01").toString()
                di.codDeposito = filaObject.get("COD_DEPOSITO").toString()
                di.fecVencimiento01 = filaObject.get("FEC_VENCIMIENTO_01").toString()
                di.cantidad = filaObject.get("CANTIDAD").toString()
                di.descArticulo = filaObject.get("DES_ARTICULO").toString()
                di.referencia = filaObject.get("REFERENCIA").toString()
                di.codCalle = if (filaObject.get("COD_CALLE").toString() == "null") {
                    ""
                } else {
                    filaObject.get("COD_CALLE").toString()
                }
                di.codPredio = if (filaObject.get("COD_PREDIO").toString() == "null") {
                    ""
                } else {
                    filaObject.get("COD_PREDIO").toString()
                }
                di.codPiso = if (filaObject.get("COD_PISO").toString() == "null") {
                    ""
                } else {
                    filaObject.get("COD_PISO").toString()
                }
                di.codApartamento = if (filaObject.get("COD_APARTAMENTO").toString() == "null") {
                    ""
                } else {
                    filaObject.get("COD_APARTAMENTO").toString()
                }
                di.codDireccion = if (filaObject.get("COD_DIRECCION").toString() == "null") {
                    ""
                } else {
                    filaObject.get("COD_DIRECCION").toString()
                }
                di.nroOrden = filaObject.get("NRO_ORDEN").toString()

                detallePlanillaInventario.add(di)

            }



            val gridLayoutManager = GridLayoutManager(context, 1)

            rvTransferenciasLDPI.layoutManager = gridLayoutManager
            rvTransferenciasLDPI.itemAnimator = DefaultItemAnimator()
            rvTransferenciasLDPI.setHasFixedSize(true)


            // this creates a vertical layout Manager
            rvTransferenciasLDPI.layoutManager = LinearLayoutManager(context)

            // This loop will create 20 Views containing
            // the image with the count of view
            // This will pass the ArrayList to our Adapter
            val adapter = AdapterDetalleInventario (
                context,
                detallePlanillaInventario,
                R.layout.card_view_detalle_planilla_inventario  )

            // Setting the Adapter with the recyclerview
            rvTransferenciasLDPI.adapter = adapter


            etCodigoBarraLDPI.requestFocus()


        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaListaInventario)")
        }

    }


    fun validaCodDireccion() : Boolean {

        var resu = false

        val _direccion: String = etCodigoBarraLDPI.text.toString().trim()


        detallePlanillaInventario.forEach {

            if(it.codDireccion.trim() == _direccion){
                resu = true
            }

        }



        return resu

    }


    fun buscaDetalle() {



        if (detallePlanillaInventario.size > 0) {

            _cod_direccion = etCodigoBarraLDPI.text.toString().trim()
            context.startActivity(Intent(context, ConfirmaInventario::class.java))

            ConfirmaInventario._cod_direccion = _cod_direccion
            ConfirmaInventario.codDeposito = detallePlanillaInventario[0].codDeposito
            ConfirmaInventario.tipComprobante = detallePlanillaInventario[0].tipComprobante
            ConfirmaInventario.serComprobante = detallePlanillaInventario[0].serComprobante
            ConfirmaInventario.nroComprobante = detallePlanillaInventario[0].nroComprobante
        }


    }


    override fun onResume() {

        super.onResume()
        if (regenerarDet) {
            try {
                cargaListaInventario()
                regenerarDet = false
            } catch (e: Exception) {

                MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
            }
        }
        etCodigoBarraLDPI.setText("")
        etCodigoBarraLDPI.requestFocus()
        BuscarPlanillaInventario.regenerarCab = true
    }


}