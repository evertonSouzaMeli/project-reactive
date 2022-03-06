import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-Blocking
 * 3. Backpressure
 * Publisher / Observable -> Quem emite os eventos
 * Publisher é "cold"
 * Publisher <- (subscribe) Subscriber
 * Subscription é criado no momento que Subscribe se subinscreve no Publisher
 * Subscription is created
 * Publisher (onSubscribe on subscription) -> Subscriber
 * Subscriber que faz o "Backpressure"
 * Publisher chama o onNext no Subscriber
 * until:
 * 1. Publisher sends all the objects requested
 * 2. Publisher envia tudo o que é possivel  (onComplete)
 * 3. There is an error (onError) -> subscriber and subscription will be canceled
 * * */
@Slf4j
public class MonoTest {

    @Test
    public void monoSubscriber() {
        String name = "Everton Souza";
        var monoString = Mono.just(name);
        //mono.subscribe, serve para obter mais detalhes do Mono
        monoString.log().subscribe();

        log.info("\n -------STEP VERIFIER --------");

        /**
         * StepVerifier verifica equidade dos processos, parece o assertEquals
         * termina a verificação com a função "verifyComplete()"
         */
        StepVerifier.create(monoString.log()).expectNext("Everton Souza").verifyComplete();
        log.info("Mono {}", monoString);
    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "Everton Souza";
        var monoString = Mono.just(name);

        //podemos executar uma ação no momento da subinscrição com o Consumer<T>
        monoString.log().subscribe(value -> log.info("Value {}", value));
    }

    @Test
    public void monoSubscriberError() {
        String name = "Everton Souza";
        Mono<String> monoString = Mono.just(name)
                .map(string -> {
                    throw new RuntimeException("Testing mono with error");
                });

        monoString.subscribe(element -> log.info("Value {}", element), x -> log.error("Something Bad happening"));
        monoString.subscribe(element -> log.info("Value{}", element), Throwable::printStackTrace);

        StepVerifier.create(monoString)
                .expectError(RuntimeException.class)
                .verify();
    }
}
