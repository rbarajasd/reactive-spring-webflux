package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.mongodb.embedded.version= latest"})
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    private static final String REVIEWS_URL = "/v1/reviews";
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository repository;

    @BeforeEach
    void setUp(){
        var reviewsList = List.of(
                new Review("123", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0)
        );
        repository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown(){
        repository.deleteAll().block();
    }

    @Test
    void addReview(){
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

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
    void getAllReviews(){
        //given
        //when

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(3);
        //then
    }

    @Test
    void getReviewsByMovieInfoId(){
        //given
        var uri = UriComponentsBuilder
                .fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", 1L)
                .buildAndExpand()
                .toUri();
        //when

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(2);
        //then
    }

    @Test
    void updateReview(){
        //given
        var newReview = new Review("123", 1L, "Excellent Movie", 10.0);

        //when

        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", newReview.getReviewId())
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(
                        reviewEntityExchangeResult -> {
                            var updatedReview = reviewEntityExchangeResult.getResponseBody();
                            assert updatedReview != null;
                            assert updatedReview.getComment().equals(newReview.getComment());
                            assert updatedReview.getRating().equals(newReview.getRating());
                        }
                );
        //then
    }

    @Test
    void deleteReview(){
        //given
        var reviewId = "123";
        //when

        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
        //then
    }
}
