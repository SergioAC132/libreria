package com.api.libreria.services;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.api.libreria.models.LibroModel;
import com.api.libreria.repositories.ILibroRepository;

@Service
public class LibroService {

    final ILibroRepository libroRepository;

    LibroService(ILibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public ArrayList<LibroModel> obtenerLibros() {
        return (ArrayList<LibroModel>) libroRepository.findAll();
    }

    public LibroModel guardarLibro(LibroModel libro) {
        if (libro.getNumeroEjemplares() == 0 || libro.getNumeroEjemplares() < 1) {
            throw new IllegalArgumentException("El libro debe tener al menos 1 ejemplar.");
        }
        return libroRepository.save(libro);
    }

    public Optional<LibroModel> obtenerLibroPorIsbn(Long isbn) {
        return libroRepository.findById(isbn);
    }

    public LibroModel actualizarLibro(LibroModel request, Long isbn) {
        LibroModel libro = libroRepository.findById(isbn)
            .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado con ISBN: " + isbn));

        libro.setTitulo(request.getTitulo());
        libro.setAutor(request.getAutor());
        libro.setAñoPublicacion(request.getAñoPublicacion());
        libro.setEditorial(request.getEditorial());
        libro.setNumeroEjemplares(request.getNumeroEjemplares());

        return libroRepository.save(libro);
    }

    public boolean eliminarLibro(Long isbn) {
        try {
            libroRepository.deleteById(isbn);
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    public ArrayList<LibroModel> obtenerLibrosPorAutor(String autor) {
        return libroRepository.findByAutor(autor);
    }

    public ArrayList<LibroModel> obtenerLibrosPorAnio(String anio) {
        return libroRepository.findByañoPublicacion(anio);
    }

    public ArrayList<LibroModel> obtenerLibrosPorEditorial(String editorial) {
        return libroRepository.findByEditorial(editorial);
    }
}