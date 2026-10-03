package com.api.libreria.services;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.api.libreria.models.LibroModel;
import com.api.libreria.models.PrestamoModel;
import com.api.libreria.repositories.IPrestamoRepository;

@Service
public class PrestamoService {

    final IPrestamoRepository prestamoRepository;

    PrestamoService(IPrestamoRepository prestamoRepository) {
        this.prestamoRepository = prestamoRepository;
    }

    public ArrayList<PrestamoModel> obtenerPrestamos() {
        return (ArrayList<PrestamoModel>) prestamoRepository.findAll();
    }

    public PrestamoModel guardarPrestamo(PrestamoModel prestamo) {
        validarDisponibilidad(prestamo.getLibro());
        return prestamoRepository.save(prestamo);
    }

    public Optional<PrestamoModel> obtenerPrestamoModelPorId(Long id) {
        return prestamoRepository.findById(id);
    }

    public PrestamoModel actualizarPrestamo(PrestamoModel request, Long id) {
        PrestamoModel prestamo = prestamoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Prestamo no encontrado con ID: " + id));

        // Solo validar si cambian el libro del prestamo
        if (!prestamo.getLibro().getIsbn().equals(request.getLibro().getIsbn())) {
            validarDisponibilidad(request.getLibro());
        }

        prestamo.setLibro(request.getLibro());
        prestamo.setSocio(request.getSocio());
        prestamo.setFechaPrestamo(request.getFechaPrestamo());
        prestamo.setFechaDevolucion(request.getFechaDevolucion());

        return prestamoRepository.save(prestamo);
    }

    public boolean eliminarPrestamo(Long id) {
        try {
            prestamoRepository.deleteById(id);
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    private void validarDisponibilidad(LibroModel libro) {
        long prestados = prestamoRepository.countByLibro(libro);
        int ejemplares = libro.getNumeroEjemplares() == 0 ? 0 : libro.getNumeroEjemplares();
        if (prestados >= ejemplares) {
            throw new IllegalArgumentException(
                "No hay ejemplares disponibles de \"" + libro.getTitulo() + "\".");
        }
    }
}