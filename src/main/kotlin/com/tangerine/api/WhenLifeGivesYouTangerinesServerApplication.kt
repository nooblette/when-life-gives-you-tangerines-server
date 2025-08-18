package com.tangerine.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class WhenLifeGivesYouTangerinesServerApplication

fun main(args: Array<String>) {
    runApplication<WhenLifeGivesYouTangerinesServerApplication>(*args)
}
