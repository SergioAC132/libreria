package com.api.libreria.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.api.libreria.exceptions.RecursoNoEncontradoException;
import com.api.libreria.models.LibroModel;
import com.api.libreria.models.PersonaModel;
import com.api.libreria.models.PrestamoModel;
import com.api.libreria.repositories.ILibroRepository;
import com.api.libreria.repositories.IPersonaRepository;
import com.api.libreria.repositories.IPrestamoRepository;

@Service
public class PrestamoService {

    final IPrestamoRepository prestamoRepository;
    final ILibroRepository libroRepository;
    final IPersonaRepository personaRepository;

    PrestamoService(IPrestamoRepository prestamoRepository, ILibroRepository libroRepository,
            IPersonaRepository personaRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.personaRepository = personaRepository;
    }

    public ArrayList<PrestamoModel> obtenerPrestamos() {
        return (ArrayList<PrestamoModel>) prestamoRepository.findAll();
    }

    public List<PrestamoModel> obtenerPrestamosActivos() {
        return prestamoRepository.findByFechaDevolucionRealIsNull();
    }

    public PrestamoModel guardarPrestamo(PrestamoModel prestamo) {
        // Basta con enviar { "libro": { "isbn": 1 }, "socio": { "id": 1 } }: aquí se cargan completos
        LibroModel libro = resolverLibro(prestamo.getLibro());
        PersonaModel socio = resolverSocio(prestamo.getSocio());
        validarDisponibilidad(libro);

        LocalDateTime fechaPrestamo = prestamo.getFechaPrestamo() != null ? prestamo.getFechaPrestamo()
                : LocalDateTime.now();
        validarFechas(fechaPrestamo, prestamo.getFechaDevolucion());

        prestamo.setId(null);
        prestamo.setLibro(libro);
        prestamo.setSocio(socio);
        prestamo.setFechaPrestamo(fechaPrestamo);
        prestamo.setFechaDevolucionReal(null);
        return prestamoRepository.save(prestamo);
    }

    public Optional<PrestamoModel> obtenerPrestamoModelPorId(Long id) {
        return prestamoRepository.findById(id);
    }

    public PrestamoModel actualizarPrestamo(PrestamoModel request, Long id) {
        PrestamoModel prestamo = buscar(id);
        if (prestamo.isDevuelto()) {
            throw new IllegalStateException("No se puede modificar un préstamo que ya fue devuelto.");
        }
        LibroModel libro = resolverLibro(request.getLibro());
        PersonaModel socio = resolverSocio(request.getSocio());

        // Solo validar disponibilidad si cambian el libro del préstamo
        if (!prestamo.getLibro().getIsbn().equals(libro.getIsbn())) {
            validarDisponibilidad(libro);
        }
        // La fecha del préstamo no se modifica (la columna es no actualizable)
        validarFechas(prestamo.getFechaPrestamo(), request.getFechaDevolucion());

        prestamo.setLibro(libro);
        prestamo.setSocio(socio);
        prestamo.setFechaDevolucion(request.getFechaDevolucion());
        return prestamoRepository.save(prestamo);
    }

    // Registra la devolución del libro: guarda la fecha real y el ejemplar queda libre
    public PrestamoModel devolverPrestamo(Long id) {
        PrestamoModel prestamo = buscar(id);
        if (prestamo.isDevuelto()) {
            throw new IllegalStateException("Este préstamo ya fue devuelto.");
        }
        prestamo.setFechaDevolucionReal(LocalDateTime.now());
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

    private PrestamoModel buscar(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado con ID: " + id));
    }

    private LibroModel resolverLibro(LibroModel referencia) {
        if (referencia == null || referencia.getIsbn() == null) {
            throw new IllegalArgumentException("El libro es obligatorio (envía su isbn).");
        }
        return libroRepository.findById(referencia.getIsbn())
                .orElseThrow(() -> new IllegalArgumentException("El libro no existe."));
    }

    private PersonaModel resolverSocio(PersonaModel referencia) {
        if (referencia == null || referencia.getId() == null) {
            throw new IllegalArgumentException("El socio es obligatorio (envía su id).");
        }
        return personaRepository.findById(referencia.getId())
                .orElseThrow(() -> new IllegalArgumentException("El socio no existe."));
    }

    private void validarFechas(LocalDateTime fechaPrestamo, LocalDateTime fechaDevolucion) {
        if (fechaDevolucion != null && fechaPrestamo != null && !fechaDevolucion.isAfter(fechaPrestamo)) {
            throw new IllegalArgumentException("La fecha de devolución debe ser posterior a la fecha del préstamo.");
        }
    }

    // Solo cuentan los préstamos en curso: un libro devuelto vuelve a estar disponible
    private void validarDisponibilidad(LibroModel libro) {
        long prestados = prestamoRepository.countByLibroAndFechaDevolucionRealIsNull(libro);
        if (prestados >= libro.getNumeroEjemplares()) {
            throw new IllegalArgumentException(
                    "No hay ejemplares disponibles de \"" + libro.getTitulo() + "\".");
        }
    }
}