package com.apolo.wms.utilidades

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.MainActivity
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


class Funciones {

    private val requestExternalStorage = 1
    private val permissionsStorage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun getModelo(c: Context, uuid: String): String {

        var imei = ""
        val telephonyManager = c.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= 29) {
            imei = uuid
        } else {
            if (Build.VERSION.SDK_INT >= 26) {
                imei = telephonyManager.getImei()
            } else {
                imei = telephonyManager.getDeviceId()
            }
        }

        if (imei.trim { it <= ' ' }.equals("null", ignoreCase = true)) {
            imei = getMAC(c)
        }
        return imei
    }

    fun getMAC(c: Context): String {
        val manager = c.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        val info = manager!!.connectionInfo

        val permission = ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return info.macAddress.uppercase(Locale.getDefault())
        } else {
            return ""
        }
        //return info.macAddress.uppercase(Locale.getDefault())
    }

    fun getIP(): String? {
        val n: Enumeration<NetworkInterface>
        try {
            n = NetworkInterface.getNetworkInterfaces()
            while (n.hasMoreElements()) {
                val e: NetworkInterface = n.nextElement()
                val a: Enumeration<InetAddress> = e.getInetAddresses()
                while (a.hasMoreElements()) {
                    val addr: InetAddress = a.nextElement()
                    if (addr.toString().indexOf("192.168.") > -1) {
                        return addr.getHostAddress()
                    }
                }
            }
        } catch (e1: SocketException) {
            e1.printStackTrace()
        }
        return null
    }


    fun verifyStoragePermissions(activity: Activity, nombre: String) {
        // verifica si hay premiso para escribir en el almacenamiento
        val permission = ActivityCompat.checkSelfPermission( activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // solicita permiso para escribir en el almacenamiento interno
            abrir(activity, nombre)
        }
    }


    private fun abrir(activity: Activity, nombre: String){
        ActivityCompat.requestPermissions(activity,permissionsStorage,requestExternalStorage)
        val file = File(nombre)
        if (Build.VERSION.SDK_INT >= 24) {
            val fileUri = FileProvider.getUriForFile(activity,"com.apolo.wms2.fileprovider",file)
            val intent = Intent(Intent.ACTION_DEFAULT, fileUri)
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, false)
//                intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
            intent.data = fileUri
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            activity.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(intent)
        }
    }


    fun mensaje(context: Context, titulo: String, mensaje: String){
        val dialogo : AlertDialog.Builder = AlertDialog.Builder(context)
        dialogo.setTitle(titulo)
        dialogo.setMessage(mensaje)
        dialogo.setPositiveButton("OK", null)
        dialogo.show()
    }

    fun mensaje(titulo: String, mensaje: String){
        val dialogo : AlertDialog.Builder = AlertDialog.Builder(MainActivity.context)
        dialogo.setTitle(titulo)
        dialogo.setMessage(mensaje)
        dialogo.show()
    }


    fun mensajeError(context: Context, titulo: String, mensaje: String){
        SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .show()
    }


    fun mensajeExito(context: Context, titulo: String, mensaje: String){

        SweetAlertDialog(context)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .show();
    }







}