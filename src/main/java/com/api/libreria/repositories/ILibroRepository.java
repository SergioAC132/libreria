package com.api.libreria.repositories;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.libreria.models.LibroModel;

public interface ILibroRepository extends JpaRepository<LibroModel, Long> {
    
    public ArrayList<LibroModel> findByAutor(String autor);

    public ArrayList<LibroModel> findByañoPublicacion(String anio);

    public ArrayList<LibroModel> findByEditorial(String editorial);
}
