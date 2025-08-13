package com.tangerine.api.order.exception

import com.tangerine.api.order.result.OrderPaymentEvaluationResult.InProgressOrder

class OrderAlreadyInProgressException(
    message: String = InProgressOrder().message,
) : RuntimeException(message)
