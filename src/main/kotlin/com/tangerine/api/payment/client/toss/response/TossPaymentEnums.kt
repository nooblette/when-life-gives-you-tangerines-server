package com.tangerine.api.payment.client.toss.response

import com.fasterxml.jackson.annotation.JsonProperty

enum class PaymentType {
    NORMAL,
    BILLING,
    BRANDPAY,
}

enum class PaymentMethod {
    @JsonProperty("카드")
    CARD,

    @JsonProperty("가상계좌")
    VIRTUAL_ACCOUNT,

    @JsonProperty("간편결제")
    EASY_PAY,

    @JsonProperty("휴대폰")
    MOBILE_PHONE,

    @JsonProperty("계좌이체")
    TRANSFER,

    @JsonProperty("문화상품권")
    CULTURE_GIFT_CERTIFICATE,

    @JsonProperty("도서문화상품권")
    BOOK_CULTURE_GIFT_CERTIFICATE,

    @JsonProperty("게임문화상품권")
    GAME_CULTURE_GIFT_CERTIFICATE,
}

enum class TossPaymentStatus {
    READY,
    IN_PROGRESS,
    WAITING_FOR_DEPOSIT,
    DONE,
    CANCELED,
    PARTIAL_CANCELED,
    ABORTED,
    EXPIRED,
}

enum class CardType {
    @JsonProperty("신용")
    CREDIT,

    @JsonProperty("체크")
    CHECK,

    @JsonProperty("기프트")
    GIFT,

    @JsonProperty("미확인")
    UNKNOWN,
}

enum class OwnerType {
    @JsonProperty("개인")
    PERSONAL,

    @JsonProperty("법인")
    CORPORATE,

    @JsonProperty("미확인")
    UNKNOWN,
}

enum class AcquireStatus {
    READY,
    REQUESTED,
    COMPLETED,
    CANCEL_REQUESTED,
    CANCELED,
}

enum class InterestPayer {
    BUYER,
    CARD_COMPANY,
    MERCHANT,
}

enum class AccountType {
    @JsonProperty("일반")
    NORMAL,

    @JsonProperty("고정")
    FIXED,
}

enum class RefundStatus {
    NONE,
    PENDING,
    FAILED,
    PARTIAL_FAILED,
    COMPLETED,
}

enum class SettlementStatus {
    INCOMPLETED,
    COMPLETED,
}

enum class CancelStatus {
    DONE,
}

enum class CashReceiptType {
    @JsonProperty("소득공제")
    INCOME_DEDUCTION,

    @JsonProperty("지출증빙")
    EXPENSE_PROOF,
}

enum class TransactionType {
    CONFIRM,
    CANCEL,
}

enum class IssueStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED,
}
