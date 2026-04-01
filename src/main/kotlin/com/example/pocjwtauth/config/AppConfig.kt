package com.example.pocjwtauth.config

import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class AppConfig {

    @Bean
    fun encodePassword() : BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }
}