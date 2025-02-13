package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.food.ordering.system.outbox.OutboxStatus.COMPLETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxCleanerScheduler implements OutboxScheduler {

	private final OrderOutboxHelper orderOutboxHelper;

	@Override
	@Transactional
	@Scheduled(cron = "@midnight")
	public void processOutboxMessage() {
		Optional<List<OrderOutboxMessage>> outboxMessagesResponse = orderOutboxHelper.getOrderOutboxMessageByOutboxStatus(COMPLETED);

		if(outboxMessagesResponse.isPresent() && outboxMessagesResponse.get().size() > 0) {
			List<OrderOutboxMessage> outboxMessages = outboxMessagesResponse.get();
			log.info("Received {} OrderOutboxMessage for clean-up", outboxMessages.size());
			orderOutboxHelper.deleteOrderOutboxMessageByOutboxStatus(COMPLETED);
			log.info("Deleted {} OrderOutboxMessage", outboxMessages.size());
		}
	}
}
