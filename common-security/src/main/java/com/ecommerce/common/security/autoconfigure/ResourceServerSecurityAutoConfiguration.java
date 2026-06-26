package com.ecommerce.common.security.autoconfigure;

import com.ecommerce.common.security.PublicEndpointMatcher;
import com.ecommerce.common.security.ResourceServerJwtFilter;
import com.ecommerce.common.security.TokenBlacklistService;
import com.ecommerce.common.security.UserOwnershipFilter;
import com.ecommerce.common.security.JwtTokenService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration(after = CommonSecurityAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "jwt.resource-server", name = "enabled", havingValue = "true")
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerSecurityAutoConfiguration {

    @Bean
    public ResourceServerJwtFilter resourceServerJwtFilter(JwtTokenService jwtTokenService,
                                                           TokenBlacklistService tokenBlacklistService,
                                                           PublicEndpointMatcher publicEndpointMatcher,
                                                           ObjectProvider<MeterRegistry> meterRegistryProvider) {
        return new ResourceServerJwtFilter(jwtTokenService, tokenBlacklistService, publicEndpointMatcher, meterRegistryProvider);
    }

    @Bean
    public UserOwnershipFilter userOwnershipFilter() {
        return new UserOwnershipFilter();
    }

    @Bean
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http,
                                                                 ResourceServerJwtFilter resourceServerJwtFilter,
                                                                 UserOwnershipFilter userOwnershipFilter,
                                                                 PublicEndpointMatcher publicEndpointMatcher) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(request -> publicEndpointMatcher.isPublic(
                                request.getMethod(), request.getRequestURI())).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(resourceServerJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userOwnershipFilter, ResourceServerJwtFilter.class);

        return http.build();
    }
}
