package com.apolo.wms.clases.entrada

import com.apolo.wms.utilidades.ObtenerAtributoPorNombre

class AnomaliaArticuloEntrada : ObtenerAtributoPorNombre {

    var codMotivo : String = ""

    var descMotivo : String = ""

    var tipo : String = ""


    override fun getFieldByString(atributo: String): String {
        return when (atributo) {
            "DESCRIPCION" -> descMotivo
            "TIPO" -> tipo
            else -> codMotivo
        }
    }

}