package com.example.pocjwtauth.service

import com.example.pocjwtauth.entity.User
import com.example.pocjwtauth.repository.UserRepository
import com.example.pocjwtauth.service.iservice.IUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserServiceImpl : IUserService, UserDetailsService{

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    override fun saveUser(user: User): Int? {
        //Encode password before saving it into the database
        user.password = bCryptPasswordEncoder.encode(user.password).toString()
        return try {
            val savedUser = userRepository.save(user)
            savedUser.id
        }catch (e: Exception){
            return null
        }
    }

    override fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    override fun loadUserByUsername(username: String): UserDetails {
        val opt : User? = userRepository.findByUsername(username)

        return if (opt == null){
            throw UsernameNotFoundException("user with username: $username not found")
        }else{
            val roles = opt.roles
            val grantedAuthorities = HashSet<GrantedAuthority>()

            for (role in roles){
                grantedAuthorities.add(SimpleGrantedAuthority(role))
            }

            org.springframework.security.core.userdetails.User(
                opt.username,
                opt.password,
                grantedAuthorities,
            )
        }
    }

}