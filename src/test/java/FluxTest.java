
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Date;
import java.util.List;

@Slf4j
public class FluxTest {

    @Test
    public void fluxSubscriber() {
        var fluxString = Flux.just("Everton", "Souza", "DevDojo", "Academy");

        StepVerifier.create(fluxString)
                //.expectNext("Everton", "Souza", "DevDojo","Academy")
                .expectNext("Everton")
                .expectNext("Souza")
                .expectNext("DevDojo")
                .expectNext("Academy")
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers() {
        var flux = Flux.range(1, 5);
        flux.log().subscribe(i -> log.info("Number: {}", i));

        log.info("---------------------------------------");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberFromList() {
        var listOfNumbers = List.of(1, 2, 3, 4, 5);
        var flux = Flux.fromIterable(listOfNumbers).log();

        log.info("-----------------------------------------");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersErrors() {
        var fluxInteger = Flux.range(1, 5)
                .map(i -> {
                    if (i == 4)
                        throw new RuntimeException("ERROR");
                    return i;
                });

        fluxInteger.subscribe(integer -> log.info("Number: {}", integer)
                , Throwable::printStackTrace
                , () -> log.info("DONE"));

        StepVerifier.create(fluxInteger)
                .expectNext(1, 2, 3)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void fluxSubscriberNumbersErrorsUglyBackpressure() {
        var listOfIntegers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        var flux = Flux.fromIterable(listOfIntegers);

        flux.log().subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private int requestCount = 2;
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    subscription.request(2);
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });

        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersErrorNotSosUglyBackpressure(){
        var listOfIntegers = List.of(1,2,3,4,5,6,7,8,9,10);
        var flux = Flux.fromIterable(listOfIntegers);

        flux.log().subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private final int requestCount = 2;


            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if(count >= requestCount){
                    count = 0;
                    request(requestCount);
                }
            }
        });

        log.info("\n\n ------------------------- \n");

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberIntervalOne() throws InterruptedException {
        var flux = Flux.interval(Duration.ofMillis(100)).take(10).log();

        flux.subscribe(i -> log.info("Number: {}", i));

        Thread.sleep(3000);
    }

    @Test
    public void fluxSubscriberIntervalTwo() {
        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofDays(1))
                .thenAwait(Duration.ofDays(1))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    private Flux<Long> createInterval(){
        return Flux.interval(Duration.ofDays(1)).log();
    }
}
