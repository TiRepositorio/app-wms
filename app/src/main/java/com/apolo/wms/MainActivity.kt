package com.apolo.wms

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.apolo.wms.clases.UsuarioLogin
import com.apolo.wms.utilidades.Constantes
import com.apolo.wms.utilidades.Funciones
import com.apolo.wms.utilidades.HttpRequest
import com.apolo.wms2.R
import jcifs.smb1.smb1.SmbFile
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {


/*
    public static Connection conn = null;
    static Statement stm2 = null;
    ResultSet rs2 = null;

    EditText codigo, descripcion, cantidad;
    Spinner sp_um;

    EditText etUsuario, etClave;
    static TextView txtVersion;
    TextView tvIP, tvNumero;

    Button btnLogin, btnCancelar, btnActualizar;

    public static Parametros parametrosConexion = new Parametros();
    public static ProcedimientosBD procedimientosBD = new ProcedimientosBD();

    static String errorConexion = "";

    public static SQLiteDatabase bdatos = null;

    public static String cod_empleado = "";
    public static List<String> lcod_empleado = new ArrayList<String>();
    public static String ind_maneja_lote_equipo = "N";
    public static String lote_defecto = "0";
    public static String fec_vencimiento_defecto = "31/12/2099";
    public static String myEquip;
    public static String myCode;
    public static boolean isPrueba;

    /*variables acutalizardor*/
    static Dialog dialogDescargarInstalador = null;
    static ProgressBar progressBar;
    static double arx;
    long urx;
    static AsyncTask<Void, Void, Void> a3;
    static AsyncTask<Void, Void, Void> a4;
    MyProgressDialog pdia;
    boolean status = false;
    static int uid= android.os.Process.myUid();
    static String _version;
    static String _fechaVersion;
    HashMap<String, String> datos;
    static String idTelefono = "";
    public static boolean inventario = false;

 */





    companion object{

        lateinit var etUser : EditText
        lateinit var etPassword : EditText

        lateinit var context : Context
        lateinit var activity: Activity


        var respuesta:String = ""
        var usuarioValido = false
        var dispositivoPermitido = false
        var mensajeRespuesta = ""
        var idTelefono = ""
        var uuid = ""
        var funciones = Funciones()
        var maximoDecimales = 3

        lateinit var usuarioLogin : UsuarioLogin

        const val _version : String = "53"
        const val _fechaVersion : String = "20240319"
        const val _versionDelDia : String = "1"

        var lote_defecto = "0"
        var fec_vencimiento_defecto = "31/12/2099"


        fun abrirInstalador(){
            try {
                val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val archivo = File(storageDir,Constantes.archivoInstaldor)
                archivo.createNewFile()

                funciones.verifyStoragePermissions(activity, archivo.absolutePath)
            } catch (e : Exception){
                funciones.mensaje(activity,"",e.message.toString())
            }
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        context = this
        activity = this

        inicializar()


    }

    private fun inicializar() {
        /*GENERAR ID UNICO SI ES LA PRIMERA VEZ*/
        generarIdUnico()
        idTelefono  = funciones.getModelo(this, uuid);

        etUser = etUsuario
        etPassword = etClave

        val miVer = "v.$_version.$_fechaVersion"
        tvVersion.text = miVer
        tvIP.text = "Dirección: " + funciones.getIP()

        //cargar iconos
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_launcher5);

        btnCancelar.setOnClickListener { finish() }
        btnActualizar.setOnClickListener { descargarActualizacion() }
        btnLogin.setOnClickListener{ iniciarSesion() }


    }

    private fun generarIdUnico() {

        val settings = getSharedPreferences("CONFIGURACION", 0)
        val silent = settings.getString("UUID", "")

        uuid = silent.toString()

        if (silent == "") {
            //SE GENERA UN IDENTIFICADOR UNICO LA PRIMERA VEZ
            val id = UUID.randomUUID().toString()
            uuid = id
            val editor = settings.edit()
            editor.putString("UUID", id)
            editor.commit()
        }



    }


    private class DescargarActualizacion() : AsyncTask<Void?, Void?, Void?>() {
        var dialogo : ProgressDialog? = null
        override fun onPreExecute() {
            dialogo = ProgressDialog.show(context,"Un momento...","Descargando Instalador",true)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            return try {

                val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val archivo = File(storageDir,Constantes.archivoInstaldor)
                archivo.createNewFile()

                val source = SmbFile(Constantes.archivoInstaladorServidor)
                //val destination = File(Constantes.rutaLocalInstalador, Constantes.archivoInstaldor)
                val destination = archivo.absolutePath
                val `in`: InputStream = source.inputStream
                val out: OutputStream = FileOutputStream(destination)

                // Copy the bits from Instream to Outstream
                val buf = ByteArray(10240)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                // Maybe in.close();
                out.close()


                null
            } catch (e: Exception) {
                respuesta = e.message.toString()
                null
            }
        }

        override fun onPostExecute(unused: Void?) {
            try {
                dialogo!!.dismiss()
            } catch (e: Exception) {
            }

            abrirInstalador()

            return
        }

    }


    private class IniciarSesion : AsyncTask<Void?, Void?, Void?>() {
        var dialogo : ProgressDialog? = null
        override fun onPreExecute() {
            dialogo = ProgressDialog.show(context,"Un momento...","Consultando",true)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            return try {

                val user = etUser.text.toString().uppercase(Locale.getDefault())
                val pass = etPassword.text.toString().uppercase(Locale.getDefault())


                usuarioValido = false

                var metodo = "login/consulta_login"
                var formBody: RequestBody = FormBody.Builder()
                    .add("USER", user)
                    .add("PASS", pass)
                    .build()
                respuesta = HttpRequest.call("", metodo, formBody).toString()

                var respuestaJson = JSONObject(respuesta)

                if (respuestaJson.has("rows")) {

                    val usuarioArray : JSONObject = (respuestaJson.get("rows") as JSONArray).get(0) as JSONObject

                    usuarioValido = true

                    usuarioLogin = UsuarioLogin()
                    usuarioLogin.codUsuario = user
                    usuarioLogin.password = pass
                    usuarioLogin.codEmpleado = usuarioArray.get("COD_EMPLEADO").toString()
                    usuarioLogin.codSucursal = usuarioArray.get("COD_SUCURSAL").toString()
                    usuarioLogin.codEmpresa = usuarioArray.get("COD_EMPRESA").toString()
                    usuarioLogin.codDeposito = usuarioArray.get("COD_DEPOSITO").toString()
                    usuarioLogin.descEmpresa = usuarioArray.get("DESC_EMPRESA").toString()
                    usuarioLogin.descSucursal = usuarioArray.get("DESC_SUCURSAL").toString()
                    usuarioLogin.cambiaSucursal = usuarioArray.get("CAMBIA_SUCURSAL").toString()
                    usuarioLogin.codPersona = usuarioArray.get("COD_PERSONA").toString()
                    usuarioLogin.permisos = usuarioArray.get("PERMISOS").toString()



                    metodo = "login/verificar_dispositivo"
                    formBody = FormBody.Builder()
                        .add("USER", user)
                        .add("PASS", pass)
                        .add("IMEI", idTelefono)
                        .add("IP", funciones.getIP())
                        .add("VERSION", "v.$_version.$_fechaVersion")
                        .build()
                    respuesta = HttpRequest.call("", metodo, formBody).toString()

                    respuestaJson = JSONObject(respuesta)

                    dispositivoPermitido = respuestaJson.get("permitido") != 0
                    mensajeRespuesta = respuestaJson.get("mensaje").toString()

                }

                null
            } catch (e: Exception) {
                respuesta = e.message.toString()
                null
            }
        }

        override fun onPostExecute(unused: Void?) {
            try {
                dialogo!!.dismiss()
            } catch (e: Exception) {
            }

            etPassword.setText("")

            if (dispositivoPermitido) {

                context.startActivity(Intent(context, Operaciones::class.java))

            } else {



                if (!usuarioValido) {
                    funciones.mensajeError(context, "Error!", "USUARIO O CONTRASEÑA INCORRECTO!")
                } else {
                    funciones.mensajeError(context, "Error!", mensajeRespuesta)
                }



            }

            return
        }

    }


    private class ConsultaInicial : AsyncTask<Void?, Void?, Void?>() {
        var dialogo : ProgressDialog? = null
        override fun onPreExecute() {
            dialogo = ProgressDialog.show(context,"Un momento...","Consultando",true)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            return try {

                val metodo = "login/valida_dispositivo"
                val formBody: RequestBody = FormBody.Builder().add("IMEI", "356074080243319").build()
                respuesta = HttpRequest.call("", metodo, formBody).toString()

                null
            } catch (e: Exception) {
                respuesta = e.message.toString()
                null
            }
        }

        override fun onPostExecute(unused: Void?) {
            try {
                dialogo!!.dismiss()
            } catch (e: Exception) {
            }
            val respuestaJson = JSONObject(respuesta)
            return
        }

    }


    fun descargarActualizacion() {

        DescargarActualizacion().execute()

    }


    fun iniciarSesion() {
        IniciarSesion().execute()
        //throw RuntimeException("Test Crash")

    }




}