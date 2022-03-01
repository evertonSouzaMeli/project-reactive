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
        var fluxStrings = Flux.just("Everton", "Souza", "Silva", "DevDojo","Academy").log();
        StepVerifier.create(fluxStrings).expectNext("Everton", "Souza", "Silva", "DevDojo", "Academy").verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers(){
        var fluxInteger = Flux.range(1, 5);
        List<Integer> list = fluxInteger.collectList().block();

        fluxInteger.log().subscribe(number -> log.info("Number: {}", number));

        log.info("\n\n ------------------------- \n");

        StepVerifier.create(fluxInteger).expectNext(1,2,3,4,5).verifyComplete();
    }
}
