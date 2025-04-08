package com.apolo.wms.utilidades

import okhttp3.RequestBody
import java.util.concurrent.Callable

class CallableWS (private val metodo: String,
                  private val formBody: RequestBody) : Callable<String> {

    override fun call(): String {

        return HttpRequest.call("", metodo, formBody)
    }

}