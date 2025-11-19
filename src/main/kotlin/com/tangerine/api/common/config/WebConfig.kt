package com.tangerine.api.common.config

import com.tangerine.api.global.ratelimit.interceptor.OrderRateLimitInterceptor
import com.tangerine.api.global.session.interceptor.SessionValidationInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${cors.allowed-origins}")
    private val allowedOrigins: String,
    private val sessionValidationInterceptor: SessionValidationInterceptor,
    private val orderRateLimitInterceptor: OrderRateLimitInterceptor,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(sessionValidationInterceptor)
            .addPathPatterns("/orders/**") // orders 하위 전체
            .order(1)

        registry
            .addInterceptor(orderRateLimitInterceptor)
            .addPathPatterns("/orders/**") // orders 하위 전체
            .order(2)
    }
}
