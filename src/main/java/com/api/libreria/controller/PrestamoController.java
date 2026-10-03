package com.api.libreria.controller;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.api.libreria.models.PrestamoModel;
import com.api.libreria.services.PrestamoService;

@Controller
@RequestMapping("/prestamos")
public class PrestamoController {
    
    @Autowired
    private PrestamoService prestamoService;

    @GetMapping
    public ArrayList<PrestamoModel> obtenerPrestamos() {
        return this.prestamoService.obtenerPrestamos();
    }

    @GetMapping(path = "/{id}")
    public Optional<PrestamoModel> obtenerPrestamoPorId(@PathVariable("id") Long id) {
        return this.prestamoService.obtenerPrestamoModelPorId(id);
    }

    @DeleteMapping(path = "/{id}")
    public String eliminarPorId(@PathVariable("id") Long id) {
        boolean ok = this.prestamoService.eliminarPrestamo(id);
        if (ok) {
            return "Se eliminó el préstamo con ID: " + id;
        } else {
            return "No se pudo eliminar el préstamo con ID: " + id;
        }
    }
}
