package com.api.libreria.controller;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.libreria.models.PersonaModel;
import com.api.libreria.services.PersonaService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/personas")
public class PersonaController {
    
    @Autowired
    private PersonaService personaService;

    @GetMapping
    public ArrayList<PersonaModel> obtenerPersonas() {
        return this.personaService.obtenerPersonas();
    }
    
    @GetMapping(path = "/{id}")
    public Optional<PersonaModel> obtenerPorId(@PathVariable("id") Long id) {
        return this.personaService.obtenerPersonaPorId(id);
    }

    @DeleteMapping(path = "/{id}")
    public String eliminarPorId(@PathVariable("id") Long id) {
        boolean ok = this.personaService.eliminarPersona(id);
        if (ok) {
            return "Se eliminó la persona con ID: " + id;
        } else {
            return "No se pudo eliminar la persona con ID: " + id;
        }
    }
    

}
