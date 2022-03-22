package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

import static com.reactivespring.util.RetryUtil.getRetrySpec;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieInfoRestClient {
    private final WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String movieInfoUrl;

    public Mono<MovieInfo> retrieveMovieInfo(String movieId){
        var url = movieInfoUrl.concat("/{id}");

        return webClient
                .get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if(clientResponse.statusCode() == HttpStatus.NOT_FOUND){
                        return Mono.error(new MoviesInfoClientException("There is no movie info available for passed in id: " + movieId, clientResponse.rawStatusCode()));
                    }

                    return clientResponse
                            .bodyToMono(String.class)
                            .flatMap(responseMessage ->
                                 Mono.error(new MoviesInfoClientException(
                                        responseMessage,
                                        clientResponse.rawStatusCode()
                                ))
                            );
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                            log.error("Status code is: {}", clientResponse.statusCode().value());
                            return clientResponse
                                    .bodyToMono(String.class)
                                    .flatMap(responseMessage ->
                                            Mono.error(
                                                    new MoviesInfoServerException("Server Exception in MovieInfoService " + responseMessage)
                                            ));
                        }
                )
                .bodyToMono(MovieInfo.class)
                .retryWhen(getRetrySpec())
                .log();

    }

    public Flux<MovieInfo> retrieveMovieInfoStream() {
        var url = movieInfoUrl.concat("/stream");

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    return clientResponse
                            .bodyToMono(String.class)
                            .flatMap(responseMessage ->
                                    Mono.error(new MoviesInfoClientException(
                                            responseMessage,
                                            clientResponse.rawStatusCode()
                                    ))
                            );
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                            log.error("Status code is: {}", clientResponse.statusCode().value());
                            return clientResponse
                                    .bodyToMono(String.class)
                                    .flatMap(responseMessage ->
                                            Mono.error(
                                                    new MoviesInfoServerException("Server Exception in MovieInfoService " + responseMessage)
                                            ));
                        }
                )
                .bodyToFlux(MovieInfo.class)
                .retryWhen(getRetrySpec())
                .log();

    }
}
