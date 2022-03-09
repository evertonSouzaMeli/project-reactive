import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@Slf4j
public class OperatorsTest {

    @Test
    public void subscribeOnSimple(){
        var flux = Flux.range(1,5)
                .map(integer -> {
                    log.info("Map 1 - Number {} on Thread {}", integer, Thread.currentThread().getName());
                    return integer;
                }).subscribeOn(Schedulers.boundedElastic())
                .map(integer -> {
                    log.info("Map 2 - Number {} on Thread {}", integer, Thread.currentThread().getName());
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
        var flux = Flux.range(1,5)
                .map(integer -> {
                    log.info("Map 1 - Number {} on Thread {}", integer, Thread.currentThread().getName());
                    return integer;
                }).publishOn(Schedulers.boundedElastic())
                .map(integer -> {
                    log.info("Map 2 - Number {} on Thread {} ", integer, Thread.currentThread().getName());
                    return integer;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();
    }

}
