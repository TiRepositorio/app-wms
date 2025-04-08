package com.apolo.wms.clases.consulta.direccion

class UnidadMedidaDireccion {


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