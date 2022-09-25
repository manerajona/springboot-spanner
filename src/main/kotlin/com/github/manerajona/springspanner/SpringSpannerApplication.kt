package com.github.manerajona.springspanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringSpannerApplication

fun main(args: Array<String>) {
    runApplication<SpringSpannerApplication>(*args)
}
