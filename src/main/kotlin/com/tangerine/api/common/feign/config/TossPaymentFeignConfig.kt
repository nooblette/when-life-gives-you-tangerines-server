package com.tangerine.api.common.feign.config

import com.tangerine.api.common.feign.decoder.TossPaymentErrorDecoder
import feign.codec.ErrorDecoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TossPaymentFeignConfig {
    @Bean
    fun tossPaymentErrorDecoder(): ErrorDecoder = TossPaymentErrorDecoder()
}
