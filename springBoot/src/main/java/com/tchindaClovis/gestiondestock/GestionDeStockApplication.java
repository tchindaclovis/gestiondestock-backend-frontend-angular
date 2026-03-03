package com.tchindaClovis.gestiondestock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication  //(scanBasePackages = "com.tchindaClovis.gestiondestock")--->argument de l'annotation
@EnableJpaAuditing     //activation de l'auditing JPA (remplace la classe HibernateConfiguration)
public class GestionDeStockApplication {

	public static void main(String[] args) {

		SpringApplication.run(GestionDeStockApplication.class, args);
	}
}


//Classe à créer dans le repertoire "config"
//package com.tchindaClovis.gestiondestock.config;
//
//		import org.springframework.context.annotation.Configuration;
//		import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//
//@Configuration //va scanner toutes les classes qui contiennent @Configuration
//@EnableJpaAuditing
//public class HibernateConfiguration {
//}
