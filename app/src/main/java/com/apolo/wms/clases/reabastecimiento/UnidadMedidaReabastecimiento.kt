package com.apolo.wms.clases.reabastecimiento

class UnidadMedidaReabastecimiento {


    var codUnidadRel : String = ""

    var referencia : String = ""

    var indBasico : String = ""

    var lastro : String = ""

    var capas : String = ""

    var mult : String = ""


    override fun toString(): String {
        return "$codUnidadRel - $referencia"
    }

}