package com.example.agendawebservice.Objetos

import android.content.Context
import android.util.Log
import com.android.volley.Cache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ProcesosPHP : Response.Listener<JSONObject>, Response.ErrorListener {
    private var request: RequestQueue? = null
    private lateinit var jsonObjectRequest: JsonObjectRequest
    private val serverip = "http://practicascarolvg.online/WebService"

    fun setContext(context: Context) {
        request = Volley.newRequestQueue(context)
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

    fun insertarContactoWebService(c: Contactos) {
        val url = "$serverip/wsRegistro.php?nombre=${c.nombre}" +
                "&telefono1=${c.telefono1}&telefono2=${c.telefono2}" +
                "&direccion=${c.direccion}&notas=${c.notas}" +
                "&favorite=${c.favorite}&idMovil=${c.idMovil}"
        val formattedUrl = url.replace(" ", "%20")
        Log.d("insertarContacto", "URL: $formattedUrl")
        jsonObjectRequest = createNoCacheRequest(Request.Method.GET, formattedUrl, null, this, this)
        request?.add(jsonObjectRequest)
    }

    fun actualizarContactoWebService(c: Contactos, id: Int) {
        val url = "$serverip/wsActualizar.php?_ID=$id" +
                "&nombre=${c.nombre}&direccion=${c.direccion}" +
                "&telefono1=${c.telefono1}&telefono2=${c.telefono2}" +
                "&notas=${c.notas}&favorite=${c.favorite}"
        val formattedUrl = url.replace(" ", "%20")
        Log.d("actualizarContacto", "URL: $formattedUrl")
        jsonObjectRequest = createNoCacheRequest(Request.Method.GET, formattedUrl, null, this, this)
        request?.add(jsonObjectRequest)
    }

    fun borrarContactoWebService(id: Int, responseListener: Response.Listener<JSONObject>) {
        val url = "$serverip/wsEliminar.php?_ID=$id"
        Log.d("borrarContacto", "URL: $url")
        jsonObjectRequest = createNoCacheRequest(Request.Method.GET, url, null, responseListener, this)
        request?.add(jsonObjectRequest)
    }

    override fun onErrorResponse(error: VolleyError) {
        Log.e("ERROR", error.toString())
    }

    override fun onResponse(response: JSONObject) {
        Log.d("Response", response.toString())
    }
}