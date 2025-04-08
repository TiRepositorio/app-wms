package com.apolo.wms.clases.entrada

import com.apolo.wms.utilidades.ObtenerAtributoPorNombre

class ReconferenciaEntrada : ObtenerAtributoPorNombre {

    var codArticulo : String = ""

    var descArticulo : String = ""

    var codUnidadMedida : String = ""

    var descUnidadMedida : String = ""

    var cantidad : String = ""

    var nroLote : String = ""

    var fecVencimiento : String = ""

    var anomalia : String = ""

    var orden : String = ""

    override fun getFieldByString(atributo: String): String {
        return when (atributo) {
            "COD_ARTICULO" -> codArticulo
            "DESC_ARTICULO" -> descArticulo
            "codUnidadMedida" -> codUnidadMedida
            "DESC_UNIDAD_MEDIDA" -> descUnidadMedida
            "cantidad" -> cantidad
            "nroLote" -> nroLote
            "fecVencimiento" -> fecVencimiento
            "anomalia" -> anomalia
            "nroOrden" -> orden
            else -> codArticulo
        }
    }


}