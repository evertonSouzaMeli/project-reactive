import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@Slf4j
public class FluxTest {

    @Test
    public void fluxSubscriber(){
        var fluxString = Flux.just("Everton", "Souza", "DevDojo","Academy");

        StepVerifier.create(fluxString)
                //.expectNext("Everton", "Souza", "DevDojo","Academy")
                .expectNext("Everton")
                .expectNext("Souza")
                .expectNext("DevDojo")
                .expectNext("Academy")
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers(){
        var flux = Flux.range(1,5);
        flux.log().subscribe(i -> log.info("Number: {}", i));

        log.info("---------------------------------------");
        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }
}
