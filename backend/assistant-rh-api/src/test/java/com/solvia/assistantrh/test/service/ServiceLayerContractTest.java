package com.solvia.assistantrh.test.service;

import com.solvia.assistantrh.service.ApplicationService;
import com.solvia.assistantrh.service.CandidateService;
import com.solvia.assistantrh.service.CommentService;
import com.solvia.assistantrh.service.DocumentService;
import com.solvia.assistantrh.service.InterviewService;
import com.solvia.assistantrh.service.JobOfferService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garde-fou : les controllers ne reçoivent des services que des DTO. Si une méthode publique
 * d'un service renvoie une entité JPA, même encapsulée (Page, Optional, List), ce test échoue.
 */
class ServiceLayerContractTest {

    private static final String ENTITY_PACKAGE = "com.solvia.assistantrh.entity.";

    private static final List<Class<?>> SERVICES = List.of(
            CandidateService.class, JobOfferService.class, ApplicationService.class,
            InterviewService.class, CommentService.class, DocumentService.class);

    @Test
    void noPublicServiceMethodReturnsAnEntity() {
        List<String> violations = SERVICES.stream()
                .flatMap(service -> Arrays.stream(service.getDeclaredMethods()))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> method.getGenericReturnType().getTypeName().contains(ENTITY_PACKAGE))
                .map(ServiceLayerContractTest::describe)
                .toList();

        assertThat(violations).isEmpty();
    }

    private static String describe(Method method) {
        return method.getDeclaringClass().getSimpleName() + "." + method.getName() + " -> " + method.getGenericReturnType().getTypeName();
    }
}
