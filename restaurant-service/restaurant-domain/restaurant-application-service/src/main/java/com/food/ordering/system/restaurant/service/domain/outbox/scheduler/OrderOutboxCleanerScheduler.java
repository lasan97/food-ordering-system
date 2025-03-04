package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxCleanerScheduler implements OutboxScheduler {

	private final OrderOutboxHelper orderOutboxHelper;

	@Scheduled(cron = "@midnight")
	@Override
	public void processOutboxMessage() {
		Optional<List<OrderOutboxMessage>> outboxMessagesResponse =
				orderOutboxHelper.getOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
		if(outboxMessagesResponse.isPresent() && outboxMessagesResponse.get().size() > 0) {
			List<OrderOutboxMessage> outboxMessages = outboxMessagesResponse.get();
			log.info("Received {} OrderOutboxMessage for clean-up", outboxMessages.size());
			orderOutboxHelper.deleteOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
			log.info("Deleted {} OrderOutboxMessage", outboxMessages.size());
		}
	}
}
