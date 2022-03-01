
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
    public void fluxSubscriber(){
        var fluxStrings = Flux.just("Everton", "Souza", "Silva", "DevDojo","Academy").log();
        StepVerifier.create(fluxStrings).expectNext("Everton", "Souza", "Silva", "DevDojo", "Academy").verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers(){
        var fluxInteger = Flux.range(1, 5);

        fluxInteger.log().subscribe(number -> log.info("Number: {}", number));

        log.info("\n\n ------------------------- \n");

        StepVerifier.create(fluxInteger).expectNext(1,2,3,4,5).verifyComplete();
    }

    @Test
    public void fluxSubscriberFromList(){
        var fluxInteger = Flux.fromIterable(List.of(1,2,3,4,5));

        fluxInteger.log().subscribe(number -> log.info("Number: {}", number));

        log.info("\n\n ------------------------- \n");

        StepVerifier.create(fluxInteger).expectNext(1,2,3,4,5).verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersErrors(){
        var fluxInteger = Flux.range(1,5)
                                            .map(number -> {
                                                if(number == 4)
                                                    throw new IndexOutOfBoundsException("Index error");
                                                return number;
                                            });

        fluxInteger.log().subscribe(number -> log.info("Number: {}", number),
                                              Throwable::printStackTrace,
                                              () -> log.info("DONE !"),
                                              subscription -> subscription.request(3));

        log.info("\n\n ------------------------- \n");

        StepVerifier.create(fluxInteger)
                    .expectNext(1,2,3)
                    .expectError(IndexOutOfBoundsException.class)
                    .verify();
    }

    @Test
    public void fluxSubscriberNumbersErrorsUglyBackpressure(){
        var fluxInteger = Flux.range(1,10);

        fluxInteger.log().subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private Subscription s;
            private int requestCount = 2;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                s.request(2);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if(count >= requestCount){
                    count = 0;
                    s.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });

        log.info("\n\n ------------------------- \n");

        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }
}
