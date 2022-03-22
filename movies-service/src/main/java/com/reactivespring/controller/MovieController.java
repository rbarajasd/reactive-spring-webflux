package com.reactivespring.controller;

import com.reactivespring.client.MovieInfoRestClient;
import com.reactivespring.client.ReviewsRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final ReviewsRestClient reviewsRestClient;
    private final MovieInfoRestClient movieInfoRestClient;

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable(name = "id") String id){

        return movieInfoRestClient.retrieveMovieInfo(id)
                .flatMap(movieInfo -> reviewsRestClient.retrieveReviews(id)
                            .collectList()
                            .map(reviews -> new Movie(movieInfo, reviews)
                            ));
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> retrieveMovieInfos(){
        return movieInfoRestClient.retrieveMovieInfoStream();
    }
}
