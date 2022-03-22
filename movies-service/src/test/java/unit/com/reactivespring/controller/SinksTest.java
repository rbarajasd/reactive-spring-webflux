package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sinks_replay(){
        //given
        Sinks.Many<Integer> replay = Sinks.many().replay().all();

        //when

        replay.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replay.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        //then

        Flux<Integer> integerFlux = replay.asFlux();
        integerFlux.subscribe(integer -> {
            System.out.println("Subscriber 1: " + integer);
        });

        Flux<Integer> integerFlux1 = replay.asFlux();
        integerFlux1.subscribe(integer -> {
            System.out.println("Subscriber 2: " + integer);
        });

        replay.tryEmitNext(3);
    }

    @Test
    void sinks_multicast(){

        //given
        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();

        //when
        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux = multicast.asFlux();
        integerFlux.subscribe(integer -> {
            System.out.println("Subscriber 1: " + integer);
        });

        Flux<Integer> integerFlux1 = multicast.asFlux();
        integerFlux1.subscribe(integer -> {
            System.out.println("Subscriber 2: " + integer);
        });

        multicast.tryEmitNext(3);
    }

    @Test
    void sinks_unicast(){

        //given
        Sinks.Many<Integer> unicast = Sinks.many().unicast().onBackpressureBuffer();

        //when
        unicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux = unicast.asFlux();
        integerFlux.subscribe(integer -> {
            System.out.println("Subscriber 1: " + integer);
        });

        Flux<Integer> integerFlux1 = unicast.asFlux();
        integerFlux1.subscribe(integer -> {
            System.out.println("Subscriber 2: " + integer);
        });

        unicast.tryEmitNext(3);
    }
}
