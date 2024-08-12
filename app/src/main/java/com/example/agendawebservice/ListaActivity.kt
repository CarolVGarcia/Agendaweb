package com.example.agendawebservice

import android.app.Activity
import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Cache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.agendawebservice.Objetos.Contactos
import com.example.agendawebservice.Objetos.Device
import com.example.agendawebservice.Objetos.ProcesosPHP
import org.json.JSONException
import org.json.JSONObject
import java.io.File


class ListaActivity : ListActivity(), Response.Listener<JSONObject>, Response.ErrorListener {
    private lateinit var btnNuevo: Button
    private val context: Context = this
    private var php = ProcesosPHP()
    private lateinit var request: RequestQueue
    private lateinit var jsonObjectRequest: JsonObjectRequest
    private var listaContactos = ArrayList<Contactos>()
    private lateinit var adapter: MyArrayAdapter
    private val serverip = "http://practicascarolvg.online/WebService"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)
        listaContactos = ArrayList()
        adapter = MyArrayAdapter(context, R.layout.layout_contacto, listaContactos)
        listView.adapter = adapter
        request = Volley.newRequestQueue(context)

        btnNuevo = findViewById(R.id.btnNuevo)
        btnNuevo.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        clearApplicationCache()
    }

    override fun onResume() {
        super.onResume()
        consultarTodosWebService()
    }

    private fun createNoCacheRequest(
        method: Int,
        url: String,
        jsonRequest: JSONObject?,
        listener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener
    ): JsonObjectRequest {
        return object : JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
            override fun getCacheEntry(): Cache.Entry? {
                return null // Desactiva la caché
            }
        }
    }

    private fun consultarTodosWebService() {
        if (!isNetworkAvailable()) {
            Toast.makeText(applicationContext, "No hay conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        val idMovil = Device.getSecureId(this)
        Log.d("IDMovil", idMovil)
        val url = "$serverip/wsConsultarTodos.php?idMovil=$idMovil"
        Log.d("ConsultaURL", url)
        jsonObjectRequest = createNoCacheRequest(Request.Method.GET, url, null, this, this)
        request.add(jsonObjectRequest)
    }

    override fun onErrorResponse(error: VolleyError) {
        Log.e("ERROR", "Error: ${error.message}", error)
        Toast.makeText(applicationContext, "Error al consultar los datos: ${error.message}", Toast.LENGTH_SHORT).show()
    }

    override fun onResponse(response: JSONObject) {
        Log.d("Response", "Respuesta del servidor: $response")
        listaContactos.clear()
        try {
            // Check if 'contactos' array exists and is not null
            if (response.has("contactos")) {
                val json = response.optJSONArray("contactos")
                if (json != null) {
                    for (i in 0 until json.length()) {
                        val jsonObject = json.getJSONObject(i)
                        val contacto = Contactos()
                        contacto._ID = jsonObject.optInt("_ID")
                        contacto.nombre = jsonObject.optString("nombre")
                        contacto.telefono1 = jsonObject.optString("telefono1")
                        contacto.telefono2 = jsonObject.optString("telefono2")
                        contacto.direccion = jsonObject.optString("direccion")
                        contacto.notas = jsonObject.optString("notas")
                        contacto.favorite = jsonObject.optInt("favorite")
                        contacto.idMovil = jsonObject.optString("idMovil")
                        Log.d("Contactos", "Contacto añadido: $contacto")
                        listaContactos.add(contacto)
                    }
                }
            }
            adapter.notifyDataSetChanged()
            Log.d("ListaActivity", "Lista actualizada: $listaContactos")
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error al procesar los datos", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private inner class MyArrayAdapter(
        context: Context,
        private val textViewResourceId: Int,
        private val objects: ArrayList<Contactos>
    ) : ArrayAdapter<Contactos>(context, textViewResourceId, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: LayoutInflater.from(context).inflate(textViewResourceId, parent, false)
            val lblNombre: TextView = view.findViewById(R.id.lblNombreContacto)
            val lblTelefono: TextView = view.findViewById(R.id.lblTelefonoContacto)
            val btnModificar: Button = view.findViewById(R.id.btnModificar)
            val btnBorrar: Button = view.findViewById(R.id.btnBorrar)

            val contacto = objects[position]

            if (contacto.favorite > 0) {
                lblNombre.setTextColor(Color.BLUE)
                lblTelefono.setTextColor(Color.BLUE)
            } else {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            }

            lblNombre.text = contacto.nombre
            lblTelefono.text = contacto.telefono1

            btnBorrar.setOnClickListener {
                php.setContext(context)
                Log.i("id", contacto._ID.toString())
                php.borrarContactoWebService(contacto._ID, object : Response.Listener<JSONObject> {
                    override fun onResponse(response: JSONObject) {
                        Log.d("Borrar", "Respuesta del servidor: $response")
                        if (response.has("success")) {
                            Toast.makeText(applicationContext, "Contacto eliminado con éxito", Toast.LENGTH_SHORT).show()
                            consultarTodosWebService()
                        } else {
                            Toast.makeText(applicationContext, "Error al eliminar el contacto", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }

            btnModificar.setOnClickListener {
                val oBundle = Bundle()
                oBundle.putSerializable("contacto", objects[position])
                val i = Intent().apply {
                    putExtras(oBundle)
                }
                setResult(Activity.RESULT_OK, i)
                finish()
            }

            return view
        }
    }

    private fun clearApplicationCache() {
        try {
            val dir = cacheDir
            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        } else {
            return false
        }
    }
}