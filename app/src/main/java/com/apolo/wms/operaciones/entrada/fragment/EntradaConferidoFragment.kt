package com.apolo.wms.operaciones.entrada.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apolo.wms.MainActivity
import com.apolo.wms.clases.entrada.ArticuloConferidoPlanillaEntrada
import com.apolo.wms.operaciones.entrada.BuscarPlanillaEntrada
import com.apolo.wms.operaciones.entrada.adapter.AdapterEntradaConferido
import com.apolo.wms.utilidades.CallableWS
import com.apolo.wms.utilidades.ExecutorRunner
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.entrada_conferido2.view.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject


class EntradaConferidoFragment : Fragment() {


    companion object {

        lateinit var context : Context
        lateinit var rvConferidos : RecyclerView

        var articuloConferidoPlanillaEntrada = ArrayList<ArticuloConferidoPlanillaEntrada>()


        fun obtieneArticulosConferidosPlanilla() {


            val executorRunner = ExecutorRunner()
            var formBody: RequestBody = FormBody.Builder()
                .add("USER", MainActivity.usuarioLogin.codUsuario)
                .add("PASS", MainActivity.usuarioLogin.password)
                .add("COD_EMPRESA", MainActivity.usuarioLogin.codEmpresa)
                .add("COD_SUCURSAL", MainActivity.usuarioLogin.codSucursal)
                .add("TIP_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.tipComprobante)
                .add("SER_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.serComprobante)
                .add("NRO_COMPROBANTE", BuscarPlanillaEntrada.planillaEntradaSeleccionada.nroComprobante)
                .build()

            executorRunner.execute(
                CallableWS("entrada/obtiene_articulos_conferidos_planilla", formBody),
                object : ExecutorRunner.Callback<String> {
                    override fun onComplete(result: String) { // handle the result obtained from the asynchronous task

                        articuloConferidoPlanillaEntrada = ArrayList()
                        var respuestaJson = JSONObject(result)
                        if (respuestaJson.has("rows")) {
                            val conferidoEntradaArray : JSONArray = (respuestaJson.get("rows") as JSONArray)
                            for (i in 0 until conferidoEntradaArray.length()) {
                                val conferidoEntradaObject : JSONObject = conferidoEntradaArray[i] as JSONObject
                                val conf = ArticuloConferidoPlanillaEntrada()
                                conf.codArticulo = conferidoEntradaObject.get("COD_ARTICULO").toString()
                                conf.descArticulo = conferidoEntradaObject.get("DESC_ARTICULO").toString()
                                conf.codUnidadMedida = conferidoEntradaObject.get("COD_UNIDAD_MEDIDA").toString()
                                conf.descUnidadMedida = conferidoEntradaObject.get("DESC_UNIDAD_MEDIDA").toString()
                                conf.cantidad = conferidoEntradaObject.get("CANTIDAD").toString()
                                conf.nroLote = conferidoEntradaObject.get("NRO_LOTE").toString()
                                conf.fecVencimiento = conferidoEntradaObject.get("FEC_VENCIMIENTO").toString()
                                conf.anomalia = conferidoEntradaObject.get("ANOMALIA").toString()
                                conf.nroOrden = conferidoEntradaObject.get("NRO_ORDEN").toString()
                                conf.deposito = conferidoEntradaObject.get("DEPOSITO").toString()
                                articuloConferidoPlanillaEntrada.add(conf)
                            }
                        }



                        val gridLayoutManager = GridLayoutManager(context, 1)

                        rvConferidos.setLayoutManager(gridLayoutManager)
                        rvConferidos.setItemAnimator(DefaultItemAnimator())
                        rvConferidos.setHasFixedSize(true)


                        // this creates a vertical layout Manager
                        rvConferidos.layoutManager = LinearLayoutManager(context)

                        // This loop will create 20 Views containing
                        // the image with the count of view
                        // This will pass the ArrayList to our Adapter
                        val adapter = AdapterEntradaConferido(context,
                            articuloConferidoPlanillaEntrada, R.layout.card_view_entrada_conferido  )

                        // Setting the Adapter with the recyclerview
                        rvConferidos.adapter = adapter


                        /*rvConferidos.addOnItemTouchListener(
                            RecyclerItemClickListener(context, object :
                                RecyclerItemClickListener.OnItemClickListener {
                                override fun onItemClick(v: View?, position: Int) {
                                    FuncionesUtiles.posicionCabecera = position
                                    adapter.notifyDataSetChanged()
                                }
                            })
                        )*/



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

        fun eliminaRecepcionMercaderia() {



        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.entrada_conferido2, container, false)

        Companion.rvConferidos = rootView.rvConferidos
        Companion.context = requireContext()

        return rootView
    }





}


