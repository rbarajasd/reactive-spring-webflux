package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.reactivespring.util.RetryUtil.getRetrySpec;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewsRestClient {

    private final WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    //movieInfoId
    public Flux<Review> retrieveReviews(String movieInfoId){
        var url = UriComponentsBuilder
                .fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieInfoId)
                .buildAndExpand()
                .toUri();

       return webClient
               .get()
               .uri(url)
               .retrieve()
               .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                   if(clientResponse.statusCode() == HttpStatus.NOT_FOUND){
                       return Mono.empty();
                   }

                   return clientResponse
                           .bodyToMono(String.class)
                           .flatMap(responseMessage ->
                                   Mono.error(new ReviewsClientException(
                                           responseMessage
                                   ))
                           );
               })
               .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                           log.error("Status code is: {}", clientResponse.statusCode().value());
                           return clientResponse
                                   .bodyToMono(String.class)
                                   .flatMap(responseMessage ->
                                           Mono.error(
                                                   new ReviewsServerException("Server Exception in ReviewsService " + responseMessage)
                                           ));
                       }
               )
                .bodyToFlux(Review.class)
               .retryWhen(getRetrySpec());

    }
}
