package com.apolo.wms.clases.transferencia

class UnidadMedidaTransferencia {

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