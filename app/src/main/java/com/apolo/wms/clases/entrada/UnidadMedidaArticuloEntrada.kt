package com.apolo.wms.clases.entrada

class UnidadMedidaArticuloEntrada {


    var codUnidadRel : String = ""

    var referencia : String = ""

    var indBasico : String = ""

    var lastro : String = ""

    var capas : String = ""


    override fun toString(): String {
        return "$codUnidadRel - $referencia"
    }


}