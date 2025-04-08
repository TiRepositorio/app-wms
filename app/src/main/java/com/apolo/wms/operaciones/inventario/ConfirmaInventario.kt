package com.apolo.wms.operaciones.inventario

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.Operaciones
import com.apolo.wms.clases.inventario.ConferidosInventario
import com.apolo.wms.clases.inventario.DetalleInventario2
import com.apolo.wms.clases.inventario.UnidadMedidaInventario
import com.apolo.wms.operaciones.inventario.adapter.AdapterConferidoInventario
import com.apolo.wms.operaciones.inventario.adapter.AdapterDetalleInventario2
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.entrada_redireccion_ub.*
import kotlinx.android.synthetic.main.inventario_conferencia.*
import kotlinx.android.synthetic.main.inventario_conferencia.btnCancelar
import kotlinx.android.synthetic.main.inventario_conferencia.btnConfirmar
import kotlinx.android.synthetic.main.inventario_conferencia.etCantidad
import kotlinx.android.synthetic.main.inventario_conferencia.etVencimiento
import kotlinx.android.synthetic.main.inventario_conferencia.spDirecciones
import kotlinx.android.synthetic.main.inventario_conferido.*
import kotlinx.android.synthetic.main.inventario_view_pager.*
import kotlinx.android.synthetic.main.list_detalle_planilla_inventario.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat


class ConfirmaInventario : AppCompatActivity() {

    companion object {
        lateinit var context : Context

        var _cod_direccion = ""
        var codDeposito = ""
        var tipComprobante = ""
        var serComprobante = ""
        var nroComprobante = ""

        var umInventario = ArrayList<UnidadMedidaInventario>()
        var posicionUM = 0
        var posicionUMBasico = 0

        var inventarioDetInventario = ArrayList<DetalleInventario2>()
        var posicionInventarioDet = 0

        var conferidoInventario = ArrayList<ConferidosInventario>()
        var posicionConferido = 0



        lateinit var rvInventarioDet : RecyclerView
        lateinit var rvConferidos : RecyclerView



        fun obtieneInventarioDet() {


            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_DEPOSITO", codDeposito)
                .add("TIP_COMPROBANTE", tipComprobante)
                .add("SER_COMPROBANTE", serComprobante)
                .add("NRO_COMPROBANTE", nroComprobante)
                .add("COD_DIRECCION", _cod_direccion)
                .build()

            var result = HttpRequest.call("", "inventario/consulta_inventario_det", formBody)


            posicionInventarioDet = 0
            inventarioDetInventario = ArrayList()

            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_inventario_det) Error ${e.message.toString()} !")
                return
            }

