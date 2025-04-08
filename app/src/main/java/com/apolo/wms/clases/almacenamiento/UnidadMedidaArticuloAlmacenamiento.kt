package com.apolo.wms.clases.almacenamiento

class UnidadMedidaArticuloAlmacenamiento {

    var codUnidadRel : String = ""

    var referencia : String = ""

    var mult : String = ""

    var indBasico : String = ""

    var lastro : String = ""

    var capas : String = ""

    override fun toString(): String {
        return "$codUnidadRel - $referencia"
    }


}