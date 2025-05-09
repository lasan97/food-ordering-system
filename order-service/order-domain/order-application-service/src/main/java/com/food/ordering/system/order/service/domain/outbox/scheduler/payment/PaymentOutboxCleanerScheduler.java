package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {

	private final PaymentOutboxHelper paymentOutboxHelper;

	@Override
	@Scheduled(cron = "@midnight")
	public void processOutboxMessage() {
		Optional<List<OrderPaymentOutboxMessage>> outboxMessageResponse = paymentOutboxHelper.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
				OutboxStatus.COMPLETED,
				SagaStatus.SUCCEEDED,
				SagaStatus.FAILED,
				SagaStatus.COMPENSATED);

		if(outboxMessageResponse.isPresent()) {
			List<OrderPaymentOutboxMessage> outboxMessages = outboxMessageResponse.get();
			log.info("Received {} OrderPaymentOutboxMessage for clean-up. The payloads: {}",
					outboxMessages.size(),
					outboxMessages.stream().map(OrderPaymentOutboxMessage::getPayload)
							.collect(Collectors.joining("\n")));
			paymentOutboxHelper.deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(
					OutboxStatus.COMPLETED,
					SagaStatus.SUCCEEDED,
                    SagaStatus.FAILED,
                    SagaStatus.COMPENSATED);
			// 시스템 분석을 위한 로깅 가능
			log.info("{} OrderPaymentOutboxMessage deleted!", outboxMessages.size());
		}
	}
}
