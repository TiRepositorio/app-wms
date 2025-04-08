package com.apolo.wms.clases.separacion

class UnidadMedidaArticuloSeparacion {


    var codUnidadRel : String = ""

    var referencia : String = ""

    var indBasico : String = ""

    var lastro : String = ""

    var capas : String = ""


    override fun toString(): String {
        return "$codUnidadRel - $referencia"
    }

}