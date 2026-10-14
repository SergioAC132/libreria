package com.api.libreria.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.api.libreria.models.LibroModel;
import com.api.libreria.models.PersonaModel;
import com.api.libreria.models.PrestamoModel;
import com.api.libreria.services.LibroService;
import com.api.libreria.services.PersonaService;
import com.api.libreria.services.PrestamoService;

@Controller
public class PrestamoVistaController {

    private final PrestamoService prestamoService;
    private final LibroService libroService;
    private final PersonaService personaService;

    public PrestamoVistaController(PrestamoService prestamoService, LibroService libroService,
            PersonaService personaService) {
        this.prestamoService = prestamoService;
        this.libroService = libroService;
        this.personaService = personaService;
    }

    @GetMapping("/vista/prestamos")
    public String mostrarPrestamos(Model model) {
        model.addAttribute("prestamos", prestamoService.obtenerPrestamos());
        model.addAttribute("personas", personaService.obtenerPersonas());  
        model.addAttribute("libros", libroService.obtenerLibros());
        return "prestamos";
    }

    @PostMapping("/vista/prestamos/guardar")
    public String guardarPrestamo(@RequestParam Long idPersona,
            @RequestParam Long idLibro,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDevolucion,
            RedirectAttributes atributos) {
        try {
            PrestamoModel prestamo = new PrestamoModel();
            prestamo.setSocio(obtenerPersona(idPersona));
            prestamo.setLibro(obtenerLibro(idLibro));
            prestamo.setFechaPrestamo(LocalDateTime.now());
            prestamo.setFechaDevolucion(fechaDevolucion);

            prestamoService.guardarPrestamo(prestamo);
            atributos.addFlashAttribute("mensaje", "Prestamo registrado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            atributos.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/vista/prestamos";
    }

    @PostMapping("/vista/prestamos/{id}/actualizar")
    public String actualizarPrestamo(@PathVariable Long id,
            @RequestParam Long idPersona,
            @RequestParam Long idLibro,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDevolucion,
            RedirectAttributes atributos) {
        try {
            PrestamoModel existente = prestamoService.obtenerPrestamoModelPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Prestamo no encontrado."));

            PrestamoModel request = new PrestamoModel();
            request.setSocio(obtenerPersona(idPersona));
            request.setLibro(obtenerLibro(idLibro));
            request.setFechaPrestamo(existente.getFechaPrestamo());
            request.setFechaDevolucion(fechaDevolucion);

            prestamoService.actualizarPrestamo(request, id);
            atributos.addFlashAttribute("mensaje", "Prestamo actualizado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            atributos.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/vista/prestamos";
    }

    @PostMapping("/vista/prestamos/{id}/devolver")
    public String devolverPrestamo(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            prestamoService.devolverPrestamo(id);
            atributos.addFlashAttribute("mensaje", "Devolucion registrada. El ejemplar ya esta disponible.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            atributos.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/vista/prestamos";
    }

    @PostMapping("/vista/prestamos/{id}/eliminar")
    public String eliminarPrestamo(@PathVariable Long id, RedirectAttributes atributos) {
        if (prestamoService.eliminarPrestamo(id)) {
            atributos.addFlashAttribute("mensaje", "Prestamo eliminado correctamente.");
        } else {
            atributos.addFlashAttribute("error", "No se pudo eliminar el prestamo.");
        }
        return "redirect:/vista/prestamos";
    }

    private LibroModel obtenerLibro(Long isbn) {
        return libroService.obtenerLibroPorIsbn(isbn)                          
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));
    }

    private PersonaModel obtenerPersona(Long id) {
        return personaService.obtenerPersonaPorId(id)                        
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada."));
    }
}