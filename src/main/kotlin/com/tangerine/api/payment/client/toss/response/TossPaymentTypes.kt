package com.tangerine.api.payment.client.toss.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class Cancel(
    val cancelAmount: Int,
    val cancelReason: String,
    val taxFreeAmount: Int,
    val taxExemptionAmount: Int,
    val refundableAmount: Int,
    val transferDiscountAmount: Int,
    val easyPayDiscountAmount: Int,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val canceledAt: LocalDateTime,
    val transactionKey: String,
    val receiptKey: String?,
    val cancelStatus: CancelStatus,
    val cancelRequestId: String?,
)

data class Card(
    val amount: Int,
    val issuerCode: String,
    val acquirerCode: String?,
    val number: String,
    val installmentPlanMonths: Int,
    val approveNo: String,
    val useCardPoint: Boolean,
    val cardType: CardType,
    val ownerType: OwnerType,
    val acquireStatus: AcquireStatus,
    val isInterestFree: Boolean,
    val interestPayer: InterestPayer?,
)

data class VirtualAccount(
    val accountType: AccountType,
    val accountNumber: String,
    val bankCode: String,
    val customerName: String,
    val depositorName: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val dueDate: LocalDateTime,
    val refundStatus: RefundStatus,
    val expired: Boolean,
    val settlementStatus: SettlementStatus,
    val refundReceiveAccount: RefundReceiveAccount?,
)

data class RefundReceiveAccount(
    val bankCode: String,
    val accountNumber: String,
    val holderName: String,
)

data class MobilePhone(
    val customerMobilePhone: String,
    val settlementStatus: SettlementStatus,
    val receiptUrl: String,
)

data class GiftCertificate(
    val approveNo: String,
    val settlementStatus: SettlementStatus,
)

data class Transfer(
    val placeholder: String? = null,
)

data class Receipt(
    val url: String,
)

data class Checkout(
    val url: String,
)

data class EasyPay(
    val provider: String,
    val amount: Int,
    val discountAmount: Int,
)

data class Failure(
    val code: String,
    val message: String,
)

data class CashReceipt(
    val type: CashReceiptType,
    val receiptKey: String,
    val issueNumber: String,
    val receiptUrl: String,
    val amount: Int,
    val taxFreeAmount: Int,
)

data class CashReceiptHistory(
    val receiptKey: String,
    val orderId: String,
    val orderName: String,
    val type: CashReceiptType,
    val issueNumber: String,
    val receiptUrl: String,
    val businessNumber: String,
    val transactionType: TransactionType,
    val amount: Int,
    val taxFreeAmount: Int,
    val issueStatus: IssueStatus,
    val failure: Failure?,
    val customerIdentityNumber: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val requestedAt: LocalDateTime,
)

data class Discount(
    val amount: Int,
)
