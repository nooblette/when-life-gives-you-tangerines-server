package com.tangerine.api

import org.apache.catalina.Context
import org.apache.tomcat.util.descriptor.web.SecurityCollection
import org.apache.tomcat.util.descriptor.web.SecurityConstraint
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableFeignClients
class WhenLifeGivesYouTangerinesServerApplication {
    // Enable SSL
    @Bean
    fun servletContainer(): ServletWebServerFactory =
        object : TomcatServletWebServerFactory() {
            override fun postProcessContext(context: Context) {
                context.requireHttpsForAllPaths()
            }
        }

    private fun Context.requireHttpsForAllPaths() {
        val collection = SecurityCollection().apply { addPattern("/*") }
        val constraint =
            SecurityConstraint().apply {
                userConstraint = "CONFIDENTIAL"
                addCollection(collection)
            }
        addConstraint(constraint)
    }
}

fun main(args: Array<String>) {
    runApplication<WhenLifeGivesYouTangerinesServerApplication>(*args)
}
