package com.apolo.wms

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.clases.Empresa
import com.apolo.wms.clases.Sucursal
import com.apolo.wms.clases.UsuarioLogin
import com.apolo.wms.clases.separacion.SeparadorSeparacion
import com.apolo.wms.operaciones.almacenamiento.ConfirmaAlmacenamientoUb
import com.apolo.wms.operaciones.cerrarsesion.CerrarSesion
import com.apolo.wms.operaciones.consulta.*
import com.apolo.wms.operaciones.direccionamiento.DireccionamientoDirecciones
import com.apolo.wms.operaciones.entrada.BuscarPlanillaEntrada
import com.apolo.wms.operaciones.fraccionado.ConsultaJaula
import com.apolo.wms.operaciones.inventario.BuscarPlanillaInventario
import com.apolo.wms.operaciones.movimiento.MovimientoHorizontal
import com.apolo.wms.operaciones.movimiento.MovimientoVertical
import com.apolo.wms.operaciones.reabastecimiento.ReabastecimientoMain
import com.apolo.wms.operaciones.reabastecimiento.ReabastecimientoMarcaderiaDriveInNew
import com.apolo.wms.operaciones.reabastecimientocorrprev.ReabastecimientoCorrPrev
import com.apolo.wms.operaciones.separacion.BuscaGrupoSeparacion
import com.apolo.wms.operaciones.separacion.BuscarPlanillaSeparacion
import com.apolo.wms.operaciones.separacion.adapter.AdapterSeparadorSeparacion
import com.apolo.wms.operaciones.transferencia.TransferenciaDeposito
import com.apolo.wms.utilidades.*
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.activity_operaciones.*
import kotlinx.android.synthetic.main.lector_codigo_jaula_almacenamiento.*
import kotlinx.android.synthetic.main.lista_separadores.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KClass


class Operaciones : AppCompatActivity() {


    companion object {
        var empresas = ArrayList<Empresa>()
        var sucursales = ArrayList<Sucursal>()
        var codEmpresaTemp = ""
        lateinit var context : Context
        lateinit var tvEmpresa : TextView


        var alist_operaciones: List<HashMap<String, String>>? = null
        var permisosFijos: Array<String>? = null
        var operaciones: Array<String>? = null
        var imagenesFijas: Array<Drawable>? = null
        var imagenes: Array<Drawable>? = null
        var clasesFijas: Array<KClass<*>>? = null
        var clases: Array<KClass<*>>? = null

        var utilidadesBD: UtilidadesBD? = null
        var bdatos: SQLiteDatabase? = null
        var sentencias = Sentencias()

        var version_android = ""



        var separadorSeparacion = java.util.ArrayList<SeparadorSeparacion>()

        fun cargaSeparadores() {


            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("FILTRO", "")
                .build()

            var result = HttpRequest.call("", "separacion/busca_separador", formBody)

            separadorSeparacion = java.util.ArrayList()

            var respuestaJson = JSONObject(result)
            if (respuestaJson.has("rows")) {

                val separadorSeparacionArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


                for (i in 0 until separadorSeparacionArray.length()) {
                    val separadorSeparacionObject : JSONObject = separadorSeparacionArray.get(i) as JSONObject
                    val ss = SeparadorSeparacion()
                    ss.codSeparador = separadorSeparacionObject.get("COD_SEPARADOR").toString()
                    ss.nombre = separadorSeparacionObject.get("NOMBRE").toString()


                    separadorSeparacion.add(ss)

                }


            }  else {
                //MainActivity.funciones.mensajeError(BuscaGrupoSeparacion.context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaSeparadores)")
            }


        }

    }


