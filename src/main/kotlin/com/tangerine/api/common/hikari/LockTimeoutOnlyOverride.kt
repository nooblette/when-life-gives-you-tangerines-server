package com.tangerine.api.common.hikari

import com.zaxxer.hikari.SQLExceptionOverride
import mu.KotlinLogging
import java.sql.SQLException
import java.sql.SQLTimeoutException

private val logger = KotlinLogging.logger {}

class LockTimeoutOnlyOverride : SQLExceptionOverride {
    override fun adjudicate(exception: SQLException): SQLExceptionOverride.Override {
        logger.debug("sqlState: ${exception.sqlState}, errorCode: ${exception.errorCode}")

        return when {
            // SQLTimeoutException 예외는 커넥션을 방출(Evict) 하지 않는다.
            exception is SQLTimeoutException -> SQLExceptionOverride.Override.DO_NOT_EVICT

            // 이 외 예외는 기본 정책대로 처리
            else -> SQLExceptionOverride.Override.CONTINUE_EVICT
        }
    }
}
