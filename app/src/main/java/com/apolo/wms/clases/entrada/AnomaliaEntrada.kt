package com.apolo.wms.clases.entrada

class AnomaliaEntrada {

    var codEmpresa : String = ""

    var codMotivo : String = ""

    var descMotivo : String = ""

    var afectaStock : String = ""

    override fun toString(): String {
        return "$codMotivo - $descMotivo"
    }

}