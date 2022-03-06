import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;


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
 * *
 */
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

    @Test
    public void monoSubscriberOnComplete() {
        String name = "Everton Souza";
        var monoString = Mono.just(name)
                .map(String::toUpperCase);

        monoString.log().subscribe(
                element -> log.info("Value {}", element)
                , Throwable::printStackTrace
                , () -> log.info("FINISHED !!!")
                //, element -> element.cancel());
                , element -> element.request(5));

        StepVerifier.create(monoString)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods() {
        String name = "Everton Souza";
        var monoString = Mono.just(name)
                .map(String::toUpperCase)
                //Acionado quando é o subscribe() é acionado
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                //Acionado sempre que houver elementos no fluxo
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                //Estamos limpando o Mono
                .flatMap(s -> Mono.empty())
                //Linha não será executada porque o Mono está vazio
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                //Acionado sempre que é feita uma requisição (subscription -> request(number))
                .doOnRequest(value -> log.info("Request Received, start to do something"))
                //Acionado sempre quando o fluxo é executado com sucesso
                .doOnSuccess(s -> log.info("doOnSucess executed {}", s));


        monoString.log().subscribe(s -> log.info("Value {}", s)
                , Throwable::printStackTrace
                , () -> log.info("FINISHED")
                , subscription -> subscription.request(5));
    }

    @Test
    public void monoDoOnErrors() {
        String name = "Everton Souza";
        var monoError = Mono.error(new RuntimeException("Something bad has happened"))
                .onErrorReturn("SOMETHING")
                //Bom para usar no FallBack
                .onErrorResume(throwable -> { log.info("Inside of onErrorResume()"); return Mono.just(name); })
                //Não será executada após o onErrorResume()
                .doOnError(exception -> log.info("Error Message: {}", exception.getMessage()));

        StepVerifier.create(monoError)
                .expectNext(name)
                .verifyComplete();
    }
}
