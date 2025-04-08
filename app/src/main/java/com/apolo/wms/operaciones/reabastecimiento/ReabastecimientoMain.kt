package com.apolo.wms.operaciones.reabastecimiento

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apolo.wms.operaciones.entrada.BuscarPlanillaEntrada
import com.apolo.wms.operaciones.entrada.EntradaMercaderia
import com.apolo.wms2.R
import kotlinx.android.synthetic.main.activity_reabastecimiento.*
import java.util.*

class ReabastecimientoMain : AppCompatActivity() {



    companion object {
        lateinit var context : Context




    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reabastecimiento)

        inicializar()

    }


    fun inicializar() {

        context = this

        title = "Reabastecimiento".uppercase(Locale.getDefault())


        btnReabastecimientoVertical.setOnClickListener { abreReabastecimientoVertical() }

        btnReabastecimientoHorizontal.setOnClickListener { abreReabastecimientoHorizontal() }

    }


    fun abreReabastecimientoVertical() {


        //ReabastecimientoMarcaderiaDriveInNew::class
        val i = Intent(this, ReabastecimientoMarcaderiaDriveInNew::class.java)
        startActivity(i)


    }

    fun abreReabastecimientoHorizontal() {


        val i = Intent(this, ReabastecimientoMercaderiaHorizontal::class.java)
        startActivity(i)



    }


}