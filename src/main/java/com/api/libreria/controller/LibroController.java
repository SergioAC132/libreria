package com.api.libreria.controller;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.api.libreria.models.LibroModel;
import com.api.libreria.services.LibroService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/libros")
public class LibroController {
    
    @Autowired
    private LibroService libroService;

    @GetMapping
    public ArrayList<LibroModel> obtenerLibros() {
        return this.libroService.obtenerLibros();
    }

    @GetMapping("/anio/{anio}")
    public ArrayList<LibroModel> obtenerPorAnio(@PathVariable String anio) {
        return this.libroService.obtenerLibrosPorAnio(anio);
    }
    
    @GetMapping("/autor/{autor}")
    public ArrayList<LibroModel> obtenerPorAutor(@PathVariable String autor) {
        return this.libroService.obtenerLibrosPorAutor(autor);
    }


    @GetMapping(path = "/{isbn}")
    public Optional<LibroModel> obtenerPorId(@PathVariable("isbn") Long isbn) {
        return this.libroService.obtenerLibroPorIsbn(isbn);
    }

    
    @DeleteMapping(path = "/{isbn}")
    public String eliminarPorId(@PathVariable("isbn") Long isbn) {
        boolean ok = this.libroService.eliminarLibro(isbn);
        if (ok) {
            return "Se eliminó el libro con ISBN: " + isbn;
        } else {
            return "No se pudo eliminar el libro con ISBN: " + isbn;
        }
    }


}
