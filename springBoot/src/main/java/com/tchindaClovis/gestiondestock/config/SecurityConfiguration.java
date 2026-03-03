package com.tchindaClovis.gestiondestock.config;

import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
import static com.tchindaClovis.gestiondestock.utils.Constants.AUTHENTICATION_ENDPOINT;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authenticationManager,
                                           ApplicationUserDetailsService userDetailsService,
                                           com.tchindaClovis.gestiondestock.config.ApplicationRequestFilter applicationRequestFilter) throws Exception {

        http
                .cors().and()
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/" + AUTHENTICATION_ENDPOINT + "/authenticate",
                                "/" + APP_ROOT + "/entreprises/create",
                                "/" + APP_ROOT + "/utilisateurs/update/password",
                                "/v2/api-docs",
                                "/swagger-resources/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/api/files/upload",
                                "/api/files/delete/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // on utilise l'AuthenticationManager déjà injecté
                .authenticationManager(authenticationManager)
                // ajout du filtre JWT personnalisé
                .addFilterBefore(applicationRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}









//package com.tchindaClovis.gestiondestock.config;
//
//import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//import static com.tchindaClovis.gestiondestock.utils.Constants.AUTHENTICATION_ENDPOINT;
//
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration {
//
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http,
//                                           ApplicationUserDetailsService userDetailsService,
//                                           ApplicationRequestFilter applicationRequestFilter) throws Exception {
//
//        http
//                .cors().and() // active le CORS
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/"+ AUTHENTICATION_ENDPOINT + "/authenticate",
//                                "/"+ APP_ROOT + "/entreprises/create",
//                                "/" + APP_ROOT + "/utilisateurs/update/password",
//                                "/v2/api-docs",
//                                "/swagger-resources",
//                                "/swagger-resources/**",
//                                "/configuration/ui",
//                                "/configuration/security",
//                                "/swagger-ui.html",
//                                "/webjars/**",
//                                "/v3/api-docs/**",
//                                "/swagger-ui/**",
//                                "/api/files/upload",
//                                "/api/files/delete/**"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authenticationManager(authenticationManager(null))
//                .addFilterBefore(applicationRequestFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//
//    @Bean
//    public CorsFilter corsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.addAllowedOrigin("http://localhost:4200");
//        config.addAllowedHeader("*");
//        config.addAllowedMethod("*");
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }
//
//
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}








//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")  // Permettre CORS sur toutes les routes
//                        .allowedOrigins("http://localhost:4200", "http://localhost:4200/assets/product.png") // L'origine de votre app Angular
//                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Méthodes autorisées
//                        .allowedHeaders("*") // En-têtes autorisés (important pour Authorization)
//                        .allowCredentials(true); // Autoriser l'envoi de cookies/auth
//            }
//        };
//    }




//
//package com.tchindaClovis.gestiondestock.config;
//
//        import java.util.Arrays;
//        import java.util.Collections;
//        import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
//        import org.springframework.beans.factory.annotation.Autowired;
//        import org.springframework.context.annotation.Bean;
//        import org.springframework.context.annotation.Configuration;
//        import org.springframework.security.authentication.AuthenticationManager;
//        import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//        import org.springframework.security.config.http.SessionCreationPolicy;
//        import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//        import org.springframework.security.crypto.password.PasswordEncoder;
//        import org.springframework.security.web.SecurityFilterChain;
//        import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//        import org.springframework.web.cors.CorsConfiguration;
//        import org.springframework.web.cors.CorsConfigurationSource;
//        import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//        import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//        import static com.tchindaClovis.gestiondestock.utils.Constants.AUTHENTICATION_ENDPOINT;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration {
//
//    private ApplicationUserDetailsService applicationUserDetailsService;
//
//    private ApplicationRequestFilter applicationRequestFilter;
//    @Autowired
//    public SecurityConfiguration(ApplicationUserDetailsService applicationUserDetailsService, ApplicationRequestFilter applicationRequestFilter) {
//        this.applicationUserDetailsService = applicationUserDetailsService;
//        this.applicationRequestFilter = applicationRequestFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/"+ AUTHENTICATION_ENDPOINT + "/authenticate",
//                                "/"+ APP_ROOT + "/entreprises/create",
//                                "/" + APP_ROOT + "/categories/create",
//                                "/v2/api-docs",
//                                "/swagger-resources",
//                                "/swagger-resources/**",
//                                "/configuration/ui",
//                                "/configuration/security",
//                                "/swagger-ui.html",
//                                "/webjars/**",
//                                "/v3/api-docs/**",
//                                "/swagger-ui/**"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .addFilterBefore(applicationRequestFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowCredentials(true);
//        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
//        configuration.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
//
//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        AuthenticationManagerBuilder authenticationManagerBuilder =
//                http.getSharedObject(AuthenticationManagerBuilder.class);
//        authenticationManagerBuilder.userDetailsService(applicationUserDetailsService)
//                .passwordEncoder(passwordEncoder());
//        return authenticationManagerBuilder.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}












//package com.tchindaClovis.gestiondestock.config;
//
//import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
//import jakarta.servlet.Filter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.NoOpPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.NoOpAuthenticationEntryPoint;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import static com.tchindaClovis.gestiondestock.utils.Constants.AUTHENTICATION_ENDPOINT;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration  {
//
//    private final ApplicationUserDetailsService applicationUserDetailsService;
//
//    public SecurityConfiguration(ApplicationUserDetailsService applicationUserDetailsService) {
//        this.applicationUserDetailsService = applicationUserDetailsService;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(AUTHENTICATION_ENDPOINT + "/authenticate",
//                             AUTHENTICATION_ENDPOINT + "/register",
//                             "/v3/api-docs/**",
//                             "/swagger-ui/**",
//                             "/swagger-ui.html",
//
//                            AUTHENTICATION_ENDPOINT + "/entreprises/create",
//                            "/v2/api-docs",
//                            "/swagger-resources",
//                            "/swagger-resources/**",
//                            "/configuration/ui",
//                            "/configuration/security",
//                            "/webjars/**").permitAll()
//                            .anyRequest().authenticated()
//                )
//                .userDetailsService(applicationUserDetailsService); // Directement avec UserDetailsService
//
//        return http.build();
//
//    }
//}







//package com.tchindaClovis.gestiondestock.config;
//
//import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration {
//
//    private final ApplicationUserDetailsService applicationUserDetailsService;
//
//    public SecurityConfiguration(ApplicationUserDetailsService applicationUserDetailsService) {
//        this.applicationUserDetailsService = applicationUserDetailsService;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/**/authenticate",
//                                "/**/register",
//                                "/v3/api-docs/**",
//                                "/swagger-ui/**",
//                                "/swagger-ui.html"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )
//                .userDetailsService(applicationUserDetailsService); // Directement avec UserDetailsService
//
//        return http.build();
//    }
//}



//package com.tchindaClovis.gestiondestock.config;
//
//        import org.springframework.context.annotation.Bean;
//        import org.springframework.context.annotation.Configuration;
//        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//        import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // Désactiver CSRF
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/**/authenticate").permitAll() // Autoriser l'authentification
//                        .anyRequest().authenticated() // Toutes les autres requêtes nécessitent une authentification
//                );
//
//        return http.build();
//    }
//}
