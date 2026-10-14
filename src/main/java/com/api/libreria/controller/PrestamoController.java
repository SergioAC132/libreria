package com.api.libreria.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.libreria.models.PrestamoModel;
import com.api.libreria.services.PrestamoService;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<PrestamoModel> obtenerPrestamos() {
        return prestamoService.obtenerPrestamos();
    }

    @GetMapping("/activos")
    public List<PrestamoModel> obtenerActivos() {
        return prestamoService.obtenerPrestamosActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoModel> obtenerPorId(@PathVariable Long id) {
        return prestamoService.obtenerPrestamoModelPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PrestamoModel> crear(@RequestBody PrestamoModel prestamo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoService.guardarPrestamo(prestamo));
    }

    @PutMapping("/{id}")
    public PrestamoModel actualizar(@PathVariable Long id, @RequestBody PrestamoModel prestamo) {
        return prestamoService.actualizarPrestamo(prestamo, id);
    }

    @PutMapping("/{id}/devolver")
    public PrestamoModel devolver(@PathVariable Long id) {
        return prestamoService.devolverPrestamo(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (prestamoService.obtenerPrestamoModelPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!prestamoService.eliminarPrestamo(id)) {
            throw new IllegalStateException("No se pudo eliminar el préstamo.");
        }
        return ResponseEntity.noContent().build();
    }
}