package com.solvia.assistantrh.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contrat de l'API : aucun controller ne renvoie une entité JPA, ni directement ni encapsulée
 * (ResponseEntity, Page, List, Optional). Tous les @RestController du projet sont analysés.
 */
class ApiContractTest {

    private static final String ENTITY_PACKAGE = "com.solvia.assistantrh.entity.";

    @Test
    void noControllerMethodReturnsAnEntity() {
        List<Class<?>> controllers = controllers();
        assertThat(controllers).extracting(Class::getSimpleName).containsExactlyInAnyOrder(
                "CandidateController", "JobOfferController", "ApplicationController", "InterviewController",
                "CommentController", "DashboardController", "DocumentController");

        List<String> violations = controllers.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods()))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> method.getGenericReturnType().getTypeName().contains(ENTITY_PACKAGE))
                .map(ApiContractTest::describe)
                .toList();

        assertThat(violations).isEmpty();
    }

    private static List<Class<?>> controllers() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        return scanner.findCandidateComponents("com.solvia.assistantrh").stream()
                .map(BeanDefinition::getBeanClassName)
                .<Class<?>>map(name -> ClassUtils.resolveClassName(name, ApiContractTest.class.getClassLoader()))
                .toList();
    }

    private static String describe(Method method) {
        return method.getDeclaringClass().getSimpleName() + "." + method.getName() + " -> " + method.getGenericReturnType().getTypeName();
    }
}
