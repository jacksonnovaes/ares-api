package br.com.ares.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "br.com.ares",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_application_or_adapters = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..",
                    "..adapter.."
            )
            .because("the domain must remain independent from orchestration and infrastructure");

    @ArchTest
    static final ArchRule application_must_not_depend_on_adapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..")
            .because("application code must communicate with infrastructure through ports");

    @ArchTest
    static final ArchRule inbound_adapters_must_not_depend_on_outbound_adapters = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
            .because("inbound and outbound adapters are independent implementation details");

    @ArchTest
    static final ArchRule outbound_adapters_must_not_depend_on_inbound_adapters = noClasses()
            .that().resideInAPackage("..adapter.out..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.in..")
            .because("outbound adapters must not access the delivery layer");

    @ArchTest
    static final ArchRule controllers_must_be_rest_controllers = classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("..adapter.in.web")
            .andShould().beAnnotatedWith(RestController.class)
            .because("HTTP entry points must follow the web adapter convention");
}
