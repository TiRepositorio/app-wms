package com.apolo.wms.clases.entrada

class DepositoEntrada {


    var codDeposito : String = ""

    var descDeposito : String = ""

    var indDefecto : String = ""

    override fun toString(): String {
        return "$codDeposito - $descDeposito"
    }


}