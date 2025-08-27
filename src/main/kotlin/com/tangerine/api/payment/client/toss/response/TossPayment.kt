package com.tangerine.api.payment.client.toss.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

/**
 * 토스페이먼츠 결제 요청 응답 객체
 * - 결제 정보(결제 한 건의 결제 상태, 결제 취소 기록, 매출 전표, 현금영수증 정보 등)를 담는다.
 * - 결제가 승인됐을 때 응답은 항상 Payment 객체로 매핑된다.
 */
data class TossPayment(
    val version: String,
    val paymentKey: String,
    val type: PaymentType,
    val orderId: String,
    val orderName: String,
    val mId: String,
    val currency: String,
    val method: PaymentMethod?,
    val totalAmount: Int,
    val balanceAmount: Int,
    val status: TossPaymentStatus,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val requestedAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val approvedAt: LocalDateTime?,
    val useEscrow: Boolean,
    val lastTransactionKey: String?,
    val suppliedAmount: Int,
    val vat: Int,
    val cultureExpense: Boolean,
    val taxFreeAmount: Int,
    val taxExemptionAmount: Int,
    val isPartialCancelable: Boolean,
    val cancels: List<Cancel>?,
    val card: Card?,
    val virtualAccount: VirtualAccount?,
    val secret: String?,
    val mobilePhone: MobilePhone?,
    val giftCertificate: GiftCertificate?,
    val transfer: Transfer?,
    val metadata: Map<String, String>?,
    val receipt: Receipt?,
    val checkout: Checkout?,
    val easyPay: EasyPay?,
    val country: String,
    val failure: Failure?,
    val cashReceipt: CashReceipt?,
    val cashReceipts: List<CashReceiptHistory>?,
    val discount: Discount?,
)
