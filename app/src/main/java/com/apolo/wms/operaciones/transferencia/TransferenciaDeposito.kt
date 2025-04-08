package com.apolo.wms.operaciones.transferencia

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.transferencia.*
import com.apolo.wms.operaciones.entrada.EntradaMercaderia
import com.apolo.wms.operaciones.separacion.ConfirmaSeparacion
import com.apolo.wms.operaciones.transferencia.adapter.AdapterDetalleTransferencia
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.entrada_conferido.*
import kotlinx.android.synthetic.main.entrada_redireccion_ub.*
import kotlinx.android.synthetic.main.lector_codigo_articulo_reabast_normal.*
import kotlinx.android.synthetic.main.lector_codigo_articulo_reabast_normal.btnAceptar
import kotlinx.android.synthetic.main.lector_codigo_articulo_reabast_normal.btn_volver
import kotlinx.android.synthetic.main.lector_codigo_articulo_reabast_normal.etCodigoBarra
import kotlinx.android.synthetic.main.reabastecimiento_mercaderia_drive_in.*
import kotlinx.android.synthetic.main.transferencia_deposito.*
import kotlinx.android.synthetic.main.transferencia_deposito.btnCancelar
import kotlinx.android.synthetic.main.transferencia_deposito.btnConfirmar
import kotlinx.android.synthetic.main.transferencia_deposito.etDestino
import kotlinx.android.synthetic.main.transferencia_deposito.rvReabastDireccion
import kotlinx.android.synthetic.main.transferencia_deposito.tvDatos
import kotlinx.android.synthetic.main.transferencia_deposito.tvDatos2
import kotlinx.android.synthetic.main.transferencia_deposito.tvDatos3
import kotlinx.android.synthetic.main.transferencia_ingresa_cant.*
import kotlinx.android.synthetic.main.transferencia_selecciona_motivo.*
import kotlinx.android.synthetic.main.transferencia_selecciona_motivo.spDirecciones
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class TransferenciaDeposito : AppCompatActivity() {

    companion object {
        lateinit var context : Context

        var detalleTransferencia = ArrayList<DetalleTransferencia>()
        var posicionDetalleTransferencia = 0

        var articuloTransferencia = ArrayList<ArticuloTransferencia>()
        var posicionArticulo = 0


        var umTransferencia = ArrayList<UnidadMedidaTransferencia>()
        var posicionUM = 0
        var posicionUMBasico = 0

        var cabecera = CabeceraTransferencia()

        lateinit var btnCancelar : Button
        lateinit var spDepositoOri : Spinner
        lateinit var spDepositoDes : Spinner
        lateinit var etOrigen : EditText
        lateinit var etDestino : EditText
        lateinit var tvDestino : TextView
        lateinit var btnConfirmar : Button
        lateinit var tvDatos : TextView
        lateinit var tvDatos2 : TextView
        lateinit var tvDatos3 : TextView


        lateinit var rvReabastDireccion : RecyclerView

        var operacion_iniciada = 0


        fun validarCabeceraProceso() : Boolean {
            var validacion = false


            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", cabecera.tipComprobante)
                .add("SER_COMPROBANTE", cabecera.serComprobante)
                .add("NRO_COMPROBANTE", cabecera.nroComprobante)
                .build()

            var result = HttpRequest.call("", "transferencia/consulta_estado_transferencia", formBody)



            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_estado_transferencia) Error ${e.message.toString()} !")
                return false
            }

            if (respuestaJson.has("rows")) {


                val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

                if (cabecera.nroComprobante == "") {
                    validacion = false
                } else {

                    if (filas.length() > 0) {
                        validacion = true
                    }


                }

            }  else {
                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validarCabeceraProceso)")
            }

            btnCancelar.isEnabled = MainActivity.usuarioLogin.codEmpleado == "665"

            return validacion

        }


        fun validaDirecciones2() : Boolean {

            if (spDepositoOri.selectedItemId == spDepositoDes.selectedItemId) {
                return false
            }else{
                if (spDepositoOri.selectedItem.toString().split("-")[0].trim() == "100" &&
                    spDepositoDes.selectedItem.toString().split("-")[0].trim() == "01" &&
                    MainActivity.usuarioLogin.codEmpresa.equals("1")) {
                    return false
                }
                if (spDepositoDes.selectedItem.toString().split("-")[0].trim() == "100" &&
                    MainActivity.usuarioLogin.codEmpresa.equals("1")) {
                    return false
                }
                if (spDepositoDes.selectedItem.toString().split("-")[0].trim() == "034" &&
                    MainActivity.usuarioLogin.codEmpresa.equals("1")) {
                    return false
                }
                return true
            }

        }

        fun eliminarCabecera() {

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", cabecera.tipComprobante)
                .add("SER_COMPROBANTE", cabecera.serComprobante)
                .add("NRO_COMPROBANTE", cabecera.nroComprobante)
                .build()

            var result = HttpRequest.call("", "transferencia/eliminar_cabecera", formBody)

            limpiarLista()
            limpiarCabecera()

        }


        fun limpiarCabecera() {

            tvDatos.text = ""
            tvDatos2.text = ""
            tvDatos3.text = ""
            etOrigen.setText("")
            etDestino.setText("")
            spDepositoOri.setSelection(0)
            spDepositoDes.setSelection(1)
            cabecera.nroComprobante = ""

        }

        fun limpiarLista() {

            detalleTransferencia.clear()
            rvReabastDireccion.adapter!!.notifyDataSetChanged()

        }


        fun cancelar() {

            if (operacion_iniciada==1) {

                if (!validarCabeceraProceso()) {
                    eliminaTransferenciaGeneral()
                    eliminarCabecera()
                    spDepositoDes.isEnabled = true
                    spDepositoOri.isEnabled = true
                    operacion_iniciada=0
                    etDestino.setText("Sin dirección")
                    definirFoco(spDepositoOri, etOrigen)
                    definirFoco(spDepositoDes, etDestino)
                    btnConfirmar.isEnabled = true
                    tvDestino.text = "Dirección Destino:"
                } else {
                    MainActivity.funciones.mensajeError(context, "Atencion", "La transferencia ya esta en proceso de verificación.")

                }

            } else {

                MainActivity.funciones.mensajeError(context, "Atencion", "No se encuentra ninguna operación iniciada.")

            }

        }

        fun definirFoco(spinner: Spinner, texto: EditText) {

            spinner.isEnabled = false
            if (spinner.selectedItemId.toInt() == 0) {
                texto.isEnabled = true
                texto.setText("")
                texto.requestFocus()
            }
            else {
                texto.isEnabled = false
            }
            if (operacion_iniciada==0) {
                spinner.isEnabled = true
            }
            if (spDepositoDes.selectedItemId.toInt() == 0 && validarCabeceraProceso()) {
                btnConfirmar.isEnabled = false
                tvDestino.text = "Codigo Artículo:"
            }
            else{
                btnConfirmar.isEnabled = true
                tvDestino.text = "Dirección Destino:"
            }

        }

        fun validarDetalle(posicion: Int) : Boolean {

            var validacion = false

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", cabecera.tipComprobante)
                .add("SER_COMPROBANTE", cabecera.serComprobante)
                .add("NRO_COMPROBANTE", cabecera.nroComprobante)
                .add("NRO_ORDEN", detalleTransferencia[posicion].nroOrden)
                .build()

            var result = HttpRequest.call("", "transferencia/validar_detalle", formBody)



            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (validar_detalle) Error ${e.message.toString()} !")
                return false
            }

            if (respuestaJson.has("rows")) {

                val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

                if (filas.length() > 0) {
                    validacion = true
                }

            }  else {
                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validarDetalle)")
            }

            return validacion;

        }


        fun eliminaTransferencia(posicion: Int) {

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", cabecera.tipComprobante)
                .add("SER_COMPROBANTE", cabecera.serComprobante)
                .add("NRO_COMPROBANTE", cabecera.nroComprobante)
                .add("NRO_ORDEN", detalleTransferencia[posicion].nroOrden)
                .build()

            var result = HttpRequest.call("", "transferencia/eliminar_detalle2", formBody)

            detalleTransferencia.removeAt(posicionDetalleTransferencia)
            rvReabastDireccion.adapter!!.notifyDataSetChanged()

        }

        fun eliminaTransferenciaGeneral() {

            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", cabecera.tipComprobante)
                .add("SER_COMPROBANTE", cabecera.serComprobante)
                .add("NRO_COMPROBANTE", cabecera.nroComprobante)
                .build()

            var result = HttpRequest.call("", "transferencia/eliminar_detalle", formBody)


        }


        fun eliminarDetalleTransferencia() {

            if (!validarCabeceraProceso()) {

                if (validaDirecciones2()) {

                    SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Atencion!")
                        .setContentText("¿Desea Borrar Direccion = ${detalleTransferencia[posicionDetalleTransferencia].codDireccion}?")
                        .setConfirmText("Si")
                        .setConfirmClickListener { sDialog ->


                            if (validarDetalle(posicionDetalleTransferencia)) {
                                if (detalleTransferencia.size > 1) {
                                    eliminaTransferencia(posicionDetalleTransferencia);
                                } else {
                                    cancelar();
                                }
                            } else {
                                MainActivity.funciones.mensajeError(context, "Atencion", "El detalle ya fue confirmado.")
                            }

                            sDialog.dismissWithAnimation()

                        }
                        .setCancelButton(
                            "No"
                        ) { sDialog -> sDialog.dismissWithAnimation() }
                        .show()


                } else {

                    MainActivity.funciones.mensajeError(context, "Atencion", "La transferencia ya esta en proceso de verificacion.")

                }

            }

        }


    }


    var cantidad_disp = "0"
    var cantidad_transferida = "0"

    var tipo_operacion_reabast = ""

    var inicio = false



    var cod_articulo_val = ""
    var cod_articulo = ""
    var cantidad_disponible = ""
    var art_adicional = "N"
    var esPesable = "N"
    var articulo_validado = false

    var codigoCausa = ""

    var cantidad_detalle = 0.0


    private lateinit var dialogo_lee_codigo: Dialog
    private lateinit var dialogo_selecciona_causa: Dialog
    private lateinit var dialogo_selecciona_direccion: Dialog
    private lateinit var dialogo_inserta_cantidad: Dialog
    private lateinit var dialogo_lee_direccion: Dialog



    var contador = 0


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transferencia_deposito)

        inicializar()

    }


    fun inicializar() {

        //limpiar todo
        detalleTransferencia = ArrayList<DetalleTransferencia>()
        posicionDetalleTransferencia = 0
        articuloTransferencia = ArrayList<ArticuloTransferencia>()
        posicionArticulo = 0
        umTransferencia = ArrayList<UnidadMedidaTransferencia>()
        posicionUM = 0
        posicionUMBasico = 0
        cabecera = CabeceraTransferencia()
        operacion_iniciada = 0

        context = this

        Companion.btnCancelar = btnCancelar
        Companion.spDepositoOri = spDepositoOri
        Companion.spDepositoDes = spDepositoDes
        Companion.etDestino = etDestino
        Companion.tvDestino = tvDestino
        Companion.btnConfirmar = btnConfirmar
        Companion.etOrigen = etOrigen
        Companion.rvReabastDireccion = rvReabastDireccion
        Companion.tvDatos = tvDatos
        Companion.tvDatos2 = tvDatos2
        Companion.tvDatos3 = tvDatos3


        tvDatos.text = "-"
        tvDatos2.text = "-"
        tvDatos3.text = ""



        /*codigo de barra de producto*/
        etOrigen.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if ((spDepositoOri.selectedItem.toString().indexOf("VERDE")>-1) || (spDepositoOri.selectedItem.toString().split("-")[0].trim() == "2012" && MainActivity.usuarioLogin.codEmpresa == "2")) {
                    if (validarCampoTexto(etOrigen)) {
                        if (!validaDirecciones(spDepositoOri, spDepositoDes)) {
                            MainActivity.funciones.mensajeError(context, "Atencion", "Direcciones invalidas")
                        } else {
                            if (!validaDirecciones2()) {
                                if (MainActivity.usuarioLogin.codSucursal == "02") {
                                    MainActivity.funciones.mensajeError(context, "Atencion", "No se permiten transferencias al deposito 034")
                                } else {
                                    MainActivity.funciones.mensajeError(context, "Atencion", "No se permiten transferencias al deposito 100")
                                }
                            }
                            procesar()
                        }
                    }
                }

            }
            override fun afterTextChanged(s: Editable?) { }
        })


        etDestino.addTextChangedListener(object : TextWatcher {
            var dir_aux = ""
            var largo = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (etDestino.text.isEmpty()) {

                }else if (largo>0 &&
                    etDestino.text.toString().trim() == dir_aux.trim().substring(0, largo - 1)
                ) {
                    etDestino.setText("")
                    dir_aux = etDestino.text.toString().trim()
                    largo = etDestino.text.toString().trim().length
                } else if (spDepositoDes.selectedItem.toString().indexOf("VERDE")>-1
                    && etDestino.text.isNotEmpty()
                ) {
                    if (validarCabeceraProceso()) {
                        leerCodigoConfirmacion(etDestino)
                        dir_aux = etDestino.text.toString().trim()
                        largo = etDestino.text.toString().trim().length
                    }else{
                        buscarDireccion(etDestino.text.toString().trim())
                        dir_aux = etDestino.text.toString().trim()
                        largo = etDestino.text.toString().trim().length
                    }
                }

            }
            override fun afterTextChanged(s: Editable?) { }
        })



        btnOk.setOnClickListener {

            if (!validaDirecciones(spDepositoOri, spDepositoDes)) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Direcciones invalidas")
            }else{
                if (!validaDirecciones2()) {
                    if (MainActivity.usuarioLogin.codSucursal == "02") {
                        MainActivity.funciones.mensajeError(context, "Atencion", "No se pueden realizar transferencias del deposito 034 al 210 ni al deposito 034")
                    } else {
                        MainActivity.funciones.mensajeError(context, "Atencion", "No se pueden realizar transferencias del deposito 100 al 01 ni al deposito 100")
                    }
                    return@setOnClickListener
                }
                procesar()
            }

        }


        btnCancelar.setOnClickListener {

            if (validaDirecciones2()) {
                if (operacion_iniciada === 1) {

                    SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Atencion!")
                        .setContentText("¿Cancelar Transferencia Actual?")
                        .setConfirmText("Si")
                        .setConfirmClickListener { sDialog ->

                            cancelar()
                            sDialog.dismissWithAnimation()

                        }
                        .setCancelButton(
                            "No"
                        ) { sDialog -> sDialog.dismissWithAnimation() }
                        .show()

                }
            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "No se encuentra ninguna transferencia iniciada.")


            }

        }


        btnConfirmar.setOnClickListener {

            if (operacion_iniciada == 1) {

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atencion!")
                    .setContentText("¿Finalizar transferencia?")
                    .setConfirmText("Si")
                    .setConfirmClickListener { sDialog ->

                        var total_sob = cantidad_disp.toDouble() - cantidad_transferida.toDouble()
                        var _band = tipo_operacion_reabast

                        if (validaDirecciones2()) {

                            if (validarCabeceraProceso()) {
                                if (validarCabecera()/*punto clave*/) {

                                    if  (
                                        ((spDepositoOri.selectedItem.toString().indexOf("VERDE")>-1) ||
                                                spDepositoDes.selectedItem.toString().indexOf("VERDE") <= -1 ||
                                                (spDepositoOri.selectedItem.toString().split("-")[0].trim() == "100" ||
                                                        (spDepositoOri.selectedItem.toString().split("-")[0].trim() == "034")))
                                        ||
                                        ((spDepositoOri.selectedItem.toString().split("-")[0] == "2012" ||
                                                spDepositoDes.selectedItem.toString().split("-")[0] != "2012"
                                                ))){

                                        /*while(detalleTransferencia.size >0){
                                            confirmarDetalle(0)
                                            eliminarItem2(0)
                                            if (detalleTransferencia.size == 0) {
                                                limpiarCabecera()
                                                MainActivity.funciones.mensajeExito(context, "Transferencia finalizada", "La operación ha finalizado exitosamente.")

                                                spDepositoDes.isEnabled = true
                                                spDepositoOri.isEnabled = true
                                            }
                                        }*/

                                        if (detalleTransferencia.size > 0) {
                                            confirmarDetalleTodo()
                                            eliminarItem2Todo()

                                            if (detalleTransferencia.size == 0) {
                                                limpiarCabecera()
                                                MainActivity.funciones.mensajeExito(context, "Transferencia finalizada", "La operación ha finalizado exitosamente.")

                                                spDepositoDes.isEnabled = true
                                                spDepositoOri.isEnabled = true
                                            }
                                        }




                                    } else {

                                        if(detalleTransferencia.size > 0){
                                            confirmarDetalle(posicionDetalleTransferencia)
                                            eliminarItem2(posicionDetalleTransferencia)
                                            if (detalleTransferencia.size == 0) {
                                                limpiarCabecera()
                                                MainActivity.funciones.mensajeExito(context, "Transferencia finalizada", "La operación ha finalizado exitosamente.")

                                                spDepositoDes.isEnabled = true
                                                spDepositoOri.isEnabled = true
                                            }
                                        }

                                    }

                                } else {

                                    MainActivity.funciones.mensajeError(context, "Error!", "La cabecera no ha sido confirmada.")

                                }


                            } else {
                                cerrarCabecera()
                                validarCabeceraProceso()
                            }

                        }

                        sDialog.dismissWithAnimation()

                    }
                    .setCancelButton(
                        "No"
                    ) { sDialog -> sDialog.dismissWithAnimation() }
                    .show()

            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "No se encuentra ninguna operacion iniciada")
            }

        }



        inicio = false
        obtiene_depositos(spDepositoOri)
        obtiene_depositos(spDepositoDes)
        spDepositoDes.setSelection(1)
        spDepositoOri.setSelection(0)
        cargaCabecera()
        if (cabecera.nroComprobante != "") {
            cargaTransferenciaDetalle()
            validarCabeceraProceso()
        }


        insertaCabecera()
        definirDireccionOrigenInicio(spDepositoOri, etOrigen)
        definirDireccionDestinoInicio(spDepositoDes, etDestino)
        cargarLista(detalleTransferencia)


        spDepositoDes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (detalleTransferencia.size == 0) {
                    definirDireccion(spDepositoDes, etDestino)
                    etDestino.requestFocus()
                }
                else{
                    definirDireccion(spDepositoDes, etDestino)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        spDepositoOri.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if(detalleTransferencia.size == 0){
                    definirDireccion(spDepositoOri, etOrigen)
                }else{
                    definirDireccion(spDepositoOri, etOrigen)
                    etDestino.requestFocus()
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }



    }


    fun validarCampoTexto(texto: EditText) : Boolean {

        val validacion = false

        if ((texto.text.toString().uppercase(Locale.getDefault()).replace("A", "").replace("\n", "").trim()
                .length in 2..8)
            || texto.text.toString().uppercase(Locale.getDefault()).replace("A", "").replace("\n", "").trim()
                .length > 9
        ) {
            val title = "Error"
            val message = "El código no corresponde a una dirección."
            if (texto.text.toString().indexOf("034") > -1) {
                MainActivity.funciones.mensajeError(context, title, message)
            }
        }


        return validacion

    }


    fun validaDirecciones(origen: Spinner, destino: Spinner) :  Boolean {

        var validacion = false

        if (origen.selectedItemId ==destino.selectedItemId) {
            validacion = false
        } else if (etOrigen.text.isEmpty() || etDestino.text.isEmpty()) {

            var message = "Debe llenar los campos de dirección de origen y destino."
            var title2 = "Atención!"
            MainActivity.funciones.mensajeError(context, title2, message)
            validacion = false
        }else {
            validacion = true
        }



        return validacion

    }



    fun validaDirecciones2() : Boolean {

        if (spDepositoOri.selectedItemId == spDepositoDes.selectedItemId) {
            return false
        }else{
            if (spDepositoOri.selectedItem.toString().split("-")[0].trim() == "100" &&
                spDepositoDes.selectedItem.toString().split("-")[0].trim() == "01" &&
                MainActivity.usuarioLogin.codEmpresa.equals("1")) {
                return false
            }
            if (spDepositoDes.selectedItem.toString().split("-")[0].trim() == "100" &&
                MainActivity.usuarioLogin.codEmpresa.equals("1")) {
                return false
            }
            if (spDepositoDes.selectedItem.toString().split("-")[0].trim() == "034" &&
                MainActivity.usuarioLogin.codEmpresa.equals("1")) {
                return false
            }
            return true
        }

    }



    fun procesar() {

        if (!validarCabeceraProceso()) {
            if (valida_existencia_direccion(etOrigen.text.toString().trim())) {
                validaArticulo(etOrigen.text.toString().trim(), spDepositoOri)
                dialogoLeeCodigo()
                seleccionarCausa(buscarMotivos())
            } else {
                definirFoco(spDepositoOri, etOrigen)
                definirFoco(spDepositoDes, etDestino)
            }
        } else {
            val message = "La transferencia ya esta en proceso de verificacion."
            val title2 = "Atención!"
            MainActivity.funciones.mensajeError(context, title2, message)
        }
        if (contador > 1 && detalleTransferencia.size == 0) {
            try {
                dialogo_selecciona_causa.dismiss()
                contador = 0
            } catch (e: Exception) {
                // TODO: handle exception
                MainActivity.funciones.mensajeError(context, "Atencion", e.message.toString())
            }
        }

    }



    fun validarCabeceraProceso() : Boolean {
        var validacion = false


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", cabecera.tipComprobante)
            .add("SER_COMPROBANTE", cabecera.serComprobante)
            .add("NRO_COMPROBANTE", cabecera.nroComprobante)
            .build()

        var result = HttpRequest.call("", "transferencia/consulta_estado_transferencia", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_estado_transferencia) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("rows")) {


            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            if (cabecera.nroComprobante == "") {
                validacion = false
            } else {

                if (filas.length() > 0) {
                    validacion = true
                }


            }

        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validarCabeceraProceso)")
        }

        btnCancelar.isEnabled = MainActivity.usuarioLogin.codEmpleado == "665"

        return validacion

    }


    fun validarCabecera() : Boolean {

        var validacion = false


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", cabecera.tipComprobante)
            .add("SER_COMPROBANTE", cabecera.serComprobante)
            .add("NRO_COMPROBANTE", cabecera.nroComprobante)
            .build()

        var result = HttpRequest.call("", "transferencia/valida_cabecera", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_cabecera) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("rows")) {


            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

            if (filas.length() > 0) {
                validacion = true
            }

        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validarCabecera)")
        }


        return validacion

    }


    fun leerCodigoConfirmacion(et: EditText) {

        val codigoArticuloValidacion = et.text.toString().trim().replace("A", "").replace("\n", "")
        var codigoArticuloValidado = ""


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", codigoArticuloValidacion)
            .build()

        var result = HttpRequest.call("", "transferencia/consulta_articulo", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_articulo) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {


            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until filas.length()) {
                val articuloObject : JSONObject = filas[i] as JSONObject

                codigoArticuloValidado = articuloObject.get("COD_ARTICULO").toString()

            }


        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validarCabeceraProceso)")
        }

        buscarEnLista(codigoArticuloValidado)

    }


    fun buscarDireccion(codArticulo: String) : Boolean {


        try {


            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_ARTICULO", codArticulo)
                .build()

            var result = HttpRequest.call("", "transferencia/buscar_direccion", formBody)



            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscar_direccion) Error ${e.message.toString()} !")
                return false
            }

            if (respuestaJson.has("rows")) {


                val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)

                val direcciones: ArrayList<String> = ArrayList()

                for (i in 0 until filas.length()) {
                    val fila : JSONObject = filas[i] as JSONObject


                    var dir = ""
                    var dir_aux = ""
                    try {
                        dir = ""
                        dir_aux = fila.get("COD_DIRECCION").toString()
                        if (dir_aux.length > 0) {
                            dir = dir_aux.substring(0, 3) + "-" + dir_aux.substring(
                                3,
                                6
                            ) + "-" + dir_aux.substring(6, 7) + "-" + dir_aux.subSequence(
                                7,
                                9
                            ) + " - Unidad"
                            direcciones.add(dir)
                        }
                        art_adicional = fila.get("ART_ADICIONAL").toString()
                    } catch (e: java.lang.Exception) {
                    }
                    try {
                        dir = ""
                        dir_aux = fila.get("COD_DIRECCION_CAJA").toString()
                        if (dir_aux.length > 0) {
                            dir = dir_aux.substring(0, 3) + "-" + dir_aux.substring(
                                3,
                                6
                            ) + "-" + dir_aux.substring(6, 7) + "-" + dir_aux.subSequence(
                                7,
                                9
                            ) + " - Caja"
                            direcciones.add(dir)
                        }
                    } catch (e: java.lang.Exception) {
                    }

                }


                if (direcciones.get(0).substring(0, 7) == "000-000" && direcciones.get(1)
                        .substring(0, 7) == "000-000"
                ) {

                    MainActivity.funciones.mensajeError(context, "Atencion", "No se encontraron direcciones de picking para el artículo ingresado.")
                    etDestino.setText("")

                } else if (direcciones[0].substring(0, 7).equals("000-000") || direcciones[1]
                        .substring(0, 7) == "000-000"
                ) {
                    if (direcciones[0].substring(0, 7).equals("000-000")) {
                        etDestino.setText(direcciones[1].replace("-", "").substring(0, 9).trim())
                    } else {
                        etDestino.setText(direcciones[0].replace("-", "").substring(0, 9).trim())
                    }
                } else {
                    if (etDestino.text.isNotEmpty()) {
                        seleccionarDireccion(direcciones, etDestino, null)
                    } else {
                        etDestino.setText("")
                    }
                }


            }  else {
                MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (buscarDireccion)")
            }

        } catch (e: Exception) {



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



        art_adicional = "N"
        return false


    }






    fun confirmarDetalle(position: Int) {


        var orden = detalleTransferencia[position].nroOrden.trim()


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", cabecera.tipComprobante.trim())
            .add("SER_COMPROBANTE", cabecera.serComprobante.trim())
            .add("NRO_COMPROBANTE", cabecera.nroComprobante.trim())
            .add("NRO_ORDEN", orden)
            .build()

        var result = HttpRequest.call("", "transferencia/confirma_detalle", formBody)



    }


    fun confirmarDetalleTodo() {



        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", cabecera.tipComprobante.trim())
            .add("SER_COMPROBANTE", cabecera.serComprobante.trim())
            .add("NRO_COMPROBANTE", cabecera.nroComprobante.trim())
            .build()

        var result = HttpRequest.call("", "transferencia/confirma_detalle_todo", formBody)

        result = result

    }



    fun eliminarItem2(position: Int) {

        detalleTransferencia.removeAt(position)
        rvReabastDireccion.adapter!!.notifyDataSetChanged()


    }

    fun eliminarItem2Todo() {

        detalleTransferencia.clear()
        rvReabastDireccion.adapter!!.notifyDataSetChanged()


    }


    fun cargaTransferenciaDetalle() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", cabecera.tipComprobante)
            .add("SER_COMPROBANTE", cabecera.serComprobante)
            .add("NRO_COMPROBANTE", cabecera.nroComprobante)
            .build()

        var result = HttpRequest.call("", "transferencia/consulta_detalle_transferencia", formBody)


        detalleTransferencia = ArrayList()
        posicionDetalleTransferencia = 0

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_detalle_transferencia) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {


            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas[i] as JSONObject
                var dt = DetalleTransferencia()
                dt.codEmpresa = filaObject.get("COD_EMPRESA").toString()
                dt.tipComprobante = filaObject.get("TIP_COMPROBANTE").toString()
                dt.serComprobante = filaObject.get("SER_COMPROBANTE").toString()
                dt.nroComprobante = filaObject.get("NRO_COMPROBANTE").toString()
                dt.nroOrden = filaObject.get("NRO_ORDEN").toString()
                dt.codDireccionDes = filaObject.get("COD_DIRECCION_DES").toString()
                dt.cantidad = filaObject.get("CANTIDAD").toString()
                dt.codArticulo = filaObject.get("COD_ARTICULO").toString()
                dt.codDepositoEnt = filaObject.get("COD_DEPOSITO_ENT").toString()

                dt.codDireccion = filaObject.get("COD_DIRECCION").toString()
                if (dt.codDireccion == null || dt.codDireccion == "null") {
                    dt.codDireccion = "Sin dirección"
                }
                dt.codDeposito = filaObject.get("COD_DEPOSITO").toString()
                dt.codCausa = filaObject.get("COD_CAUSA").toString()


                detalleTransferencia.add(dt)

            }

            if (detalleTransferencia.size == 0) {
                cancelar()
            }

            definirDireccion(spDepositoDes, etDestino)
            definirDireccion(spDepositoOri, etOrigen)


        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validarCabeceraProceso)")
        }

    }


    fun cerrarCabecera() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", cabecera.tipComprobante.trim())
            .add("SER_COMPROBANTE", cabecera.serComprobante.trim())
            .add("NRO_COMPROBANTE", cabecera.nroComprobante.trim())
            .build()

        var result = HttpRequest.call("", "transferencia/cerrar_cabecera", formBody)

        MainActivity.funciones.mensajeExito(context, "Confirmacion exitosa", "La transferencia ya esta en proceso de verificacion.")
        definirFoco(spDepositoOri, etOrigen);
        definirFoco(spDepositoDes, etDestino);


    }


    fun obtiene_depositos(spinner: Spinner) {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        var result = HttpRequest.call("", "transferencia/obtiene_depositos", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (obtiene_depositos) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {


            var depositoTransferencia = ArrayList<DepositoTransferencia>()

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas[i] as JSONObject
                var dt = DepositoTransferencia()
                dt.codDeposito = filaObject.get("COD_DEPOSITO").toString()
                dt.descDeposito = filaObject.get("DESCRIPCION").toString()

                depositoTransferencia.add(dt)

            }


            val spinnerAdapter : ArrayAdapter<DepositoTransferencia> =
                ArrayAdapter(context, R.layout.spinner_adapter, depositoTransferencia)
            spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinnerAdapter



            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    etDestino.requestFocus()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }


            if (depositoTransferencia.size > 0) {
                spinner.setSelection(0)
            }


        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (obtiene_depositos)")
        }



    }



    fun cargaCabecera() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        var result = HttpRequest.call("", "transferencia/cargar_cabecera", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (cargar_cabecera) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {



            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas[i] as JSONObject

                cabecera.estado = filaObject.get("ESTADO").toString()
                cabecera.tipComprobante = filaObject.get("TIP_COMPROBANTE").toString()
                cabecera.serComprobante = filaObject.get("SER_COMPROBANTE").toString()
                cabecera.nroComprobante = filaObject.get("NRO_COMPROBANTE").toString()
                operacion_iniciada = 1

            }



        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaCabecera)")
        }



    }


    fun insertaCabecera() {

        if (cabecera.nroComprobante == "") {
            operacion_iniciada = 0
        } else {

            tvDatos3.text = "Transferencia Iniciada - Estado: ${cabecera.estado}"
            tvDatos.text = "Nro. de comprobante: ${cabecera.nroComprobante}"
            tvDatos2.text = "Deposito de origen: ${cabecera.codDeposito}"
            operacion_iniciada = 1

            val aux = etDestino.text.toString().trim()
            val aux2 = etOrigen.text.toString().trim()

            definirFoco(spDepositoOri, etOrigen)
            definirFoco(spDepositoDes, etDestino)

            etDestino.setText(aux)
            etOrigen.setText(aux2)

        }

    }





    fun definirDireccionOrigenInicio(spinner: Spinner, et: EditText) {

        if (detalleTransferencia.size > 0) {
            var confirmacion = true
            for (i in 0 until spinner.count) {
                if (confirmacion) {
                    if (spinner.getItemAtPosition(i).toString().split("-")[0].trim() == detalleTransferencia[0].codDeposito.trim()) {
                        spinner.setSelection(i)
                        confirmacion = false
                        if (spinner.selectedItem.toString().indexOf("VERDE") > -1 ||
                            spinner.selectedItem.toString().split("-")[0].trim() == "2012" && MainActivity.usuarioLogin.codEmpleado == "2"
                        ) {
                            et.setText("")
                            btnConfirmar.isEnabled = false
                        } else {
                            et.setText("Sin dirección")
                            et.isEnabled = false
                        }
                    }
                }
            }
        }

    }


    fun definirDireccionDestinoInicio(spinner: Spinner, et: EditText) {

        if (detalleTransferencia.size > 0) {
            var confirmacion = true
            for (i in 0 until spinner.count) {
                if (confirmacion) {
                    if (spinner.getItemAtPosition(i).toString().split("-")[0].trim() == detalleTransferencia[0].codDepositoEnt.trim()
                    ) {
                        spinner.setSelection(i)
                        confirmacion = false
                        if (spinner.selectedItem.toString().indexOf("VERDE") > -1 ||
                            spinner.selectedItem.toString().split("-")[0].trim() == "2012" && MainActivity.usuarioLogin.codEmpleado == "2"
                        ) {
                            et.setText("")
                        } else {
                            et.setText("Sin dirección")
                            et.isEnabled = false
                        }
                        if (((spDepositoDes.selectedItem.toString().indexOf("VERDE") > -1) ||
                                    (spinner.selectedItem.toString().split("-")[0].trim() == "2012" && MainActivity.usuarioLogin.codEmpleado == "2"))
                            && validarCabeceraProceso()
                        ) {
                            btnConfirmar.isEnabled = false
                            tvDestino.text = "Codigo Articulo:"
                        } else {
                            btnConfirmar.isEnabled = true
                            tvDestino.text = "Dirección Destino:"
                        }
                    }
                }
            }
        }

    }


    fun cargarLista(lista: ArrayList<DetalleTransferencia>) {


        val gridLayoutManager = GridLayoutManager(context, 1)

        rvReabastDireccion.layoutManager = gridLayoutManager
        rvReabastDireccion.itemAnimator = DefaultItemAnimator()
        rvReabastDireccion.setHasFixedSize(true)


        // this creates a vertical layout Manager
        rvReabastDireccion.layoutManager = LinearLayoutManager(context)

        if (spDepositoDes.selectedItem.toString().lowercase(Locale.getDefault()).indexOf("verde") > -1 ||
            spDepositoDes.selectedItem.toString().split("-")[0].trim() == "2012" && MainActivity.usuarioLogin.codEmpleado == "2"
        ) {


            val adapter = AdapterDetalleTransferencia(
                context,
                lista,
                R.layout.card_view_confirmar_transferencia_dep_verde  )

            adapter.notifyDataSetChanged()
            // Setting the Adapter with the recyclerview
            rvReabastDireccion.adapter = adapter


        } else {

            val adapter = AdapterDetalleTransferencia(
                context,
                lista,
                R.layout.card_view_confirmar_transferencia)

            adapter.notifyDataSetChanged()
            // Setting the Adapter with the recyclerview
            rvReabastDireccion.adapter = adapter

        }


    }


    fun definirDireccion(spinner: Spinner, et: EditText) {

        if (spinner.selectedItem.toString().lowercase(Locale.getDefault()).indexOf("verde")==-1) {
            et.setText("Sin dirección")
            et.isEnabled = false
            if (spinner.selectedItem.toString().split("-")[0].trim().equals("2012") && MainActivity.usuarioLogin.codEmpleado == "2") {
                et.setText("")
                et.isEnabled = true
            }
        }else{
            et.setText("")
            et.isEnabled = true
        }


    }



    fun valida_existencia_direccion(codDireccion : String) : Boolean {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", codDireccion)
            .build()

        var result = HttpRequest.call("", "transferencia/valida_existencia_direccion", formBody)

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_existencia_direccion) Error ${e.message.toString()} !")
            return false
        }

        if (respuestaJson.has("respuesta")) {
            var respuesta = respuestaJson.get("respuesta").toString()

            if (etOrigen.text.toString().trim() == "Sin dirección") {
                respuesta = "S"
            }
            if (respuesta.trim() == "S") {
                return true
            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "DIRECCION SIN EXISTENCIA")
                return false
            }



        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaExistenciaDireccion)")
            return false
        }

    }



    fun validaArticulo(articuloDireccion: String, spinner: Spinner) {


        cod_articulo_val = ""
        articulo_validado = false
        var dep = ""
        var direccion = articuloDireccion

        var metodo = ""

        if ((spinner.selectedItem.toString().indexOf("VERDE")>-1) ||
            (spinner.selectedItem.toString().split("-")[0].trim() == "2012" && MainActivity.usuarioLogin.codEmpleado == "2")) {

            metodo = "transferencia/valida_articulo1"

        } else if (spDepositoDes.selectedItem.toString().indexOf("VERDE")>-1 ||
            (spDepositoDes.selectedItem.toString().split("-")[0].trim() == "2012" &&
                    MainActivity.usuarioLogin.codEmpleado == "2"))
        {

            dep = spinner.selectedItem.toString().split("-")[0].trim()
            metodo = "transferencia/valida_articulo2"

            direccion = etDestino.text.toString().trim().replace("A", "").replace("\n", "")

        } else {

            dep = spinner.selectedItem.toString().split("-")[0].trim()
            metodo = "transferencia/valida_articulo3"

        }




        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", dep)
            .add("COD_DIRECCION", direccion)
            .build()

        var result = HttpRequest.call("", metodo, formBody)

        articuloTransferencia = ArrayList()
        posicionArticulo = 0



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (validaArticulo) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {



            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas[i] as JSONObject
                var at = ArticuloTransferencia()

                at.codArticulo = filaObject.get("COD_ARTICULO").toString()
                at.cantidad = filaObject.get("CANT_DISP").toString()

                articuloTransferencia.add(at)

            }

        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaArticulo)")
        }

    }


    fun dialogoLeeCodigo() {

        try {
            dialogo_lee_codigo.dismiss()
        } catch (e: Exception) {

        }

        dialogo_lee_codigo = Dialog(context)
        dialogo_lee_codigo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_lee_codigo.setContentView(R.layout.lector_codigo_articulo_reabast_normal)

        dialogo_lee_codigo.btnAceptar.text = "Aceptar"
        dialogo_lee_codigo.btnAceptar.setOnClickListener {
            cod_articulo = dialogo_lee_codigo.etCodigoBarra.text.toString().trim().replace("\n", "").replace("A", "")
            operacion_validacion()
        }


        dialogo_lee_codigo.btn_volver.setOnClickListener {
            dialogo_lee_codigo.dismiss()
            definirFoco(spDepositoOri, etOrigen)
            definirFoco(spDepositoDes, etDestino)
        }

        dialogo_lee_codigo.etCodigoBarra.setOnFocusChangeListener { view, _ ->

            if (!view.hasFocus()) {

                cod_articulo = dialogo_lee_codigo.etCodigoBarra.text.toString()
                operacion_validacion()

            }

        }

        dialogo_lee_codigo.etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n") > -1) {

                    dialogo_lee_codigo.etCodigoBarra.setText(dialogo_lee_codigo.etCodigoBarra.text.toString().replace("\n", ""))
                    cod_articulo = dialogo_lee_codigo.etCodigoBarra.text.toString()
                    operacion_validacion()

                }



            }
        })

        dialogo_lee_codigo.setCancelable(false)
        dialogo_lee_codigo.show()

    }


    fun operacion_validacion() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", dialogo_lee_codigo.etCodigoBarra.text.toString().replace("\n", ""))
            .build()

        var result = HttpRequest.call("", "transferencia/consulta_articulo2", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_articulo2) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {



            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas[i] as JSONObject

                for (i2 in 0 until articuloTransferencia.size) {

                    if (filaObject.get("COD_ARTICULO").toString() == articuloTransferencia[i2].codArticulo) {

                        articulo_validado = true
                        cod_articulo_val = articuloTransferencia[i2].codArticulo
                        cod_articulo = filaObject.get("COD_ARTICULO").toString()
                        esPesable = filaObject.get("ES_PESABLE").toString()


                        cantidad_disponible = articuloTransferencia[i2].cantidad
                        art_adicional = filaObject.get("ART_ADICIONAL").toString()

                    } else {

                        if(etDestino.text.toString().indexOf("GRA000000") != -1 && operacion_iniciada == 1) {

                            articulo_validado = true

                        } else {

                            if (cod_articulo_val.isEmpty()) {

                                articulo_validado = false

                            }

                        }

                    }

                }

            }

            if(articulo_validado){

                if(operacion_iniciada == 0) {
                    registrarCabecera()
                    cargaCabecera()
                    insertaCabecera()
                    operacion_iniciada = 1
                    if (spDepositoDes.selectedItemId.toInt() == 0) {
                        validarDestino()
                        if (articulo_validado) {
                            insertaDetalletTransferencia()
                            operacion_iniciada=1
                        }
                    }
                    else {
                        insertaDetalletTransferencia()
                        operacion_iniciada=1
                    }

                } else{
                    if (spDepositoDes.selectedItemId.toInt() == 0 ) {
                        if (validarDestino()) {
                            insertaDetalletTransferencia()
                            operacion_iniciada=1
                        }
                    }else{
                        if (articulo_validado) {
                            insertaDetalletTransferencia()
                            operacion_iniciada=1
                        }
                    }
                }


            } else{

                MainActivity.funciones.mensajeError(context, "Atencion", "Articulo no corresponde")

                if (spDepositoOri.selectedItemId.toInt() == 0) {
                    etOrigen.setText("")
                    etOrigen.requestFocus()
                }
                else if(spDepositoDes.selectedItemId.toInt() == 0){
                    etDestino.setText("")
                    etDestino.requestFocus()
                }
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

            dialogo_lee_codigo.dismiss()


        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (operacion_validacion)")
        }



    }


    fun registrarCabecera() {


        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DEPOSITO", spDepositoOri.selectedItem.toString().split("-")[0].trim().replace("\n", ""))
            .build()

        var result = HttpRequest.call("", "transferencia/registrar_cabecera", formBody)


    }


    fun seleccionarCausa(direcciones: ArrayList<String>) {

        try {
            dialogo_selecciona_causa.dismiss()
        } catch (e: Exception) {
        }

        dialogo_selecciona_causa = Dialog(context)
        dialogo_selecciona_causa.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_selecciona_causa.setContentView(R.layout.transferencia_selecciona_motivo)

        cargarDirecciones(direcciones, dialogo_selecciona_causa.spDirecciones)

        dialogo_selecciona_causa.btnAceptar.setOnClickListener {

            codigoCausa = dialogo_selecciona_causa.spDirecciones.selectedItem.toString().split("-")[0].trim()
            dialogo_selecciona_causa.dismiss()

        }

        dialogo_selecciona_causa.btn_volver.setOnClickListener { }

        if ((spDepositoOri.selectedItem.toString().indexOf("VERDE")>-1  && MainActivity.usuarioLogin.codEmpresa != "2") ||
            (spDepositoOri.selectedItem.toString().split("-")[0].trim().equals("2012") && MainActivity.usuarioLogin.codEmpresa != "2")) {
            contador++
        }
        if (detalleTransferencia.size > 0) {
            contador = 0
        }
        dialogo_selecciona_causa.setCancelable(false)
        dialogo_selecciona_causa.show()


    }


    fun buscarMotivos() : ArrayList<String> {


        var metodo = "transferencia/buscar_motivo1"

        if (spDepositoOri.selectedItem.toString().split("-")[0].trim() == "100" ||
            spDepositoOri.selectedItem.toString().split("-")[0].trim() == "034"
        ) {

            metodo = "transferencia/buscar_motivo2"

        }




        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .build()

        var result = HttpRequest.call("", metodo, formBody)

        val listaCausas: ArrayList<String> = ArrayList()


        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (buscarMotivos) Error ${e.message.toString()} !")
            return listaCausas
        }

        if (respuestaJson.has("rows")) {



            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas[i] as JSONObject

                listaCausas.add("${filaObject.get("COD_CAUSA")} - ${filaObject.get("DESCRIPCION")}")


            }

        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaArticulo)")
        }

        return listaCausas


    }


    fun buscarEnLista(codigoArticuloValidado: String) {

        val direccionesConfirmacion: ArrayList<String> = ArrayList()
        val ordenConfirmacion: ArrayList<String> = ArrayList()
        val positionConfirmacion: ArrayList<Int> = ArrayList()
        for (i in 0 until detalleTransferencia.size) {
            if (detalleTransferencia[i].codArticulo == codigoArticuloValidado) {
                direccionesConfirmacion.add(detalleTransferencia[i].codDireccionDes)
                ordenConfirmacion.add(detalleTransferencia[i].nroOrden)
                positionConfirmacion.add(i)
            }
        }
        if (direccionesConfirmacion.size == 0) {
            etDestino.setText("")
            MainActivity.funciones.mensajeError(context, "Atencion", "El articulo no esta pendiente en esta transferencia.")
        } else if (direccionesConfirmacion.size == 1) {
            posicionDetalleTransferencia = positionConfirmacion[0]
            dialogoLeeDireccion()
            etDestino.setText("")
        } else if (direccionesConfirmacion.size > 1) {
            seleccionarDireccion(direccionesConfirmacion, etDestino, positionConfirmacion)
        }

    }

    fun seleccionarDireccion(direcciones: ArrayList<String>, et: EditText, positionConfirmacion: ArrayList<Int>?) {

        try {
            dialogo_selecciona_direccion.dismiss()
        } catch (e: Exception) {
        }

        dialogo_selecciona_direccion = Dialog(context)
        dialogo_selecciona_direccion.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_selecciona_direccion.setContentView(R.layout.transferencia_selecciona_direccion)

        cargarDirecciones(direcciones, dialogo_selecciona_direccion.spDirecciones)

        dialogo_selecciona_direccion.btnAceptar.setOnClickListener {

            if (validarCabeceraProceso()) {
                posicionDetalleTransferencia = positionConfirmacion!![dialogo_selecciona_direccion.spDirecciones.selectedItemPosition]
                dialogoLeeDireccion()
                et.setText("")
                et.requestFocus()
                dialogo_selecciona_direccion.dismiss()
            }else{
                et.setText(dialogo_selecciona_direccion.spDirecciones.selectedItem.toString().replace("-", "").substring(0,9).trim())
                dialogo_selecciona_direccion.dismiss()
            }

        }

        dialogo_selecciona_direccion.setCancelable(false)
        dialogo_selecciona_direccion.show()


    }


    fun eliminarDetalle() {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("TIP_COMPROBANTE", cabecera.tipComprobante)
            .add("SER_COMPROBANTE", cabecera.serComprobante)
            .add("NRO_COMPROBANTE", cabecera.nroComprobante)
            .build()

        var result = HttpRequest.call("", "transferencia/eliminar_detalle", formBody)

    }




    fun validarDestino() : Boolean {

        if (spDepositoDes.selectedItemId.toInt() === 0) {
            articulo_validado = false
            validaArticuloDestino(etDestino.text.toString().trim(), spDepositoDes)
            operacion_validacion_destino(articuloTransferencia)
            if (!articulo_validado) {
                MainActivity.funciones.mensajeError(context, "Atencion", "El articulo no corresponde a la dirección de destino.")
                etDestino.setText("")
            }
        }
        return articulo_validado

    }


    fun insertaDetalletTransferencia() {

        try {
            dialogo_inserta_cantidad.dismiss()
        } catch (e: Exception) {
        }


        dialogo_inserta_cantidad = Dialog(context)
        dialogo_inserta_cantidad.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_inserta_cantidad.setContentView(R.layout.transferencia_ingresa_cant)


        var total = cantidad_disponible.toDouble()
        var insertado = calcularCantidad(cod_articulo)

        var dispo = total - insertado

        cantidad_disponible = dispo.toString()

        dialogo_inserta_cantidad.tvCantidadDisp.text = "Cantidad disponible: $cantidad_disponible unidades"

        FuncionesUtiles.limitarDecimales(MainActivity.maximoDecimales, dialogo_inserta_cantidad.etCodigoBarra)

        if (esPesable == "S") {
            dialogo_inserta_cantidad.etCodigoBarra.inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        } else {
            dialogo_inserta_cantidad.etCodigoBarra.inputType = (InputType.TYPE_CLASS_NUMBER)
        }

        cargaUnidadMedida()

        dialogo_inserta_cantidad.spDirecciones.setSelection(0)

        dialogo_inserta_cantidad.btnAceptar.setOnClickListener {

            if (verificaTotal(dialogo_inserta_cantidad.etCodigoBarra.text.toString())) {

                var ingresado = dialogo_inserta_cantidad.etCodigoBarra.text.toString().toDouble()
                var multiploUm = umTransferencia[posicionUM].mult.toDouble()

                var _ingres = ingresado * multiploUm
                cantidad_detalle = _ingres
                registrarDetalles()
                cargaTransferenciaDetalle()
                cargarLista(detalleTransferencia)
                dialogo_inserta_cantidad.dismiss()
            }

        }


        dialogo_inserta_cantidad.btn_volver.setOnClickListener {

            dialogo_inserta_cantidad.dismiss()
            definirFoco(spDepositoOri, etOrigen)
            definirFoco(spDepositoDes, etDestino)

        }

        if (cantidad_disponible.toDouble() <= 0) {

            MainActivity.funciones.mensajeError(context, "Stock agotado", "Ya no quedan artículos para transferir en esta dirección.")

            if (spDepositoDes.selectedItem.toString().lowercase(Locale.getDefault()).indexOf("verde") > -1){
                etDestino.setText("")
            }else if (spDepositoOri.selectedItem.toString().lowercase(Locale.getDefault()).indexOf("verde") > -1){
                etOrigen.setText("")
            }
        }else{
            dialogo_inserta_cantidad.setCancelable(false)
            dialogo_inserta_cantidad.show()
            dialogo_inserta_cantidad.etCodigoBarra.requestFocus()
            dialogo_inserta_cantidad.etCodigoBarra.selectAll()
        }


    }


    fun cargarDirecciones(direcciones: ArrayList<String>, spinner: Spinner) {

        val spinnerAdapter : ArrayAdapter<String> =
            ArrayAdapter(
                context, R.layout.spinner_adapter,
                direcciones
            )
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter



        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                posicionUM = position

            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

    }


    fun dialogoLeeDireccion() {

        try {
            dialogo_lee_direccion.dismiss()
        } catch (e: Exception) {
        }

        dialogo_lee_direccion = Dialog(context)
        dialogo_lee_direccion.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo_lee_direccion.setContentView(R.layout.lector_codigo_direccion_transferencia)

        dialogo_lee_direccion.btnAceptar.text = "Aceptar"
        dialogo_lee_direccion.btnAceptar.setOnClickListener {
            dialogo_lee_direccion.dismiss()
        }

        dialogo_lee_direccion.btn_volver.setOnClickListener {

            dialogo_lee_direccion.dismiss()
            definirFoco(spDepositoOri, etOrigen)
            definirFoco(spDepositoDes, etDestino)

        }


        dialogo_lee_direccion.etCodigoBarra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (validarCampoTexto(dialogo_lee_direccion.etCodigoBarra)) {
                    if (validarCabecera()/*punto clave*/) {
                        if (dialogo_lee_direccion.etCodigoBarra.text.toString().trim().replace("\n", "") == detalleTransferencia[posicionDetalleTransferencia].codDireccionDes) {
                            confirmarDetalle(posicionDetalleTransferencia)
                            eliminarItem2(posicionDetalleTransferencia)
                            MainActivity.funciones.mensajeExito(context, "Exito!", "Operación exitosa")
                            if (detalleTransferencia.size == 0) {
                                limpiarCabecera()
                                MainActivity.funciones.mensajeExito(context, "Transferencia finalizada", "La operación ha finalizado exitosamente.")
                            }
                            dialogo_lee_direccion.dismiss()
                        } else {
                            MainActivity.funciones.mensajeError(context, "Error!", "La dirección ingresada no corresponde al destino.")
                            dialogo_lee_direccion.etCodigoBarra.setText("")
                        }
                    }else {
                        MainActivity.funciones.mensajeError(context, "Error!", "La cabecera no ha sido confirmada.")
                    }
                }

            }
            override fun afterTextChanged(s: Editable?) { }
        })


        dialogo_lee_direccion.setCancelable(false)
        dialogo_lee_direccion.show()




    }

    fun validaArticuloDestino(articulo_direccion: String, spinner: Spinner) {

        cod_articulo_val = ""
        articulo_validado = false




        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_DIRECCION", articulo_direccion)
            .build()

        var result = HttpRequest.call("", "transferencia/valida_articulo_destino", formBody)

        articuloTransferencia = ArrayList()
        posicionArticulo = 0



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (valida_articulo_destino) Error ${e.message.toString()} !")
            return
        }


        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas[i] as JSONObject
                var at = ArticuloTransferencia()

                at.codArticulo = filaObject.get("COD_ARTICULO").toString()
                at.cantidad = filaObject.get("CANT_DISP").toString()

                articuloTransferencia.add(at)

            }

        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (validaArticulo)")
        }

    }


    fun operacion_validacion_destino(items: ArrayList<ArticuloTransferencia>) {



        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", cod_articulo)
            .build()

        var result = HttpRequest.call("", "transferencia/consulta_articulo2", formBody)



        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_articulo2) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {

            val filas : JSONArray = (respuestaJson.get("rows") as JSONArray)
            for (i in 0 until filas.length()) {
                val filaObject : JSONObject = filas[i] as JSONObject

                for (i2 in 0 until items.size) {

                    if (filaObject.get("COD_ARTICULO").toString() == items[i2].codArticulo) {

                        articulo_validado = true
                        cod_articulo_val = items[i2].codArticulo

                    } else {

                        if(etDestino.text.toString().indexOf("GRA000000") != -1 && operacion_iniciada == 1) {

                            articulo_validado = true

                        } else {

                            if (cod_articulo_val.isEmpty()) {

                                articulo_validado = false

                            }

                        }

                    }

                }

            }

        }  else {
            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (operacion_validacion_destino)")
        }



    }


    fun calcularCantidad(articulo: String?): Double {
        var insertado = 0.0
        for (i in 0 until detalleTransferencia.size) {
            if (detalleTransferencia[i].codArticulo == articulo &&
                detalleTransferencia[i].codDireccion == etOrigen.text.toString()
            ) {
                insertado += detalleTransferencia[i].cantidad.toDouble()
            }
        }
        return insertado
    }


    fun cargaUnidadMedida() {

        var formBody: RequestBody = FormBody.Builder()
            .add("USER", MainActivity.usuarioLogin.codUsuario)
            .add("PASS", MainActivity.usuarioLogin.password)
            .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
            .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
            .add("COD_ARTICULO", cod_articulo)
            .build()

        var result = HttpRequest.call("", "transferencia/consulta_unidad_medida", formBody)


        posicionUM = 0
        posicionUMBasico = 0
        umTransferencia = ArrayList()

        var respuestaJson: JSONObject

        try {
            respuestaJson = JSONObject(result)
        } catch (e: Exception) {
            MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (consulta_unidad_medida) Error ${e.message.toString()} !")
            return
        }

        if (respuestaJson.has("rows")) {
            val filasArray : JSONArray = (respuestaJson.get("rows") as JSONArray)


            for (i in 0 until filasArray.length()) {
                val filaObject : JSONObject = filasArray.get(i) as JSONObject
                val um = UnidadMedidaTransferencia()
                um.codUnidadRel = filaObject.get("COD_UNIDAD_REL").toString()
                um.referencia = filaObject.get("REFERENCIA").toString()
                um.mult = filaObject.get("MULT").toString()
                um.indBasico = filaObject.get("IND_BASICO").toString()
                um.lastro = filaObject.get("LASTRO").toString()
                um.capas = filaObject.get("CAPAS").toString()

                umTransferencia.add(um)


                if (um.indBasico == "S") {
                    posicionUMBasico = i
                }

            }

            val spinnerAdapter : ArrayAdapter<UnidadMedidaTransferencia> =
                ArrayAdapter(
                    context, R.layout.spinner_adapter,
                    umTransferencia
                )
            spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            dialogo_inserta_cantidad.spDirecciones.adapter = spinnerAdapter



            dialogo_inserta_cantidad.spDirecciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    posicionUM = position

                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }

            if (umTransferencia.size != 0) {
                dialogo_inserta_cantidad.spDirecciones.setSelection(
                    posicionUMBasico
                )
            }


        } else {

            MainActivity.funciones.mensajeError(context, "Atencion", "Ocurrio un error en la respuesta del servidor. (cargaUnidadMedida)")


        }

    }


    fun verificaTotal(ingresado: String) : Boolean {

        val _ingres: Double = ingresado.toDouble() * umTransferencia[posicionUM].mult.toDouble()

        return if (ingresado.isNotEmpty() && _ingres > 0) {
            if (cantidad_disponible.toDouble() >= cantidad_transferida.toDouble() + _ingres) {
                true
            } else {
                MainActivity.funciones.mensajeError(context, "Atencion", "CANTIDAD TOTAL SOBREPASA LO DISPONIBLE EN DIRECCION")
                false
            }
        } else {
            MainActivity.funciones.mensajeError(context, "Atencion", "DEBE INGRESAR CANTIDAD MAYOR A 0")
            false
        }

    }

    fun registrarDetalles() {



        var direccionDes = etDestino.text.toString().replace("\n", "")
        var direccionOri = etOrigen.text.toString().replace("\n", "")

        if (direccionDes.equals("Sin dirección")) {
            direccionDes = ""
        }

        if (direccionOri.equals("Sin dirección")) {
            direccionOri = ""
        }

        if (operacion_iniciada==1 && cabecera.nroComprobante != "") {



            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("COD_DEPOSITO_ORIGEN", spDepositoOri.selectedItem.toString().split("-")[0].trim().replace("\n", ""))
                .add("COD_DEPOSITO_DESTINO", spDepositoDes.selectedItem.toString().split("-")[0].trim().replace("\n", ""))
                .add("TIP_COMPROBANTE", "ENV")
                .add("SER_COMPROBANTE", "A")
                .add("NRO_COMPROBANTE", cabecera.nroComprobante)
                .add("COD_DIRECCION", direccionOri)
                .add("COD_DIRECCION_DES", direccionDes)
                .add("COD_ARTICULO", cod_articulo)
                .add("COD_UNIDAD_MEDIDA", "01")
                .add("CANTIDAD", cantidad_detalle.toString().trim().replace(".", ","))
                .add("FEC_VENCIMIENTO", "31/12/2099")
                .add("COD_CAUSA", codigoCausa)
                .build()

            var result = HttpRequest.call("", "transferencia/inserta_transferencia_det", formBody)
            var mensaje = ""

            var respuestaJson: JSONObject

            try {
                respuestaJson = JSONObject(result)
            } catch (e: Exception) {
                MainActivity.funciones.mensajeError(context, "Atencion", "Error en la respuesta del servidor. (inserta_transferencia_det) Error ${e.message.toString()} !")
                return
            }


            if (respuestaJson.has("respuesta")) {

                val respuesta = respuestaJson.get("respuesta").toString()

                val res: List<String> = respuesta.split("*")

                if (res[0] == "01") {
                    mensaje = "REGISTRADO CON EXITO"
                } else {
                    mensaje = result
                }

            } else {
                mensaje = result
            }




            inicio = true


            if(!mensaje.equals("REGISTRADO CON EXITO")){

                SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Atencion!")
                    .setContentText(mensaje)
                    .setConfirmText("OK")
                    .setConfirmClickListener { sDialog ->

                        sDialog.dismissWithAnimation()

                    }
                    .setCancelButton(
                        "No"
                    ) { sDialog -> sDialog.dismissWithAnimation() }
                    .show()

            }
        }


    }




}