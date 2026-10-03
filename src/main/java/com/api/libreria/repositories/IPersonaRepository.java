package com.api.libreria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.libreria.models.PersonaModel;

public interface IPersonaRepository extends JpaRepository<PersonaModel, Long> {
    public PersonaModel findByCorreoElectronico(String correoElectronico);
}
