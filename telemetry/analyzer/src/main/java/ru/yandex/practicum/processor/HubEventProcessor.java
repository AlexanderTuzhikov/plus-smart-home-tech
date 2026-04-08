package ru.yandex.practicum.processor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.TopicsConfig;
import ru.yandex.practicum.kafka.consumer.EventConsumer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.HubEventServiceImpl;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final TopicsConfig topicsConfig;
    private final EventConsumer eventConsumer;
    private final HubEventServiceImpl hubEventServiceImpl;
    private final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    private List<String> topics;

    @PostConstruct
    private void topicsInit() {
        topics = List.of(topicsConfig.getHubs());
    }

    @Override
    public void run() {
        Consumer<String, HubEventAvro> consumer = eventConsumer.getConsumer();

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(topics);

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    log.info("Обработка входящего сообщения HubEvent: key={}, value={}", record.key(), record.value());
                    HubEventAvro hubEventAvro = record.value();
                    hubEventServiceImpl.HubEventHandle(hubEventAvro);
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки сообщений HubEvent", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }
}