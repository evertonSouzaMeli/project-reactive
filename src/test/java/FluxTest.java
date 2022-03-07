
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

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
}
