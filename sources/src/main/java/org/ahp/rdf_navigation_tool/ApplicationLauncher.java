package org.ahp.rdf_navigation_tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@ComponentScan
@SpringBootApplication
public class ApplicationLauncher {

	/**
	 * API launch function. Once the process is initialized, controllers are listening for request for the given context-path
	 * @param args no arguments required
	 */
	public static void main(String[] args) {
		SpringApplication.run(ApplicationLauncher.class, args);
	}
}
