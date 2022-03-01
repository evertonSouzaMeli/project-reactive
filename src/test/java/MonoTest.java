import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;


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
    public void monoSubscriber(){
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
    public void monoSubscriberConsumer(){
        String name = "Everton Souza";
        var monoString = Mono.just(name);

        //podemos executar uma ação no momento da subinscrição com o Consumer<T>
        monoString.log().subscribe(value -> log.info("Value {}", value));
    }

    @Test
    public void monoSubscriberError(){
        String name = "Everton Souza";
        var monoString = Mono.just(name)
                                           .map(value -> {
                                               throw new RuntimeException("Testing mono with erro");
                                           });

        //podemos executar uma ação no momento da subinscrição com o Consumer<T>
        monoString.log().subscribe(value -> log.info("Value {}", value), error -> log.error("Something bad as happened"));

        StepVerifier.create(monoString).expectError(RuntimeException.class).verify();
    }

    @Test
    public void monoSubscriberOnComplete(){
        String name = "Everton Souza";
        var monoString = Mono.just(name).log().map(String::toUpperCase);

        /*podemos prepara o subscribe para eventos difentes, como um try-catch-finally
        monoString.log().subscribe(value -> log.info("Value {}", value),
                                   Throwable::printStackTrace,
                                   () -> log.info("FINISHED\n"),
                                   //podemos adicionar o Subscription e ele vai cancelar o relacionamento Pub-Sub
                                   Subscription::cancel);*/

        monoString.log().subscribe(value -> log.info("Value {}", value),
                Throwable::printStackTrace,
                () -> log.info("FINISHED\n"),
                //Aqui fazemos o backpressure, ou seja, falamos a quantidade de elementos que o Sub vai consumir do Pub
                subscription -> subscription.request(5));

        StepVerifier.create(monoString).expectNext(name.toUpperCase(Locale.ROOT)).verifyComplete();
    }

    @Test
    public void monoDoOnMethods(){
        String name = "Everton Souza";
        var monoString = Mono.just(name)
                                           .log()
                                           .map(String::toUpperCase)
                                           .doOnSubscribe(subscription -> log.info("Subscribed {}", subscription))
                                           .doOnRequest(longNumber -> log.info("Request received, start doing something.."))
                                           .doOnNext(string -> log.info("Value is here. Executing doOnNext({})", string))
                                            //esvazia a lista
                                           .flatMap(x -> Mono.empty())
                                            //não será executada por a lista está vazia
                                           .doOnNext(string -> log.info("Value is here. Executing doOnNext({})", string))
                                           .doOnSuccess(s -> log.info("doOnSucess executed {}", s));

        monoString.log().subscribe(value -> log.info("Value {}", value),
                Throwable::printStackTrace,
                () -> log.info("FINISHED\n"));
    }

    @Test
    public void monoDoOnErrors(){
        var error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                  .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                //Não é executado, pois o doOnError para a execução
                .doOnNext(s -> log.info("Executing this doOnNext"))
                //Aqui continuamos o fluxo de dados apesar do erro
                  .log();

        StepVerifier.create(error).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    public void monoDoOnErrorsResume(){
        String name = "Everton Souza";
        var error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                .onErrorResume( s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                //o doOnError não será executado porque o onErrorResume já trata a situação com return
                .doOnError(ex -> log.info("Error message: {}", ex.getMessage()))
                .log();

        StepVerifier.create(error).expectNext(name).verifyComplete();
    }

    @Test
    public void monoDoOnErrorsReturn(){
        String name = "Everton Souza";
        var error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                //Retorna valor simples, funciona como o Resume
                .onErrorReturn("EMPTY")
                .onErrorResume( s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                //o doOnError não será executado porque o onErrorResume já trata a situação com o return
                .doOnError(ex -> log.info("Error message: {}", ex.getMessage()))
                .log();

        StepVerifier.create(error).expectNext(name).verifyComplete();
    }

    @Test
    public void switchIfEmptyOperator(){
        var flux = emptyFlux().switchIfEmpty(Flux.just("not empty anymore")).log();

        StepVerifier.create(flux)
                    .expectSubscription()
                    .expectNext("not empty anymore")
                    .expectComplete()
                    .verify();
    }

    @Test
    public void deferOperator() throws Exception{
        var just = Mono.just(System.currentTimeMillis());
        //Defer é bom, toda vez que um subscribe entrar no Mono retorna valor atualizado
        var defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

        //Apesar do uso de Delay, o resultado do tempo será o mesmo
        // isso ocorre no momento da instancia, ele guarda o valor na memória
        defer.subscribe(x -> log.info("time {}", x));
        Thread.sleep(100);
        defer.subscribe(x -> log.info("time {}", x));
        Thread.sleep(100);
        defer.subscribe(x -> log.info("time {}", x));
        Thread.sleep(100);
        defer.subscribe(x -> log.info("time {}", x));

        AtomicLong atomicLong = new AtomicLong();
        defer.subscribe(atomicLong::set);
        Assertions.assertTrue(atomicLong.get() > 0L);
    }

    @Test
    public void concatOperator(){
        //Concat é um Lazy Operator ou seja ele vai esperar que o primeiro publisher termine para começar o subsequente
        var flux1 = Flux.just("a","b");
        var flux2 = Flux.just("c","d");

        var concatFlux = Flux.concat(flux1, flux2).log();

        StepVerifier.create(concatFlux)
                    .expectSubscription()
                    .expectNext("a","b","c","d")
                    .expectComplete()
                    .verify();
    }

    @Test
    public void concatWithOperator(){
        var flux1 = Flux.just("a","b");
        var flux2 = Flux.just("c","d");

        //Serve para mesclar fluxos
        var concatFlux = flux1.concatWith(flux2).log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a","b","c","d")
                .expectComplete()
                .verify();
    }

    @Test
    public void combineLatestOperator(){
        var flux1 = Flux.just("a","b");
        var flux2 = Flux.just("c","d");

        //CombineLastest pega o último emitido do primeiro flux, com o último valor emitido do segundo flux
        var combineLatest = Flux.combineLatest(flux1, flux2, (s1, s2) -> s1.toUpperCase().concat(s2.toUpperCase())).log();

        StepVerifier.create(combineLatest)
                .expectSubscription()
                .expectNext("BC","BD")
                .expectComplete()
                .verify();
    }

    //IMPORTANTE PARA SABER TRABALHAR COM FLAT MAP
    @Test
    public void mergeOperator() throws Exception{
        var flux1 = Flux.just("a","b").delayElements(Duration.ofMillis(200));
        var flux2 = Flux.just("c","d");
        var mergeFlux = Flux.merge(flux1, flux2)
                                        .delayElements(Duration.ofMillis(200))
                                        .log();

        //mergeFlux.subscribe(log::info);

        //Thread.sleep(1000);

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeWithOperator() throws Exception{
        var flux1 = Flux.just("a","b").delayElements(Duration.ofMillis(200));
        var flux2 = Flux.just("c","d");

        //MergeWith é a mesma coisa, contudo podemos chamar diretamente do flux, ele só aceita 1 flux como parametro
        var mergeFlux = flux1.mergeWith(flux2)
                                         .delayElements(Duration.ofMillis(200))
                                         .log();

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeSequentialOperator() throws Exception{
        var flux1 = Flux.just("a","b").delayElements(Duration.ofMillis(200));
        var flux2 = Flux.just("c","d");

        var mergeFlux = Flux.mergeSequential(flux1,flux2,flux1)
                .delayElements(Duration.ofMillis(200))
                .log();

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("a","b","c","d","a","b")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatOperatorError() throws Exception{
        var flux1 = Flux.just("a","b")
                .map(s -> {
                    if(s.equals("b"))
                        throw new IllegalArgumentException();
                    return s;
                });

        var flux2 = Flux.just("c","d");

        //concatDelayError é bom para adiar erro
        var concatFlux = Flux.concatDelayError(flux1,flux2).log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a","b","c","d")
                .expectError()
                .verify();
    }

    private Flux<Object> emptyFlux(){
        return Flux.empty();
    }
}
