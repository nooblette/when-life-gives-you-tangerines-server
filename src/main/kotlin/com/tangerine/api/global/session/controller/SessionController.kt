package com.tangerine.api.global.session.controller

import com.tangerine.api.global.response.ApiResponse
import com.tangerine.api.global.session.manager.SessionManager
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("/sessions")
class SessionController(
    private val sessionManager: SessionManager,
) {
    @PostMapping
    fun createSession(response: HttpServletResponse): ApiResponse<String> {
        val sessionId = sessionManager.createSession()

        // Set-Cookie 헤더 설정
        val cookie =
            ResponseCookie
                .from("sessionId", sessionId)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Duration.ofMinutes(30))
                .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
        return ApiResponse.Success("Ok")
    }
}
