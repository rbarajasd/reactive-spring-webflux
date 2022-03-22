package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {
    private static final String REVIEWS_URL = "/v1/reviews";

    @MockBean
    private ReviewReactiveRepository repository;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void addReview(){
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(repository.save(isA(Review.class))).thenReturn(Mono.just(new Review("ars", 1L, "Awesome Movie", 9.0)));

        //when

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(
                        reviewEntityExchangeResult -> {
                            var savedReview = reviewEntityExchangeResult.getResponseBody();
                            assert savedReview != null;
                            assert savedReview.getReviewId() != null;
                        }
                );
        //then
    }

    @Test
    void addReview_badRequest(){
        //given
        var review = new Review(null, null, "Awesome Movie", 9.0);

        //when

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId : must not be null");
        //then
    }

    @Test
    void getReviews(){
        //given
        var reviewsList = List.of(
                new Review("123", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0)
        );
        when(repository.findAll()).thenReturn(Flux.fromIterable(reviewsList));


        //when
        //then
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReview(){
        //given
        var existingReview = new Review("123", 1L, "Awesome Movie", 9.0);
        var newReview = new Review("123", 1L, "Master Piece", 10.0);

        when(repository.findById(isA(String.class))).thenReturn(Mono.just(existingReview));
        when(repository.save(isA(Review.class))).thenReturn(Mono.just(newReview));

        //when
        //then


        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", "123")
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var responseBody = reviewEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getRating() == 10.0;
                    assert responseBody.getComment().equals("Master Piece");
                });

    }

    @Test
    void deleteReview(){
        //given
        var existingReview = new Review("123", 1L, "Awesome Movie", 9.0);

        when(repository.findById(isA(String.class))).thenReturn(Mono.just(existingReview));
        when(repository.deleteById("123")).thenReturn(Mono.empty());

        //when
        //then
        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", "123")
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
