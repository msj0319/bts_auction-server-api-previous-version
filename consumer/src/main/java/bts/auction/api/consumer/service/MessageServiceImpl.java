package bts.auction.api.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final Sinks.Many<Object> sinksMany;
    private final KafkaService kafkaService;
    private final ObjectMapper objectMapper;

    @Value("{kafka.topic}")
    String topic;

    @Override
    public Flux<ServerSentEvent<Object>> receive() {
        return sinksMany
                .asFlux()
                .publishOn(Schedulers.parallel())
                .map(message -> ServerSentEvent.builder(message).build())       //Sink로 전송되는 message를 SSE로 전송
                .doOnCancel(() -> System.out.println("disconnected by client"));//Client 종료 시, ping으로 인지하고 cancel signal을 받음
    }

    private Flux<ServerSentEvent<Object>> ping() {
        return Flux.interval(Duration.ofMillis(500))
                .map(i -> ServerSentEvent.<Object>builder().build());
    }
}