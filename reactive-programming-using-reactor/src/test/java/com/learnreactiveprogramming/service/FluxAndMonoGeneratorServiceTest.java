package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService service = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux(){
        //given

        //when
        var namesFlux = service.namesFlux();
        //then

        StepVerifier.create(namesFlux)
                .expectNext("Alex", "Ben", "Xochitl")
//                .expectNextCount(3) //counts the remaining elements
                .verifyComplete();
    }

    @Test
    void namesFlux_map(){
        var namesFlux = service.namesFlux_map();

        StepVerifier.create(namesFlux)
                .expectNext("ALEX", "ARMANDO", "XOCHITL")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatMap() {
        var namesFlux = service.namesFlux_flatMap();

        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X","R","O","S","Y")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatMap_async() {
        var namesFlux = service.namesFlux_flatMap_async();

        StepVerifier.create(namesFlux)
//                .expectNext("A","L","E","X","R","O","S","Y")
                .expectNextCount(8)
                .verifyComplete();
    }

    @Test
    void namesFlux_concatMap() {
        var namesFlux = service.namesFlux_concatMap();

        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X","R","O","S","Y")
//                .expectNextCount(8)
                .verifyComplete();
    }


    @Test
    void namesFlux_flatmap_mono() {
        var namesListMono = service.namesFlux_flatmap_mono();

        StepVerifier.create(namesListMono)
                .expectNext(List.of("A", "L", "E", "X"))
//                .expectNextCount(8)
                .verifyComplete();
    }

    @Test
    void namesFlux_flatmapmany() {
        var namesFlux = service.namesFlux_flatmapmany();

        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform() {
        var namesFlux = service.namesFlux_transform();

        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X","R","O","S","Y")
                .verifyComplete();
    }

    @Test
    void namesFlux_defaultIfEmpty() {
        var strLength = 3;

        var namesFlux = service.namesFlux_defaultIfEmpty(strLength);

        StepVerifier.create(namesFlux)
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    void namesFlux_switchIfEmpty() {
        var strLength = 7;

        var namesFlux = service.namesFlux_switchIfEmpty(strLength);

        StepVerifier.create(namesFlux)
                .expectNext("d", "e", "f", "a", "u", "l", "t")
                .verifyComplete();
    }

    @Test
    void explore_concat() {
        var flux = service.explore_concat();
        StepVerifier.create(flux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_concatWith() {

        var flux = service.explore_concatWith();
        StepVerifier.create(flux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_concatWith_mono() {

        var flux = service.explore_concatWith_mono();
        StepVerifier.create(flux)
                .expectNext("A", "B")
                .verifyComplete();
    }

    @Test
    void explore_merge() {
        var mergedFlux = service.explore_merge();
        StepVerifier.create(mergedFlux)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void explore_mergeWith() {
        var mergedFlux = service.explore_mergeWith();
        StepVerifier.create(mergedFlux)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void explore_mergeWith_mono() {
        var mergedFlux = service.explore_mergeWith_mono();
        StepVerifier.create(mergedFlux)
                .expectNext("A","B")
                .verifyComplete();
    }

    @Test
    void explore_mergeSequential() {
        var flux = service.explore_mergeSequential();
        StepVerifier.create(flux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_zip() {
        var flux = service.explore_zip();
        StepVerifier.create(flux)
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }

    @Test
    void explore_zip_2() {
        var flux = service.explore_zip_2();
        StepVerifier.create(flux)
                .expectNext("AD14","BE25","CF36")
                .verifyComplete();
    }

    @Test
    void explore_zipWith() {
        var flux = service.explore_zipWith();
        StepVerifier.create(flux)
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }

    @Test
    void explore_zipWith_mono() {
        var mono = service.explore_zipWith_mono();
        StepVerifier.create(mono)
                .expectNext("AB")
                .verifyComplete();
    }
}