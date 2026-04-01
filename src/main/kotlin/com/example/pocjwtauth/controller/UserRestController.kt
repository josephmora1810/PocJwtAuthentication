package com.example.pocjwtauth.controller

import com.example.pocjwtauth.entity.UserRequest
import com.example.pocjwtauth.entity.UserResponse
import com.example.pocjwtauth.entity.User
import com.example.pocjwtauth.utils.JWTUtils
import com.example.pocjwtauth.service.iservice.IUserService
import com.example.pocjwtauth.utils.Constants
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping(Constants.URL_BASE_USERS)
class UserRestController(
    private val iUserService: IUserService,
    private val jwtUtils: JWTUtils,
    private val authenticationManager: AuthenticationManager
) {

    @PostMapping("/saveUsers")
    fun saveUser(@RequestBody user: User): ResponseEntity<String> {
        val id : Int? = iUserService.saveUser(user)
        lateinit var message: String
        return if (id != null && id > 0) {
            message = "user with id $id saved successfully!"
            ResponseEntity.ok(message)
        }else{
            message = "Error saving User"
            ResponseEntity.badRequest().body(message)
        }
    }

    @PostMapping("/loginUser")
    fun login(@RequestBody user: UserRequest): ResponseEntity<UserResponse> {
        //validate username/password with DB (required in case of stateless authentication)
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(user.username, user.password)
        )
        val token : String = jwtUtils.generateToken(user.username)
        return ResponseEntity.ok(UserResponse(token, "Token generated!"))
    }

    @PostMapping("/getData")
    fun testAfterLogin(p : Principal) : ResponseEntity<String> {
        return ResponseEntity.ok("You´re successfully authenticated!, You´re: ${p.name}")
    }
}