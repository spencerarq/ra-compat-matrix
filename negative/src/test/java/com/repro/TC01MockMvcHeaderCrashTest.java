package com.repro;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.SpringVersion;
import groovy.lang.GroovySystem;

/**
 * Test case for verifying binary compatibility between:
 * - Spring Boot 4 / Spring Framework 7
 * - RestAssured (HEAD of master, internal version 5.5.7-SNAPSHOT)
 *
 * Originally designed to investigate VerifyError / NoSuchMethodError issues
 * observed when using MockMvc + header() in the newest Spring Boot generation.
 */
public class TC01MockMvcHeaderCrashTest {

    @RestController
    public static class TestController {
        @GetMapping("/test")
        public String test() { return "OK"; }
    }

    /**
     * Environment evidence log.
     * Shows:
     * - Java version
     * - Groovy version
     * - Spring Framework version
     * - The RestAssured JAR actually loaded at runtime
     *
     * Note: HEAD snapshots do NOT define Implementation-Version in MANIFEST.MF,
     * so RA version may appear as null. The JAR path is the real evidence.
     */
    @BeforeAll
    public static void printEnvironmentEvidence() {
        System.out.println("================= [HUNTER EVIDENCE LOG] =================");
        System.out.println("1. OS Name:       " + System.getProperty("os.name"));
        System.out.println("2. Java Version:  " + System.getProperty("java.version"));
        System.out.println("3. Groovy Ver:    " + GroovySystem.getVersion());
        System.out.println("4. Spring Ver:    " + SpringVersion.getVersion());

        // RestAssured Version (may be null for local snapshots)
        Package raPackage = io.restassured.RestAssured.class.getPackage();
        String raVersion = raPackage != null ? raPackage.getImplementationVersion() : null;
        System.out.println("5. RA Version (MANIFEST): " + (raVersion != null ? raVersion : "null (expected for HEAD local snapshot)"));

        // Real proof: JAR location
        String raJarLocation = io.restassured.RestAssured.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toString();
        System.out.println("6. RA JAR Path:   " + raJarLocation);
        System.out.println("=========================================================");
    }

    @Test
    public void testHeadersWithSpringBoot4() {
        RestAssuredMockMvc.standaloneSetup(new TestController());
        System.out.println(">>> [HUNTER] EXECUTING TEST: SPRING BOOT 4 + RestAssured HEAD <<<");

        RestAssuredMockMvc.given()
                .header("X-Hunter", "Test")
                .when()
                .get("/test")
                .then()
                .status(HttpStatus.OK);

        System.out.println(">>> [HUNTER] EXECUTION COMPLETED SUCCESSFULLY <<<");
    }
}
