package com.api.libreria.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "prestamos")
public class PrestamoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "libro_isbn", nullable = false)
    private LibroModel libro;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "socio_id", nullable = false)
    private PersonaModel socio;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPrestamo;

    // Fecha límite acordada para devolver el libro (opcional)
    @Column
    private LocalDateTime fechaDevolucion;

    // Fecha y hora en que realmente se devolvió el libro; null = préstamo en curso
    @Column
    private LocalDateTime fechaDevolucionReal;

    @PrePersist
    private void asignarFechaPrestamo() {
        if (fechaPrestamo == null) {
            fechaPrestamo = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LibroModel getLibro() {
        return libro;
    }

    public void setLibro(LibroModel libro) {
        this.libro = libro;
    }

    public PersonaModel getSocio() {
        return socio;
    }

    public void setSocio(PersonaModel socio) {
        this.socio = socio;
    }

    public LocalDateTime getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDateTime fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public LocalDateTime getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(LocalDateTime fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    // Solo lectura: aparece en las respuestas como "devuelto", pero no se acepta al recibir datos
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isDevuelto() {
        return fechaDevolucionReal != null;
    }
}