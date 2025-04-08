package com.apolo.wms.clases.almacenamiento

class VencimientoArticuloAlmacenamiento {

    var codArticulo : String = ""

    var codDireccion : String = ""

    var fecVencimiento : String = ""

    var cantDisponible : String = ""



    override fun toString(): String {
        return "$fecVencimiento"
    }


}