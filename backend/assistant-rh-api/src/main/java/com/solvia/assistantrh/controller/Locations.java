package com.solvia.assistantrh.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * En-tête Location des réponses 201 : URL de la ressource créée, relative à la requête courante.
 */
final class Locations {

    private Locations() {
    }

    static URI of(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
