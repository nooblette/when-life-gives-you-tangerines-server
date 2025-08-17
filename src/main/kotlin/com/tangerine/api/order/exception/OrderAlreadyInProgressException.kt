package com.tangerine.api.order.exception

import com.tangerine.api.order.result.EvaluateOrderPaymentResult.InProgressOrder

class OrderAlreadyInProgressException : RuntimeException(InProgressOrder().message)