    var sd: Adapter_lista_operaciones? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operaciones)

        inicializar()
        version_android = android.os.Build.VERSION.SDK_INT.toString()

    }


    fun inicializar() {

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_launcher5);

        title = "USUARIO: " + MainActivity.usuarioLogin.codUsuario
        context = this

        abreocreabd()

        btnCerrarSesion.setOnClickListener{ cerrarSesion() }
        Operaciones.tvEmpresa = tvEmpresa
        consultarDeposito()
        cargaListaOperaciones()
        cargaSeparadores()

    }

    fun abreocreabd() {
        try {
            utilidadesBD = UtilidadesBD(this, null)
            bdatos = utilidadesBD!!.writableDatabase
            //bdatos = openOrCreateDatabase("BdDatos", MODE_WORLD_READABLE, null)
            var _sql: String? = null

            //---------------------------------------------------//
            _sql = sentencias.createTableWms_configuraciones()
            try {
                bdatos!!.execSQL(_sql)
            } catch (e: java.lang.Exception) {
                MainActivity.funciones.mensajeError(context, "Fallo Base de Datos", "Error al crear la tabla wms_configuraciones!")
            }
            //---------------------------------------------------//
            _sql = sentencias.createTableWms_transferencias_manuales()
            try {
                bdatos!!.execSQL(_sql)
            } catch (e: java.lang.Exception) {
                MainActivity.funciones.mensajeError(context, "Fallo Base de Datos", "Error al crear la tabla wms_transferencias_manuales!")
            }
            //---------------------------------------------------//
            _sql = sentencias.createTableWms_conferencias_iniciadas()
            try {
                bdatos!!.execSQL(_sql)
            } catch (e: java.lang.Exception) {
                MainActivity.funciones.mensajeError(context, "Fallo Base de Datos", "Error al crear la tabla wms_conferencias_manuales!")
            }
            //---------------------------------------------------//
            _sql = sentencias.createTableWms_almacenamientos_iniciados()
            try {
                bdatos!!.execSQL(_sql)
            } catch (e: java.lang.Exception) {
                MainActivity.funciones.mensajeError(context, "Fallo Base de Datos", "Error al crear la tabla wms_amacenamientos_manuales!")
            }
            //---------------------------------------------------//
            _sql = sentencias.createTableWms_transferencias_dep()
            try {
                bdatos!!.execSQL(_sql)
            } catch (e: java.lang.Exception) {
                MainActivity.funciones.mensajeError(context, "Fallo Base de Datos", "Error al crear la tabla wms_transferencias_manuales!")
            }
            //---------------------------------------------------//
            _sql = sentencias.createTableWms_inventario_018()
            try {
                bdatos!!.execSQL(_sql)
            } catch (e: java.lang.Exception) {
                MainActivity.funciones.mensajeError(context, "Fallo Base de Datos", "Error al crear la tabla wms_transferencias_manuales! ")
            }
            //---------------------------------------------------//
            _sql = sentencias.createTableWms_Separadores()
            try {
                bdatos!!.execSQL(_sql)
            } catch (e: java.lang.Exception) {
                MainActivity.funciones.mensajeError(context, "Fallo Base de Datos", "Error al crear la tabla wms_separadores! ")
            }
            //---------------------------------------------------//
            _sql = "ALTER TABLE wms_transferencias_manuales ADD TIP_OPERACION_REABAST TEXT DEFAULT ''"
            try {
                bdatos!!.execSQL(_sql)
            } catch (e: java.lang.Exception) {
            }
            //---------------------------------------------------//
            //---------------------------------------------------//
            _sql = sentencias.createTableWmsUsuarioSucursal()
            try {
                bdatos!!.execSQL(_sql)
            } catch (e: java.lang.Exception) {
                MainActivity.funciones.mensajeError(context, "Fallo Base de Datos", "Error al crear la tabla wms_usuario_sucursal! ")
            }
        } catch (erro: java.lang.Exception) {

            MainActivity.funciones.mensajeError(context, "Fallo Base de Datos", erro.message.toString())
        }
    }


    override fun onBackPressed() {
        cerrarSesion()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_operaciones, menu)

        menu!!.findItem(R.id.m_cambiar_empresa).isVisible = MainActivity.usuarioLogin.cambiaSucursal == "S"
        menu!!.findItem(R.id.m_cambiar_sucursal).isVisible = MainActivity.usuarioLogin.cambiaSucursal == "S"

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.getItemId()) {
            R.id.m_cambiar_empresa -> {
                cambiarEmpresa()
                true
            }
            R.id.m_cambiar_sucursal -> {
                codEmpresaTemp = MainActivity.usuarioLogin.codEmpresa
                cambiarSucursal()
                true
            }
            R.id.m_cerrar_sesion -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun cambiarEmpresa() {

        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .build()

        executorRunner?.execute(
            CallableWS("operaciones/consulta_empresas", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    empresas = ArrayList()
                    var respuestaJson = JSONObject(result)
                    if (respuestaJson.has("rows")) {
                        val empresasArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
                        for (i in 0 until empresasArray.length()) {
                            val empresaObject : JSONObject = empresasArray.get(i) as JSONObject
                            val e = Empresa()
                            e.codEmpresa = empresaObject.get("COD_EMPRESA").toString()
                            e.descripcion = empresaObject.get("DESCRIPCION").toString()
                            empresas.add(e)
                        }
                    }

                    dialogoSeleccionarEmpresa(context)

                }
                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })

    }


    fun cambiarSucursal() {


        val executorRunner = ExecutorRunner()
        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", codEmpresaTemp)
            .build()

        executorRunner?.execute(
            CallableWS("operaciones/consulta_sucursales", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    sucursales = ArrayList()
                    var respuestaJson = JSONObject(result)

                    if (respuestaJson.has("rows")) {

                        val sucursalesArray : JSONArray = (respuestaJson.get("rows") as JSONArray)

                        for (i in 0 until sucursalesArray.length()) {

                            val sucursalObject : JSONObject = sucursalesArray.get(i) as JSONObject
                            val s = Sucursal()
                            s.codEmpresa = sucursalObject.get("COD_EMPRESA").toString()
                            s.codSucursal = sucursalObject.get("COD_SUCURSAL").toString()
                            s.descripcion = sucursalObject.get("DESCRIPCION").toString()

                            sucursales.add(s)
                        }
                    }

                    dialogoSeleccionarSucursal(context)

                }
                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })
    }

    fun cerrarSesion() {

        SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Desea Cerrar Sesion?")
            .setContentText("")
            .setConfirmText("Si")
            .setConfirmClickListener {

                val executorRunner = ExecutorRunner()

                var formBody: RequestBody = FormBody.Builder()
                    .add("USER", MainActivity.usuarioLogin.codUsuario)
                    .add("PASS", MainActivity.usuarioLogin.password)
                    .add("IMEI", MainActivity.idTelefono)
                    .add("IP", MainActivity.funciones.getIP())
                    .add("VERSION", "v.${MainActivity._version}.${MainActivity._fechaVersion}")
                    .build()


                executorRunner?.execute(
                    CallableWS("login/cerrar_sesion", formBody),
                    object : ExecutorRunner.Callback<String> {
                        override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                            MainActivity.usuarioLogin = UsuarioLogin()

                            finish()
                        }
                        override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                            if (e != null) {
                                MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                            } else {
                                MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                            }
                        }
                    })

            }
            .setCancelButton(
                "No"
            ) { sDialog -> sDialog.dismissWithAnimation() }
            .show()


    }



    fun consultarDeposito() {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()


        var result = HttpRequest.call("", "operaciones/consulta_depositos", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(DireccionamientoDirecciones.context, "Atencion", "Error en la respuesta del servidor. (mensaje2) Error ${e.message.toString()} ! ${result}")

            return
        }

        if (respuestaJson.has("rows")) {
            val sucursalesArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until sucursalesArray.length()) {
                val sucursalObject : JSONObject = sucursalesArray.get(i) as JSONObject
                MainActivity.usuarioLogin.codDepositoVerde = sucursalObject.get("COD_DEPOSITO_VERDE").toString()
                MainActivity.usuarioLogin.codDepositoAmarillo = sucursalObject.get("COD_DEPOSITO_AMARILLO").toString()
                MainActivity.usuarioLogin.codDepositoRojo = sucursalObject.get("COD_DEPOSITO_ROJO").toString()
                MainActivity.usuarioLogin.descEmpresa = sucursalObject.get("DESC_EMPRESA").toString()
                MainActivity.usuarioLogin.descSucursal = sucursalObject.get("DESC_SUCURSAL").toString()

                tvEmpresa.text = "${MainActivity.usuarioLogin.codEmpresa} - " +
                        "${MainActivity.usuarioLogin.descEmpresa} \n " +
                        "${MainActivity.usuarioLogin.codSucursal} - " +
                        "${MainActivity.usuarioLogin.descSucursal} "

            }

        } else {

            Toast.makeText(DireccionamientoDirecciones.context, result, Toast.LENGTH_LONG).show()


        }

    }


    fun consultarDeposito2() {

        val executorRunner = ExecutorRunner()

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        executorRunner?.execute(
            CallableWS("operaciones/consulta_depositos", formBody),
            object : ExecutorRunner.Callback<String> {
                override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                    try {
                        var respuestaJson = JSONObject(result)
                        if (respuestaJson.has("rows")) {
                            val sucursalesArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
                            for (i in 0 until sucursalesArray.length()) {
                                val sucursalObject : JSONObject = sucursalesArray.get(i) as JSONObject
                                MainActivity.usuarioLogin.codDepositoVerde = sucursalObject.get("COD_DEPOSITO_VERDE").toString()
                                MainActivity.usuarioLogin.codDepositoAmarillo = sucursalObject.get("COD_DEPOSITO_AMARILLO").toString()
                                MainActivity.usuarioLogin.codDepositoRojo = sucursalObject.get("COD_DEPOSITO_ROJO").toString()
                                MainActivity.usuarioLogin.descEmpresa = sucursalObject.get("DESC_EMPRESA").toString()
                                MainActivity.usuarioLogin.descSucursal = sucursalObject.get("DESC_SUCURSAL").toString()

                                tvEmpresa.text = "${MainActivity.usuarioLogin.codEmpresa} - " +
                                        "${MainActivity.usuarioLogin.descEmpresa} \n " +
                                        "${MainActivity.usuarioLogin.codSucursal} - " +
                                        "${MainActivity.usuarioLogin.descSucursal} "

                            }
                        }
                    } catch (e: Exception) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    }

                }
                override fun onError(e: Exception?) { // handle the result obtained from the asynchronous task
                    if (e != null) {
                        MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "Error null")
                    }
                }
            })

    }


    fun dialogoSeleccionarEmpresa(context: Context) {

        val checkedItem = intArrayOf(-1)

        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setIcon(R.drawable.ic_launcher5)
        // title of the alert dialog
        alertDialog.setTitle("Seleccione la empresa")
        var listItems = arrayOfNulls<String>(empresas.size)

        for (i in 0 until empresas.size) {
            listItems[i] = "${empresas[i].descripcion} (${empresas[i].codEmpresa})"
        }

        alertDialog.setSingleChoiceItems(listItems, checkedItem.get(0),
            DialogInterface.OnClickListener { dialog, which ->
                checkedItem[0] = which
                dialog.dismiss()
                codEmpresaTemp = empresas[which].codEmpresa
                cambiarSucursal()

            })

        alertDialog.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> })
        val customAlertDialog: AlertDialog = alertDialog.create()
        customAlertDialog.show()

    }


    fun dialogoSeleccionarSucursal(context: Context) {

        val checkedItem = intArrayOf(-1)

        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setIcon(R.drawable.ic_launcher5)
        alertDialog.setTitle("Seleccione la sucursal")
        var listItems = arrayOfNulls<String>(sucursales.size)

        for (i in 0 until sucursales.size) {

            listItems[i] = "${sucursales[i].descripcion} (${sucursales[i].codSucursal})"

        }

        alertDialog.setSingleChoiceItems(listItems, checkedItem.get(0),
            DialogInterface.OnClickListener { dialog, which ->
                checkedItem[0] = which

                dialog.dismiss()

                MainActivity.usuarioLogin.codEmpresa = sucursales[which].codEmpresa
                MainActivity.usuarioLogin.codSucursal = sucursales[which].codSucursal
                consultarDeposito()

            })

        alertDialog.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> })
        val customAlertDialog: AlertDialog = alertDialog.create()
        customAlertDialog.show()

    }



    fun cargaListaOperaciones() {

        alist_operaciones = ArrayList()


        var permisosUsuario: List<String> = MainActivity.usuarioLogin.permisos.split("||")

        permisosFijos = arrayOf(
            "PERMISO_ENTRADA",
            "PERMISO_MOV_HORIZONTAL",
            "PERMISO_MOV_VERTICAL",
            "PERMISO_ALMACENAMIENTO",
            "PERMISO_REABASTECIMIENTO",
            "PERMISO_SEPARACION",
            "PERMISO_REA_CORR_PREV",
            "PERMISO_CONS_DIRECCION",
            "PERMISO_CONS_ARTICULO",
            "PERMISO_CONS_STOCK",
            "PERMISO_INVENTARIO",
            "PERMISO_CONF_FRACCIONADO",
            "PERMISO_CONS_FRACCIONADO",
            "PERMISO_TRANSFERENCIA",
            "PERMISO_CERRAR_SESIONES",
            "PERMISO_CONSULTAR_CAMPOS",
            "PERMISO_CONSULTAR_TABLAS"
        )
        //PERMISO_CAMBIAR_EMPRESA

        operaciones = arrayOf(
            "Entrada",
            "Movimiento Horizontal",
            "Movimiento Vertical",
            //"Direccionamiento Direcciones",
            "Almacenamiento",
            "Reabastecimiento",
            "Separación",
            "Reabastecimiento Preventivo/Correctivo",
            "Consulta Dir. por Artículo",
            "Consultar Art. por Dirección",
            "Stock",
            "Inventario",
            "Confirmación de Fraccionado",
            "Consultar Fraccionado",
            "Transferencia",
            "Cerrar sesiones",
            "Consultar campos",
            "Consultar tablas"
        )

        /* clases = arrayOf(
             Operaciones::class,
             Operaciones::class
         )*/

        clasesFijas = arrayOf(
            BuscarPlanillaEntrada::class,
            MovimientoHorizontal::class,
            MovimientoVertical::class,
            //DireccionamientoDirecciones::class,
            ConfirmaAlmacenamientoUb::class,
            ReabastecimientoMarcaderiaDriveInNew::class,
            //ReabastecimientoMain::class,
            BuscarPlanillaSeparacion::class,  //							 ListReabastecimientoCorrPrev.class ,
            ReabastecimientoCorrPrev::class,  //							 ConfirmaRedireccion.class          , //Redireccion directa
            ConsultaArticulo::class,
            ConsultaDireccion::class,
            ConsultaStock::class,
            BuscarPlanillaInventario::class,  //ReabastecimientoMarcaderia.class ,
            ConsultaJaula::class,
            BuscarFraccionado::class,
            TransferenciaDeposito::class,
            CerrarSesion::class,
            ConsultarCampos::class,
            ConsultarTablas::class
        ) // 2 creado


        imagenesFijas = arrayOf(
            resources.getDrawable(R.drawable.inbox_into),
            resources.getDrawable(R.drawable.inbox_into),
            resources.getDrawable(R.drawable.inbox_into),
            resources.getDrawable(R.drawable.inbox_into),
            //resources.getDrawable(R.drawable.inbox_into),
            resources.getDrawable(R.drawable.transferencia),
            resources.getDrawable(R.drawable.pallet),
            resources.getDrawable(R.drawable.transferencia),
            resources.getDrawable(R.drawable.buscar2),
            resources.getDrawable(R.drawable.buscar2),
            resources.getDrawable(R.drawable.stock),
            resources.getDrawable(R.drawable.inventario),  //getResources().getDrawable(R.drawable.pallet),
            resources.getDrawable(R.drawable.pallet),
            resources.getDrawable(R.drawable.buscar2),
            resources.getDrawable(R.drawable.transferdep2),
            resources.getDrawable(R.drawable.buscar2),
            resources.getDrawable(R.drawable.buscar2),
            resources.getDrawable(R.drawable.buscar2)
        ) // 3 c


        var nreg = permisosFijos!!.size
        /*val cod_empleado2: String = MainActivity.usuarioLogin.codEmpleado
        if (MainActivity.usuarioLogin.codEmpleado == "665") {
            nreg = imagenes!!.size
        } else if (MainActivity.usuarioLogin.codEmpleado == "1737" || MainActivity.usuarioLogin.codEmpleado == "") {
            nreg = imagenes!!.size - 3
        } else {
            nreg = imagenes!!.size - 3
            //nreg = imagenes.length-3; // 4 creado
        }*/

        var listClasesActivas : List<KClass<*>> = ArrayList();
        var listImageesActivas : List<Drawable> = ArrayList();

        for (i in 0 until nreg) {
            var poseePermiso = false
            permisosUsuario.forEach {

                if (permisosFijos!![i] == it.toString()) {
                    poseePermiso = true
                    return@forEach
                }

            }

            if (poseePermiso) {

                val map2 = HashMap<String, String>()
                map2["DESC_OPERACION"] = operaciones!![i]
                (alist_operaciones as ArrayList<HashMap<String, String>>).add(map2)
                (listClasesActivas as ArrayList<KClass<*>>).add(clasesFijas!![i])
                (listImageesActivas as ArrayList<Drawable>).add(imagenesFijas!![i])

            }


        }

        clases = listClasesActivas.toTypedArray()
        imagenes = listImageesActivas.toTypedArray()

        sd = Adapter_lista_operaciones(
            this@Operaciones, alist_operaciones,
            R.layout.list_text_operaciones, arrayOf(
                "DESC_OPERACION"
            ), intArrayOf(R.id.tvDescOperacion)
        )

        lvOperaciones.setAdapter(sd)
        lvOperaciones.setOnItemClickListener(OnItemClickListener { parent, v, position, id ->
            //Ejecuta lo que esta arriba al dar clic de acuerdo a la posicion
            //				saveTransferencia = position;
            lvOperaciones.invalidateViews()
        })


    }


    class Adapter_lista_operaciones(
        context: Context?,
        items: List<HashMap<String, String>>?,
        resource: Int,
        from: Array<String?>?,
        to: IntArray?
    ) :
        SimpleAdapter(context, items, resource, from, to) {
        var _sqlupdate: String? = null
        private val colors = intArrayOf(Color.parseColor("#696969"), Color.parseColor("#808080"))

        inner class ViewHolder {
            var ivImagen: ImageView? = null
            var tvDescOperacion: TextView? = null
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = super.getView(position, convertView, parent)
            val colorPos = position % colors.size

//	    	view.setBackgroundColor(colors[colorPos]);
//
//	    	if (position == saveOperaciones) {
//	    		  view.setBackgroundColor(Color.BLUE);
//	    	}
            val holder: ViewHolder = ViewHolder()
            holder.ivImagen = view.findViewById(R.id.ivOperacion) as ImageView
            holder.tvDescOperacion = view.findViewById(R.id.tvDescOperacion) as TextView?
            holder.ivImagen!!.setImageDrawable(imagenes!![position])
            holder.ivImagen!!.setBackgroundColor(colors[colorPos])
            view.setOnClickListener {
                try {
                    context.startActivity(Intent(context, (clases!![position]).java))
                } catch (e: java.lang.Exception) {
                    var err = e.message
                    err += ""
                }
            }
            view.setTag(holder)
            return view
        }
    }



}