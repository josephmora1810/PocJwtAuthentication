package com.example.pocjwtauth.service.iservice

import com.example.pocjwtauth.entity.User

interface IUserService {
    fun saveUser(user: User) : Int?
    fun findByUsername(username: String) : User?
}