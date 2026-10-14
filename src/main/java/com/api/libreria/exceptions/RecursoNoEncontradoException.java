package com.api.libreria.exceptions;

// Extiende IllegalArgumentException para que los controladores de vista que ya
// capturan IllegalArgumentException sigan funcionando igual.
public class RecursoNoEncontradoException extends IllegalArgumentException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
