package com.apolo.wms.utilidades

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit


class HttpRequest {


    companion object{
        private const val URL = "http://10.1.1.115:5000/wms/"
//        private const val URL = "http://192.168.0.18:5000/wms/"
          val JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8")
//        var client = OkHttpClient()



        fun call(metodo: String, url: String?, formBody: RequestBody): String {

            var client = OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS) // Tiempo máximo de espera para establecer la conexión
                .readTimeout(60, TimeUnit.SECONDS) // Tiempo máximo de espera para leer los datos
                .writeTimeout(60, TimeUnit.SECONDS) // Tiempo máximo de espera para escribir los datos
                .build()



            val request: Request = Request.Builder()
                .url(URL + url)
                .post(formBody)
                .build()





            try {
                val response = client.newCall(request).execute()
                return response!!.body()!!.string()
            } catch (e: Exception) {
                return "ERROR ${e.toString()}"
            }



        }


        fun post(metodo:String, json: String): String {
            val body: RequestBody = RequestBody.create(JSON, json)
            //val body: RequestBody = RequestBody.create(json, JSON) // new
            // RequestBody body = RequestBody.create(JSON, json); // old
            val request: Request = Request.Builder()
                .url(URL + metodo)
                .header("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoiSU5WIiwicGFzcyI6IkFQT0xPMjAyMiIsImlhdCI6MTY1NzI5NzU4MywiZXhwIjoxNjU3OTAyMzgzfQ.i-YNPGN8yAuS_HATsyNibT77raQefp5zHIMS97h4w40")
                .post(body)
                .build()

            var client = OkHttpClient()

            val response = client.newCall(request).execute()
            return response.body()!!.string()
        }
    }



    fun run(url: String) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
        })
    }
}