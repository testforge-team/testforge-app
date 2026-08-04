package com.testforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the whole backend.
 *
 * @SpringBootApplication does three things at once:
 *  1. Marks this as a configuration class.
 *  2. Turns on auto-configuration (Spring guesses sensible defaults
 *     based on the dependencies in pom.xml).
 *  3. Scans the com.testforge package and every sub-package for
 *     @Component / @Service / @RestController classes and wires them up.
 *
 * Running main() starts an embedded Tomcat web server on port 8080
 * (set in application.properties) and our API becomes live.
 */
@SpringBootApplication
public class TestForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestForgeApplication.class, args);
    }
}
