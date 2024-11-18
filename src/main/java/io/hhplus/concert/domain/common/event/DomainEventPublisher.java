package io.hhplus.concert.domain.common.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);

}