            if (respuestaJson.has("rows")) {
                val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

                for (i in 0 until filaArray.length()) {
                    val filaObject : JSONObject = filaArray.get(i) as JSONObject
                    val um = DetalleInventario2()
                    um.codArticulo01 = filaObject.get("COD_ARTICULO_01").toString()
                    um.descArticulo01 = filaObject.get("DESC_ARTICULO_01").toString()
                    um.codArticulo02 = filaObject.get("COD_ARTICULO_02").toString()
                    um.descArticulo02 = filaObject.get("DESC_ARTICULO_02").toString()
                    um.nroOrden = filaObject.get("NRO_ORDEN").toString()
                    um.codDireccion = filaObject.get("COD_DIRECCION").toString()

                    inventarioDetInventario.add(um)



                }



                val gridLayoutManager = GridLayoutManager(context, 1)

                rvInventarioDet.layoutManager = gridLayoutManager
                rvInventarioDet.itemAnimator = DefaultItemAnimator()
                rvInventarioDet.setHasFixedSize(true)


                // this creates a vertical layout Manager
                rvInventarioDet.layoutManager = LinearLayoutManager(context)

                // This loop will create 20 Views containing
                // the image with the count of view
                // This will pass the ArrayList to our Adapter
                val adapter = AdapterDetalleInventario2 (
                    context,
                    inventarioDetInventario,
                    R.layout.card_view_conferencia_inventario_018  )

                // Setting the Adapter with the recyclerview
                rvInventarioDet.adapter = adapter

            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargarUM)")


            }


        }



        fun obtieneArticulosConferidosPlanilla() {


            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_DEPOSITO", codDeposito)
                .add("TIP_COMPROBANTE", tipComprobante)
                .add("SER_COMPROBANTE", serComprobante)
                .add("NRO_COMPROBANTE", nroComprobante)
                .build()

            var result = HttpRequest.call("", "inventario/obtiene_articulos_conferidos_planilla", formBody)


            posicionConferido = 0
            conferidoInventario = ArrayList()

            var respuestaJson: JSONObject


            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_articulos_conferidos_planilla) Error ${e.message.toString()} !")
                return
            }

            if (respuestaJson.has("rows")) {
                val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

                for (i in 0 until filaArray.length()) {
                    val filaObject : JSONObject = filaArray.get(i) as JSONObject
                    val ci = ConferidosInventario()
                    ci.codArticulo01 = filaObject.get("COD_ARTICULO_01").toString()
                    ci.descArticulo01 = filaObject.get("DESC_ARTICULO_01").toString()
                    ci.referencia01 = filaObject.get("REFERENCIA_01").toString()
                    ci.cantidad01 = filaObject.get("CANTIDAD_01").toString()
                    ci.codArticulo02 = filaObject.get("COD_ARTICULO_02").toString()
                    ci.descArticulo02 = filaObject.get("DESC_ARTICULO_02").toString()
                    ci.referencia02 = filaObject.get("REFERENCIA_02").toString()
                    ci.cantidad02 = filaObject.get("CANTIDAD_02").toString()
                    ci.nroOrden = filaObject.get("NRO_ORDEN").toString()
                    ci.codDireccion = filaObject.get("COD_DIRECCION").toString()

                    conferidoInventario.add(ci)



                }



                val gridLayoutManager = GridLayoutManager(context, 1)

                rvConferidos.layoutManager = gridLayoutManager
                rvConferidos.itemAnimator = DefaultItemAnimator()
                rvConferidos.setHasFixedSize(true)


                // this creates a vertical layout Manager
                rvConferidos.layoutManager = LinearLayoutManager(context)

                // This loop will create 20 Views containing
                // the image with the count of view
                // This will pass the ArrayList to our Adapter
                val adapter = AdapterConferidoInventario (
                    context,
                    conferidoInventario,
                    R.layout.card_view_inventario_conferencia  )

                // Setting the Adapter with the recyclerview
                rvConferidos.adapter = adapter

            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargarUM)")


            }

        }


        fun procesoEliminaConferencia(item: ConferidosInventario) : Boolean {

            //no se tiene permisos a menos que sea INV ??
            return true

        }



    }

    var esPicking = false


    var art_adicional = "N"
    var esPesable = "N"


    //tab1
    var cod_articulo_2 = ""
    var ind_maneja_vto = "N"
    var nro_orden_actual = ""



    //tab2



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inventario_view_pager)

        inicializar()

    }


    fun inicializar() {

        context = this

        title = "CONFERENCIA INVENTARIO"

        Companion.rvInventarioDet = rvInventarioDet
        Companion.rvConferidos = rvConferidos

        esPicking = esPickingOri(_cod_direccion)

        tvNroInventario.text = "Nro.Inv.:${nroComprobante}"

        ibtnBuscarPlanilla.setOnClickListener {  }


        mostrarContenido(tabConferencia)

        FuncionesUtiles.limitarDecimales(MainActivity.maximoDecimales, etCantidad)


    }


    fun mostrarContenido(view: View) {
        tabConferencia.setBackgroundColor(Color.parseColor("#474747"))
        tabConferido.setBackgroundColor(Color.parseColor("#474747"))
        lTabConferencia.setBackgroundColor(Color.parseColor("#474747"))
        lTabConferidos.setBackgroundColor(Color.parseColor("#474747"))
        layoutInventarioConferencia.visibility = View.GONE
        layoutInventarioConferido.visibility = View.GONE
        view.setBackgroundColor(Color.parseColor("#116600"))
        if (view.id == tabConferencia.id){
            layoutInventarioConferencia.visibility = View.VISIBLE
            lTabConferencia.setBackgroundColor(Color.parseColor("#116600"))

            inicializarTab1()

        }
        if (view.id == tabConferido.id){

            layoutInventarioConferido.visibility = View.VISIBLE
            lTabConferidos.setBackgroundColor(Color.parseColor("#116600"))

            inicializarTab2()

        }


    }


    fun inicializarTab1() {
        etCodDireccion.setText(_cod_direccion)
        etCodDireccion.isEnabled = false

        if(Operaciones.version_android != "25"){
            etDesvioFocus1.visibility = View.GONE
        }

        etDesvioFocus2.visibility = View.GONE

        if(Operaciones.version_android != "25"){
            etCodDireccion.inputType = 0
        }

        /*codigo de barra de producto*/
        etCodDireccion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (layoutInventarioConferencia.visibility == View.VISIBLE) {
                    if (s.toString().indexOf("\n") > -1) {
                        if(Operaciones.version_android == "25"){
                            etDesvioFocus1.requestFocus()
                        }
                    }
                }
            }
        })
        etCodDireccion.setOnFocusChangeListener { view, _ ->

            if (layoutInventarioConferencia.visibility == View.VISIBLE) {

                if (!view.hasFocus()) {
                }

            }

        }

        etCodArticulo.requestFocus()
        etCodArticulo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n") > -1) {

                    etDesvioFocus2.requestFocus()

                }
            }
        })
        etCodArticulo.setOnFocusChangeListener { view, _ ->

            if (!view.hasFocus()) {
                if (layoutInventarioConferencia.visibility == View.VISIBLE) {

                    if(etCodArticulo.length() != 0){
                        buscaDetalleArticulo()

                        etDesvioFocus2.requestFocus()
                        try {
                            prueba(context, etDesvioFocus2)
                        } catch (e: Exception) {
                            val aa: String
                            aa = ""
                        }
                    }

                }
            }

        }

        etDesvioFocus2.setOnFocusChangeListener { view, _ ->
            if (view.hasFocus()) {
                if (layoutInventarioConferencia.visibility == View.VISIBLE) {
                    etCantidad.requestFocus()
                }
            }
        }

        etCantidad.setOnFocusChangeListener { view, _ ->
            if (!view.hasFocus()) {
                if (layoutInventarioConferencia.visibility == View.VISIBLE) {
                }
            }
        }


        btnConfirmar.setOnClickListener { confirmaInventario() }
        btnCancelar.setOnClickListener { cancelar() }


        etCodDireccion.setText(etCodDireccion.text.toString().replace("\n", ""))
        val _cod_direccion = etCodDireccion.text.toString().trim()
        obtieneInventarioDet()


    }

    fun cancelar() {


        nro_orden_actual = ""

        etCodArticulo.setText("")
        tvDescArticulo_2.text = ""
        tvDescripcionTitulo_2.text = ""
        etCantidad.setText("")
        etVencimiento.setText("")

        val dataAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item, ArrayList<String>()
        )
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        spDirecciones.adapter = dataAdapter

        obtieneInventarioDet()

    }




    private fun prueba(context: Context, _et: EditText) {
        val imm: InputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(_et.windowToken, 0)
    }


    fun inicializarTab2() {
        //TODO

        btnCerrarConferencia.setOnClickListener{  }

        obtieneArticulosConferidosPlanilla()

    }






    fun esPickingOri(codDireccion: String) : Boolean {

        var resu = false

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", codDireccion)
            .build()

        var result = HttpRequest.call("", "inventario/es_picking_ori", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (es_picking_ori) Error ${e.message.toString()} !")
            return false
        }


        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            if (filas.length() > 0) {
                val filaObject : JSONObject = filas.get(0) as JSONObject

                if (filaObject.get("ES_PICKING").toString() == "S") {

                    resu = true

                }

            }


        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (esPickingOri)")
        }



        return resu

    }



    fun buscaDetalleArticulo() {

        etCodArticulo.setText(etCodArticulo.text.toString().replace("\n", ""))
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", etCodArticulo.text.toString() ) //
            .build()


        var result = HttpRequest.call("", "inventario/busca_detalle_articulo", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (busca_detalle_articulo) Error ${e.message.toString()} !")
            return
        }

        tvDescArticulo_2.text = ""
        etCantidad.inputType = InputType.TYPE_CLASS_NUMBER
        var cod_unidad_rec = ""

        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas.get(i) as JSONObject

                cod_articulo_2 = filaObject.get("COD_ARTICULO").toString()
                tvDescArticulo_2.text = filaObject.get("DESCRIPCION").toString()
                ind_maneja_vto = filaObject.get("IND_MANEJA_VTO").toString()

                art_adicional = filaObject.get("ART_ADICIONAL").toString()
                esPesable = filaObject.get("ES_PESABLE").toString()

                cod_unidad_rec = filaObject.get("UNIDAD").toString()

                var dir = ""
                val dir_aux: String = filaObject.get("COD_DIRECCION").toString()
                if (dir_aux.isNotEmpty()) {
                    dir = dir_aux.substring(0, 3) + "-" + dir_aux.substring(
                        3,
                        6
                    ) + "-" + dir_aux.substring(6, 7) + "-" + dir_aux.subSequence(7, 9)
                }
                tvDescripcionTitulo_2.text = "Descripcion: (COD:${cod_articulo_2}) DIR: $dir"

            }

            if (esPesable == "S") {
                etCantidad.inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
            }

            if (art_adicional == "S") {

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("¡Atención!")
                    .setContentText("Verifique las partes del artículo. \nEl artículo seleccionado tiene más de un volumen.")
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        sDialog.dismissWithAnimation()

                    }
                    .show()

            }


            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_ARTICULO", cod_articulo_2)
                .build()

            var result = HttpRequest.call("", "inventario/consulta_unidad_medida", formBody)


            posicionUM = 0
            posicionUMBasico = 0
            umInventario = ArrayList()

            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_unidad_medida) Error ${e.message.toString()} !")
                return
            }

            if (respuestaJson.has("rows")) {
                val filaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

                for (i in 0 until filaArray.length()) {
                    val filaObject : JSONObject = filaArray.get(i) as JSONObject
                    val um = UnidadMedidaInventario()
                    um.codUnidadRel = filaObject.get("COD_UNIDAD_REL").toString()
                    um.referencia = filaObject.get("REFERENCIA").toString()
                    um.indBasico = filaObject.get("IND_BASICO").toString()
                    um.lastro = filaObject.get("LASTRO").toString()
                    um.capas = filaObject.get("CAPAS").toString()

                    umInventario.add(um)

                    if (tipComprobante == "REP") {
                        if (um.codUnidadRel == "01") {
                            posicionUMBasico = i
                        }
                    } else {
                        if (um.codUnidadRel == cod_unidad_rec) {
                            posicionUMBasico = i
                        }
                    }

                }


                val spinnerAdapter : ArrayAdapter<UnidadMedidaInventario> =
                    ArrayAdapter(context, R.layout.spinner_adapter, umInventario)
                spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                spDirecciones.adapter = spinnerAdapter


                spDirecciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                        posicionUM = position
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) { }
                }


                if (umInventario.size != 0) {
                    spDirecciones.setSelection(posicionUMBasico)
                }


                var c = 0

                if (etCodArticulo.text.toString().indexOf(" ") > -1) {
                    c = 1
                    etCodArticulo.setText(etCodArticulo.text.toString().replace(" ", ""))
                    etCodArticulo.setSelection(etCodArticulo.text.toString().length)
                }

                if (c == 1) {
                    if (cod_articulo_2 == "") {

                        MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ningun articulo con el codigo ingresado")

                    } else {

                        etCantidad.requestFocus()

                    }
                } else {
                    val a_validar = etCodDireccion.text.toString()
                    val piso = a_validar.substring(6, 7)

//					if (ind_maneja_vto.equals("S") && !isPicking()) {
                    if (ind_maneja_vto == "S" && ! esPicking) {
                        var _fec: String?
                        val fec2: String
                        _fec = obtiene_fecha_vencimiento()
                        fec2 = condensaFecha2(_fec)
                        _fec = fec2
                        etVencimiento.setText(_fec)
                        etVencimiento.isEnabled = true
                    } else {
                        etVencimiento.isEnabled = false
                        etVencimiento.setText(MainActivity.fec_vencimiento_defecto)
                    }
                }



                if (cod_articulo_2.trim() == "") {

                    SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Atencion")
                        .setContentText("ARTÍCULO NO LOCALIZADO!!")
                        .setConfirmText("Si")
                        .setConfirmClickListener { sDialog ->

                            sDialog.dismissWithAnimation()

                        }
                        .show()
                }


            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargarUM)")


            }




        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (esPickingOri)")
        }


    }

    fun obtiene_fecha_vencimiento() : String {

        var resu = ""

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", _cod_direccion)
            .add("COD_ARTICULO", cod_articulo_2)
            .build()

        var result = HttpRequest.call("", "inventario/obtener_fecha_vencimiento", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtener_fecha_vencimiento) Error ${e.message.toString()} !")
            return ""
        }

        if (respuestaJson.has("respuesta")) {

            val resp = respuestaJson.get("respuesta").toString()

            val respuesta: List<String> = resp.split("-")

            if (respuesta.size < 2) {

                MainActivity.funciones.mensajeError(context, "Atencion", "Fecha de vencimiento invalida. Articulo $cod_articulo_2. Direccion $_cod_direccion. ${resp}")

            } else {

                if (respuesta[1] == null) {
                    MainActivity.funciones.mensajeError(context, "Atencion", respuesta[2])
                } else {
                    resu = respuesta[1]
                }

            }



        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (obtiene_fecha_vencimiento)")

        }

        return resu

    }


    fun condensaFecha2(viejaFecha: String) : String {

        var nuevaFecha = viejaFecha
        val _fec = ""
        var _dd = ""
        var _mm = ""
        var _aa = ""

        val mSinBarra: List<String> = viejaFecha.split("/")
        val dimSinBarra = mSinBarra.size
        if (dimSinBarra > 1) {
            for (i in mSinBarra.indices) {
                if (i == 0) {
                    _dd = mSinBarra[i]
                } else if (i == 1) {
                    _mm = mSinBarra[i]
                } else {
                    _aa = mSinBarra[i]
                    if (_aa.length > 2) {
                        _aa = _aa.substring(2, 4)
                    }
                }
            }
            nuevaFecha = _dd + _mm + _aa
        }


        return nuevaFecha

    }


    fun confirmaInventario() {

        var id = inventarioDetInventario[posicionInventarioDet]

        nro_orden_actual = id.nroOrden

        var cod_direccion_valida = etCodDireccion.text.toString()
        if(cod_direccion_valida.length <= 8){
            MainActivity.funciones.mensajeError(context, "Atencion", "Ingresar Direccion")
            return
        }

        if(!validaArticuloPicking()){
            etCodArticulo.requestFocus()
            etCodArticulo.selectAll()
            return
        }

        if(!validadCantidad(cod_articulo_2)){
            MainActivity.funciones.mensajeError(context, "Atencion", "La cantidad asignada debe ser mayor a 0")
            etCantidad.requestFocus()
            return
        }

        if (nro_orden_actual == "") {
            MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ninguna direccion seleccionada")
            etCodDireccion.requestFocus()
            return
        }

        if (cod_articulo_2 == "") {
            MainActivity.funciones.mensajeError(context, "Atencion", "No se encontro ningun articulo con el codigo ingresado")
            tvDescArticulo_2.text = ""
            return
        }

        if (umInventario == null) {
            MainActivity.funciones.mensajeError(context, "Atencion", "No existe ninguna unidad de medida para este articulo")
            return
        }

        if (umInventario.size == 0) {
            MainActivity.funciones.mensajeError(context, "Atencion", "No existe ninguna unidad de medida para este articulo")
            return
        }


        var cantidadConferencia = 0.0
        try {
            //cantidadConferencia = Integer.parseInt(etCantidad.text.toString())
            cantidadConferencia = etCantidad.text.toString().toDouble()
        } catch (e: Exception) {

        }
        var f_cantidadConferencia = cantidadConferencia

        var fec = etVencimiento.text.toString()

        if(fec.length == 6){

            try {
                var dfDate =  SimpleDateFormat("dd/MM/yyyy")
                dfDate.isLenient = false
                fec = fec.substring(0,2)+"/"+fec.substring(2,4)+"/20"+fec.substring(4, 6)
                dfDate.parse(fec)
                etVencimiento.setText(fec)
            } catch (e: Exception) {
                var err = e.message.toString()//
                MainActivity.funciones.mensajeError(context, "Atencion", "FECHA INGRESADA INCORRECTA")
                etVencimiento.requestFocus()
                return
            }


        } else {

            if(ind_maneja_vto == "S" && !isPicking()){

                MainActivity.funciones.mensajeError(context, "Atencion", "FECHA INGRESADA INCORRECTA")
                etVencimiento.requestFocus()
                return
            }

        }

        procesoInsertaConferencia(cantidadConferencia, fec)


    }


    fun validadCantidad(codArticulo: String) : Boolean {

        var resultado = false
        if (codArticulo == "999") {
            resultado = true
        } else {
            var cCantidad = etCantidad.text.toString()
            if (cCantidad == null ||
                cCantidad.trim().isEmpty()
            ) {
                cCantidad = "0"
            }
            val nCantidad = cCantidad.toDouble()
            resultado = nCantidad > 0
        }
        return resultado

    }


    fun procesoInsertaConferencia(cantidadConferencia: Double, fec: String) {

        var di = inventarioDetInventario[posicionInventarioDet]
        nro_orden_actual = di.nroOrden


        var _ins = false

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", codDeposito)
            .add("TIP_COMPROBANTE", tipComprobante)
            .add("SER_COMPROBANTE", serComprobante)
            .add("NRO_COMPROBANTE", nroComprobante)
            .add("COD_ARTICULO", cod_articulo_2)
            .add("COD_UNIDAD_MEDIDA", umInventario[posicionUM].codUnidadRel)
            .add("CANTIDAD", cantidadConferencia.toString().replace(".", ","))
            .add("FEC_VENCIMIENTO", fec)
            .add("NRO_ORDEN", nro_orden_actual)
            .build()

        var result = HttpRequest.call("", "inventario/inserta_conferencia_inventario", formBody)


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (inserta_conferencia_inventario) Error ${e.message.toString()} !")
            return
        }


        if (respuestaJson.has("respuesta")) {

            val respuesta = respuestaJson.get("respuesta").toString()

            if (respuesta.indexOf("EXITO") > -1) {
                DetallePlanillaInventario.regenerarDet = true;
                //tab2.obtiene_articulos_conferidos_planilla(); //COMENTAR PARA VERSION DE INVENTARIO
                obtieneArticulosConferidosPlanilla()

                cancelar();
                obtieneInventarioDet();

                _ins = true
            }

            if (!_ins) {

                val _men = "No se ha podido insertar el registro. \n Verifique su conexión"
                MainActivity.funciones.mensajeError(context, "Atencion", _men)


            }



        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (procesoInsertaConferencia)")

        }

    }





    fun isPicking() : Boolean {

        return try {
            val a_validar = etCodDireccion.text.toString()
            val piso = a_validar.substring(6, 7)
            piso == "0"
        } catch (e: java.lang.Exception) {
            false
        }

    }




    fun validaArticuloPicking() : Boolean {

        var cod_direccion_valida = etCodDireccion.text.toString()
        var piso = cod_direccion_valida.substring(6, 7)
        var calle = cod_direccion_valida.substring(0, 3)

        if(esPicking(cod_direccion_valida) && !calle.equals("GRA")){

            var cod_articulo_ingresado = etCodArticulo.text.toString()
            var cod_articulo_valida = recuperaArticuloPicking(cod_direccion_valida)

            if (MainActivity.usuarioLogin.codEmpresa == "2") {
                cod_articulo_valida = recuperaArticuloPicking(cod_direccion_valida,cod_articulo_ingresado)
            }

            if(cod_articulo_valida != cod_articulo_2){
                MainActivity.funciones.mensajeError(context, "Atencion", "Articulo no Corresponde a Direccion")
                return false
            }

        }

        return true

    }


    fun recuperaArticuloPicking(codDireccion: String) : String {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", codDireccion)
            .build()

        var result = HttpRequest.call("", "inventario/recupera_articulo_picking", formBody)

        var codigo = ""

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (recupera_articulo_picking) Error ${e.message.toString()} !")
            return ""
        }

        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas.get(i) as JSONObject

                codigo = filaObject.get("COD_ARTICULO").toString()

            }
        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (recuperaArticuloPicking)")
        }

        return codigo

    }

    fun recuperaArticuloPicking(codDireccion: String, codArticulo: String) : String {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", codDireccion)
            .add("COD_ARTICULO", codArticulo)
            .build()

        var result = HttpRequest.call("", "inventario/recupera_articulo_picking_2", formBody)

        var codigo = ""

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (recupera_articulo_picking_2) Error ${e.message.toString()} !")
            return ""
        }

        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas.get(i) as JSONObject

                codigo = filaObject.get("COD_ARTICULO").toString()

            }
        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (recuperaArticuloPicking)")
        }

        return codigo

    }



    fun esPicking(codDireccion: String) : Boolean {


        var resu = false

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", MainActivity.usuarioLogin.codDepositoVerde)
            .add("COD_DIRECCION", codDireccion)
            .build()

        var result = HttpRequest.call("", "inventario/es_picking", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (es_picking) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            if (filas.length() > 0) {
                    resu = true
            }


        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (esPickingOri)")
        }



        return resu

    }


}