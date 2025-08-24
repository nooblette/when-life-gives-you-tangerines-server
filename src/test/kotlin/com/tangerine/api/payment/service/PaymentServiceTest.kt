package com.tangerine.api.payment.service

import com.tangerine.api.payment.command.ApprovePaymentCommand
import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.fixture.command.createApprovePaymentCommand
import com.tangerine.api.payment.fixture.entity.findPaymentEntityByOrderId
import com.tangerine.api.payment.port.PaymentGatewayPort
import com.tangerine.api.payment.response.failure
import com.tangerine.api.payment.response.success
import com.tangerine.api.payment.result.ApprovePaymentResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
class PaymentServiceTest {
    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @MockitoBean
    private lateinit var paymentGatewayPort: PaymentGatewayPort

    private lateinit var approvePaymentCommand: ApprovePaymentCommand

    @BeforeEach
    fun setUp() {
        approvePaymentCommand = createApprovePaymentCommand()
    }

    @Test
    fun `결제 성공시 PaymentEntity 상태를 COMPLETED로 변경, requestedAt와 approvedAt를 업데이트한다`() {
        // given
        val expectedResponse =
            success(
                orderId = approvePaymentCommand.orderId,
                paymentKey = approvePaymentCommand.paymentKey,
            )
        whenever(
            paymentGatewayPort.approve(any()),
        ).thenReturn(
            expectedResponse,
        )

        // when
        val actualResponse = paymentService.approvePayment(command = approvePaymentCommand)
        actualResponse.shouldBeInstanceOf<ApprovePaymentResult.Success>()
        actualResponse.paymentKey shouldBe approvePaymentCommand.paymentKey

        val paymentEntity =
            checkNotNull(
                findPaymentEntityByOrderId(
                    jdbcTemplate = jdbcTemplate,
                    orderId = approvePaymentCommand.orderId,
                ),
            )
        paymentEntity.status shouldBe PaymentStatus.COMPLETED
        paymentEntity.orderName shouldBe expectedResponse.orderName
        paymentEntity.requestedAt shouldBe expectedResponse.requestedAt
        paymentEntity.approvedAt shouldBe expectedResponse.approvedAt
    }

    @Test
    fun `결제 실패시 PaymentEntity 상태를 FAILED로 변경, failCode와 failReason를 업데이트한다`() {
        // given
        val expectedResponse = failure(paymentKey = approvePaymentCommand.paymentKey)
        whenever(
            paymentGatewayPort.approve(any()),
        ).thenReturn(
            expectedResponse,
        )

        // when
        val actualResponse = paymentService.approvePayment(command = approvePaymentCommand)
        actualResponse.shouldBeInstanceOf<ApprovePaymentResult.Failure>()
        actualResponse.paymentKey shouldBe approvePaymentCommand.paymentKey

        val paymentEntity =
            checkNotNull(
                findPaymentEntityByOrderId(
                    jdbcTemplate = jdbcTemplate,
                    orderId = approvePaymentCommand.orderId,
                ),
            )
        paymentEntity.status shouldBe PaymentStatus.FAILED
        paymentEntity.failCode shouldBe expectedResponse.code
        paymentEntity.failReason shouldBe expectedResponse.message
        paymentEntity.approvedAt shouldBe null
    }
}
