package com.solvia.assistantrh.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Applique le tri par défaut d'une liste quand l'appelant n'en demande pas.
 */
final class Pageables {

    private Pageables() {
    }

    static Pageable withDefaultSort(Pageable pageable, Sort defaultSort) {
        if (pageable.isUnpaged() || pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
    }
}
