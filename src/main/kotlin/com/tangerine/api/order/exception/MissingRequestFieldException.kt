package com.tangerine.api.order.exception

class MissingRequestFieldException(
    fieldName: String,
) : RuntimeException("${fieldName}이(가) 누락되었습니다.")
