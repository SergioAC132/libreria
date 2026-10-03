package com.api.libreria.services;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.libreria.models.PersonaModel;
import com.api.libreria.repositories.IPersonaRepository;

@Service
public class PersonaService {
    
    @Autowired
    IPersonaRepository personaRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public ArrayList<PersonaModel> obtenerPersonas() {
        return (ArrayList<PersonaModel>) personaRepository.findAll();
    }

    public PersonaModel guardarPersona(PersonaModel persona) {
        persona.setPassword(passwordEncoder.encode(persona.getPassword()));

        return personaRepository.save(persona);
    }

    public Optional<PersonaModel> obtenerPersonaPorId(Long id) {
        return personaRepository.findById(id);
    }

    public PersonaModel actualizarPersona(PersonaModel request, Long id) {
        PersonaModel persona = personaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));

        persona.setNombre(request.getNombre());
        persona.setApellidoPaterno(request.getApellidoPaterno());
        persona.setApellidoMaterno(request.getApellidoMaterno());
        persona.setFechaNacimiento(request.getFechaNacimiento());
        persona.setCorreoElectronico(request.getCorreoElectronico());
        persona.setTelefono(request.getTelefono());
        persona.setDireccion(request.getDireccion());


        return personaRepository.save(persona);
    }

    public boolean eliminarPersona(Long id) {
        try {
            personaRepository.deleteById(id);
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    public PersonaModel obtenerPersonasPorCorreoElectronico(String correoElectronico) {
        return personaRepository.findByCorreoElectronico(correoElectronico);
    }
}
