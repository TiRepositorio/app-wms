package com.apolo.wms.clases.entrada

import com.apolo.wms.utilidades.ObtenerAtributoPorNombre

class ArticuloConferidoPlanillaEntrada : ObtenerAtributoPorNombre {

    var codArticulo : String = ""

    var descArticulo : String = ""

    var codUnidadMedida : String = ""

    var descUnidadMedida : String = ""

    var cantidad : String = ""

    var nroLote : String = ""

    var fecVencimiento : String = ""

    var anomalia : String = ""

    var nroOrden : String = ""

    var deposito : String = ""


    override fun getFieldByString(atributo: String): String {
        return when (atributo) {
            "COD_ARTICULO" -> codArticulo
            "DESC_ARTICULO" -> descArticulo
            "COD_UNIDAD_MEDIDA" -> codUnidadMedida
            "DESC_UNIDAD_MEDIDA" -> descUnidadMedida
            "CANTIDAD" -> cantidad
            "ANOMALIA" -> anomalia
            "VENCIMIENTO" -> fecVencimiento
            "DEPOSITO" -> deposito
            else -> codArticulo
        }
    }


}