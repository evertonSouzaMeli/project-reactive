import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class OperatorsTest {

    @Test
    public void subscribeOnSimple(){
        var flux = Flux.range(1,5)
                .log()
                .subscribeOn(Schedulers.boundedElastic())
                .map(integer -> {
                    log.info("Map 1 - Number {} Thread {}",integer, Thread.currentThread());
                    return integer;
                })
                .subscribeOn(Schedulers.single())
                .map(integer -> {
                    log.info("Map 2 - Number {} Thread {}",integer, Thread.currentThread());
                    return integer;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();
    }

    @Test
    public void publishOnSimple() {
        var flux = Flux.range(1, 4)
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                //publishOn afeta somente o que vai vir depois da função
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        flux.subscribe();
        flux.subscribe();

        /*StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }*/
    }

    @Test
    public void multipleSubscribeOnSimple() {
        var flux = Flux.range(1, 4)
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void multiplePublishOnSimple() {
        var flux = Flux.range(1, 4)
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void subscribeAndPublishOnSimple() {
        var flux = Flux.range(1, 4)
                //publishOn tem preferencia sobre o subscribeOn
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void publishAndSubscribeOnSimple() {
        var flux = Flux.range(1, 4)
                //publishOn tem preferencia sobre o subscribeOn
                .subscribeOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    //Esse methodo é muito bom para fazer chamadas para APIS externas
    @Test
    public void subscribeOnIO() throws Exception {
        //Executa uma Thread em background quando a thread alvo estiver bloqueada
        var monoList = Mono.fromCallable(() -> Files.readAllLines(Path.of("text-file")))
                .log()
                .subscribeOn(Schedulers.boundedElastic());

        StepVerifier.create(monoList)
                .expectSubscription()
                .thenConsumeWhile(list -> {
                    Assertions.assertFalse(list.isEmpty());
                    log.info("Size {}", list.size());
                    return true;
                }).verifyComplete();
    }

    @Test
    public void switchIfEmptyOperator(){
        var flux = emptyFlux()
                .switchIfEmpty(Flux.just("Not empty anymore"))
                .log();


        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("Not empty anymore")
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

    private Flux<Object> emptyFlux(){
        return Flux.empty();
    }
}
