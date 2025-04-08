package com.apolo.wms.utilidades

class Sentencias {


    private var _sql: String = ""

    fun createTableWms_configuraciones(): String {
        _sql = ("CREATE TABLE IF NOT EXISTS wms_configuraciones"
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, COD_EMPRESA  TEXT, COD_SUCURSAL TEXT);")
        return _sql
    }


    fun createTableWms_transferencias_manuales(): String {
        _sql = ("CREATE TABLE IF NOT EXISTS wms_transferencias_manuales "
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT	, "
                + " COD_EMPRESA  TEXT		, COD_SUCURSAL TEXT 	, COD_DIRECCION_ORIGEN TEXT		, "
                + " NRO_COMPROBANTE TEXT	, ESTADO TEXT			, TIP_COMPROBANTE TEXT			, "
                + " SER_COMPROBANTE TEXT	, COD_DEPOSITO TEXT 	, COD_ARTICULO TEXT				, "
                + " NRO_LOTE TEXT			, FEC_VENCIMIENTO TEXT	, COD_UNIDAD_MED TEXT			, "
                + " CANTIDAD TEXT         	, TIPO TEXT				, TIP_OPERACION_REABAST TEXT DEFAULT '',"
                + " ESTADO_D TEXT DEFAULT 'P');")
        return _sql
    }

    fun createTableWms_almacenamientos_iniciados(): String {
        _sql = ("CREATE TABLE IF NOT EXISTS wms_almacenamientos_iniciados "
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, COD_EMPRESA  TEXT, COD_SUCURSAL TEXT, COD_JAULA TEXT, "
                + "  NRO_COMPROBANTE TEXT, TIP_COMPROBANTE TEXT, SER_COMPROBANTE TEXT);")
        return _sql
    }

    fun createTableWms_conferencias_iniciadas(): String {
        _sql = ("CREATE TABLE IF NOT EXISTS wms_conferencias_iniciadas "
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, COD_EMPRESA  TEXT, COD_SUCURSAL TEXT, COD_JAULA TEXT, "
                + "  NRO_COMPROBANTE TEXT, TIP_COMPROBANTE TEXT, SER_COMPROBANTE TEXT);")
        return _sql
    }

    // solo este se utiliza //
    fun createTableWms_Separadores(): String {
        _sql = ("CREATE TABLE IF NOT EXISTS wms_separadores "
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, COD_SEPARADOR  TEXT, NOMBRE TEXT);")
        return _sql
    }

    fun createTableWms_transferencias_dep(): String {
        _sql = ("CREATE TABLE IF NOT EXISTS wms_transferencias_dep "
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT	, "
                + " COD_EMPRESA  TEXT		, COD_SUCURSAL TEXT 	, COD_DIRECCION_ORIGEN TEXT		, "
                + " NRO_COMPROBANTE TEXT	, ESTADO TEXT			, TIP_COMPROBANTE TEXT			, "
                + " SER_COMPROBANTE TEXT	, COD_DEPOSITO TEXT 	, COD_ARTICULO TEXT				, "
                + " NRO_LOTE TEXT			, FEC_VENCIMIENTO TEXT	, COD_UNIDAD_MED TEXT			, "
                + " CANTIDAD TEXT         	, TIPO TEXT				, TIP_OPERACION_REABAST TEXT DEFAULT '');")
        return _sql
    }

    fun createTableWms_inventario_018(): String {
        _sql = ("CREATE TABLE IF NOT EXISTS wms_inventario_018 "
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT	, "
                + " NRO_ORDEN  TEXT		, DESC_ARTICULO TEXT 	, CANTIDAD TEXT		, "
                + " ESTADO TEXT			 );")
        return _sql
    }

    fun createTableWms_reabastecimiento_drive(): String {
        _sql = ("CREATE TABLE IF NOT EXISTS wms_reabastecimiento_drive_detalle "
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT	, "
                + " COD_DIRECCION 	TEXT		, CANTIDAD TEXT			, ESTADO TEXT DEFAULT 'P',"
                + " NRO_ORDEN 		INTEGER);")
        return _sql
    }

    fun createTableWmsUsuarioSucursal(): String {
        _sql = ("CREATE TABLE IF NOT EXISTS wms_usuario_sucursal "
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT	, "
                + " COD_EMPLEADO 	TEXT		, COD_SUCURSAL TEXT			, COD_EMPRESA TEXT ,"
                + " COD_USUARIO     TEXT);")
        return _sql
    }

}