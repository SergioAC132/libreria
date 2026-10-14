package com.api.libreria.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.libreria.models.LibroModel;
import com.api.libreria.models.PrestamoModel;

public interface IPrestamoRepository extends JpaRepository<PrestamoModel, Long> {

    // Ejemplares de un libro que siguen prestados (todavía no se devuelven)
    long countByLibroAndFechaDevolucionRealIsNull(LibroModel libro);

    // Préstamos en curso
    List<PrestamoModel> findByFechaDevolucionRealIsNull();
}