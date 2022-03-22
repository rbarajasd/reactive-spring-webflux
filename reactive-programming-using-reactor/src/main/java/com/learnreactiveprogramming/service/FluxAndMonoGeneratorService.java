package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public static void main(String[] args) {
        FluxAndMonoGeneratorService service = new FluxAndMonoGeneratorService();

//        service.namesFlux()
//                .subscribe((name) -> {
//                    System.out.println("Name is: " + name);
//                });

        System.out.println("/////////////////////////////////////");

//        service.nameMono().subscribe((name) -> {
//            System.out.println("Name is: " + name);
//        });

        System.out.println("/////////////////////////////////////");

//        service.namesFlux_map().subscribe(System.out::println);

        System.out.println("/////////////////////////////////////");

        service.namesFlux_flatMap().subscribe(System.out::println);
    }

    public Flux<String> namesFlux(){
        return Flux.fromIterable(List.of("Alex", "Ben", "Xochitl"))
                .log();
    }

    public Flux<String> namesFlux_map(){
        return Flux.fromIterable(List.of("Alex", "Armando", "Xochitl"))
                .map(String::toUpperCase)
                .log();
    }

    public Flux<String> namesFlux_flatMap(){
        return Flux.fromIterable(List.of("Alex", "Armando", "Rosy"))
                .filter(s -> s.length() <= 4)
                .flatMap(s -> splitString(s))
                .map(s -> s.toUpperCase(Locale.ROOT))
                .log();
    }

    public Flux<String> namesFlux_flatMap_async(){
        return Flux.fromIterable(List.of("Alex", "Armando", "Rosy"))
                .filter(s -> s.length() <= 4)
                .flatMap(s -> splitString_withDelay(s))
                .map(String::toUpperCase)
                .log();
    }

    public Flux<String> namesFlux_concatMap(){
        return Flux.fromIterable(List.of("Alex", "Armando", "Rosy"))
                .filter(s -> s.length() <= 4)
                .concatMap(s -> splitString_withDelay(s))
                .map(String::toUpperCase)
                .log();
    }

    public Mono<List<String>> namesFlux_flatmap_mono(){
        return Mono.just("Alex")
                .map(String::toUpperCase)
                .flatMap(this::mono_namesList)
                .log();
    }

    public Flux<String> namesFlux_transform(){

        Function<Flux<String>, Flux<String>> filterMap = name -> name
                .filter(s -> s.length() <= 4)
                .flatMap(s -> splitString(s));


        return Flux.fromIterable(List.of("Alex", "Armando", "Rosy"))
                .transform(filterMap)
                .map(s -> s.toUpperCase(Locale.ROOT))
                .log();
    }

    public Flux<String> namesFlux_defaultIfEmpty(int strLength){

        Function<Flux<String>, Flux<String>> filterMap = name -> name
                .filter(s -> s.length() <= strLength)
                .flatMap(s -> splitString(s));


        return Flux.fromIterable(List.of("Alex", "Armando", "Rosy"))
                .transform(filterMap)
                .map(s -> s.toUpperCase(Locale.ROOT))
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> explore_concat(){
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");

        return Flux.concat(abcFlux, defFlux);
    }

    public Flux<String> explore_concatWith(){
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");

        return abcFlux.concatWith(defFlux);
    }

    public Flux<String> explore_concatWith_mono(){
        var aMono = Mono.just("A");
        var bMono = Mono.just("B");

        return aMono.concatWith(bMono);
    }

    public Flux<String> explore_merge(){
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.merge(abcFlux, defFlux);
    }

    public Flux<String> explore_mergeWith(){
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return abcFlux.mergeWith(defFlux);
    }

    public Flux<String> explore_mergeWith_mono(){
        var aMono = Mono.just("A");

        var bMono = Mono.just("B");

        return aMono.mergeWith(bMono);
    }

    public Flux<String> explore_mergeSequential(){
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(abcFlux, defFlux);
    }

    public Flux<String> explore_zip(){
        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return Flux.zip(abcFlux, defFlux, (abc, def) -> abc + def );
    }

    public Flux<String> explore_zip_2(){
        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        var flux123 = Flux.just("1", "2", "3");

        var flux456 = Flux.just("4", "5", "6");

        return Flux.zip(abcFlux, defFlux, flux123, flux456)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                .log();
    }

    public Flux<String> explore_zipWith(){
        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return abcFlux.zipWith(defFlux, (abc, def) -> abc + def);
    }

    public Mono<String> explore_zipWith_mono(){
        var aMono = Mono.just("A");

        var bMono = Mono.just("B");

        return aMono.zipWith(bMono)
                .map(t2 -> t2.getT1() + t2.getT2());
    }

    public Flux<String> namesFlux_switchIfEmpty(int strLength){

        Function<Flux<String>, Flux<String>> filterMap = name -> name
                .filter(s -> s.length() >= strLength)
                .flatMap(this::splitString);

        Flux<String> defaultFlux = Flux.just("default")
                .transform(filterMap);

        return Flux.fromIterable(List.of("Alex", "Coral", "Rosy"))

                .transform(filterMap)
                .switchIfEmpty(defaultFlux)
                .log();
    }

    private Mono<List<String>> mono_namesList(String names){
        var charArray = names.split("");
        var namesList = List.of(charArray);

        return Mono.just(namesList);
    }

    public Flux<String> namesFlux_flatmapmany(){
        return Mono.just("Alex")
                .map(String::toUpperCase)
                .flatMapMany(this::splitString)
                .log();
    }

    public Flux<String> splitString(String name){
        var charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    public Flux<String> splitString_withDelay(String name){
        var delay = new Random().nextInt(1000);
        var charArray = name.split("");
        return Flux.fromArray(charArray)
                .delayElements(Duration.ofMillis(delay));
    }

    public Mono<String> nameMono(){
        return Mono.just("Alex")
                .log();
    }
}
