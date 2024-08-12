package com.example.agendawebservice.Objetos

import java.io.Serializable

data class Contactos(
    var _ID: Int = 0,
    var nombre: String? = null,
    var telefono1: String? = null,
    var telefono2: String? = null,
    var direccion: String? = null,
    var notas: String? = null,
    var favorite: Int = 0,
    var idMovil: String? = null
) : Serializable {
    constructor() : this(0, null, null, null, null, null, 0, null)

    constructor(
        nombre: String?,
        telefono1: String?,
        telefono2: String?,
        direccion: String?,
        notas: String?,
        favorite: Int,
        idMovil: String?
    ) : this(0, nombre, telefono1, telefono2, direccion, notas, favorite, idMovil)
}

