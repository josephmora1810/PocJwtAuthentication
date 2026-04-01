package com.example.pocjwtauth

import com.example.pocjwtauth.utils.JWTUtils
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
class PocJwtAuthApplicationTests {

    @Autowired
    private lateinit var jwtUtils: JWTUtils

    private val secretKey = "my-super-secret-key-for-jwt-authentication-2024-spring-boo" // EN produccion, esto vendría de application.yml

    @Test
    fun `should generate and parse JWT correctly`() {
        val subject = "token1"
        val token = jwtUtils.generateToken(subject, secretKey)

        println("------------------------TOKEN----------------------------------------------------")
        println(token)
        println()

        //Aqui validamos si el token no esta vacio
        assertNotNull(token)
        assertTrue(token.isNotEmpty())

        val claims : Claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secretKey.toByteArray()))
            .build()
            .parseSignedClaims(token)
            .payload

        println("------------------------CLAIMS----------------------------------------------------")
        println("Token ID: ${claims.id}")
        println("Token Subject: ${claims.subject}")
        println("Token Issuer: ${claims.issuer}")
        println("Token Issue Date: ${claims.issuedAt}")
        println("Token Expiration Date: ${claims.expiration}")
    }

}
