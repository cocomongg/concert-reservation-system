package io.hhplus.concert.app.common.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);

}
