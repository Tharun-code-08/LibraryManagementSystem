package com.university.lms.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the Clean Architecture layering described in docs/02-Architecture.md:
 * entity → repository → service → ui, with util/business/security as framework-agnostic
 * support layers nothing above them may be depended on. A class in a lower layer must never
 * import a class from a higher one — that direction is what keeps persistence details and
 * business rules independent of any particular UI framework.
 */
class LayerDependencyTest {

    private static final com.tngtech.archunit.core.domain.JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.university.lms");

    @Test
    void entitiesDoNotDependOnRepositoriesServicesOrUi() {
        ArchRule rule = noClasses().that().resideInAPackage("..entity..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..repository..", "..service..", "..ui..", "..config..", "..database..");
        rule.check(CLASSES);
    }

    @Test
    void repositoriesDoNotDependOnServicesOrUi() {
        ArchRule rule = noClasses().that().resideInAPackage("..repository..")
                .should().dependOnClassesThat().resideInAnyPackage("..service..", "..ui..");
        rule.check(CLASSES);
    }

    @Test
    void servicesDoNotDependOnUi() {
        ArchRule rule = noClasses().that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAnyPackage("..ui..");
        rule.check(CLASSES);
    }

    @Test
    void businessRulesDoNotDependOnServicesOrUi() {
        ArchRule rule = noClasses().that().resideInAPackage("..business..")
                .should().dependOnClassesThat().resideInAnyPackage("..service..", "..ui..");
        rule.check(CLASSES);
    }

    @Test
    void utilitiesDoNotDependOnRepositoriesServicesOrUi() {
        ArchRule rule = noClasses().that().resideInAPackage("..util..")
                .should().dependOnClassesThat().resideInAnyPackage("..repository..", "..service..", "..ui..");
        rule.check(CLASSES);
    }
}
