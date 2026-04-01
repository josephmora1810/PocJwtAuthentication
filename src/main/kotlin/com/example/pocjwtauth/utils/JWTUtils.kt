package com.example.pocjwtauth.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

@Component  // Le dice a Spring Boot que gestione esta clase como un Bean (singleton)
// para poder inyectarla con @Autowired donde sea necesario
class JWTUtils(
    @Value($$"${jwt.secret.key}") private val secretKey : String
) {

    // Código para generar tokens JWT
    fun generateToken(
        subject: String,            // El sujeto del token, normalmente email o username del usuario
        tokenId: String = UUID.randomUUID().toString(),
    ): String {

        return Jwts.builder()                               // Inicia la construcción del JWT
            .id(tokenId)                                   // Identificador único del token (JTI - JWT ID)
            .subject(subject)                               // El sujeto (SUB) - normalmente identifica al usuario
            .issuer("ABC_Ltd")                              // Quién emitió el token (ISS - Issuer)
            // Ejemplo real: "https://api.miapp.com"
            .audience().add("XYZ_Ltd").and()
            .issuedAt(Date(System.currentTimeMillis()))     // Fecha de emisión (IAT - Issued At)
            .expiration(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1))) // Expiración (EXP)
            // En este caso el token dura 1 día
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray())) // Firma Digital
            // Firma el token con la clave secreta para garantizar que no fue alterado
            // Keys.hmacShaKeyFor convierte la clave en formato seguro para HMAC
            .compact()                                      // Construye el token final como String
        // Une header, payload y signature con puntos y codifica en Base64Url
    }

    // Código para obtener y validar los claims del token
    fun getClaims( token: String,): Claims {             // Retorna los claims (datos) contenidos en el token
        return Jwts.parser()                                    // Crea un parser para analizar el JWT
            .verifyWith(Keys.hmacShaKeyFor(secretKey.toByteArray())) // Configura la clave para verificar la firma
            .build()                                            // Construye el parser con la configuración
            .parseSignedClaims(token)                           // Parsea el token y verifica la firma automáticamente
            .payload                                            // Extrae solo los claims (datos), no la firma
    }

    fun isValidToken(token: String): Boolean{
        return try {
            getClaims(token).expiration.after(Date(System.currentTimeMillis()))
        }catch (ex : Exception){
            false
        }
    }

    fun isValidJwtToken(token: String, userName : String): Boolean{
        return try {
            val tokenUserName = getClaims(token).subject
            (userName == tokenUserName && !isTokenExpired(token))
        }catch (ex : Exception){
            false
        }
    }

    fun isTokenExpired(token: String): Boolean{
        return try {
            getClaims(token).expiration.before(Date(System.currentTimeMillis()))
        }catch (ex : Exception){
            false
        }
    }

    fun getExpirationDate(token : String) : Date?{
        return try {
            getClaims(token).expiration
        }catch (ex : Exception){
            null
        }
    }

    fun getSubject(token : String) : String? {
        return try {
            getClaims(token).subject
        }catch (ex : Exception){
            null
        }
    }
}