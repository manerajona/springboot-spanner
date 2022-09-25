package com.github.manerajona.springspanner

import org.hibernate.annotations.GenericGenerator
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "singer")
class Singer(
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "singer_id") val id: String?,
    @Column(name = "singer_name") val name: String = ""
)

interface SingerRepo : JpaRepository<Singer, String>
