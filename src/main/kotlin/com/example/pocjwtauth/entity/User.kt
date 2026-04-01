package com.example.pocjwtauth.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Email

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(unique = true, nullable = false)
    val username: String = "",

    @Column(nullable = false)
    var password: String = "",

    @Column(unique = true, nullable = false)
    @field:Email
    val email: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "roles",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "user_role")
    val roles: Set<String> = emptySet()
)