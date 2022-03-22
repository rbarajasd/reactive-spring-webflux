package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import wiremock.org.apache.http.HttpStatus;
import wiremock.org.apache.http.protocol.HTTP;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//@TestPropertySource(properties = {"spring.mongodb.embedded.version= latest"})
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl: http://localhost:8084/v1/movieinfos",
                "restClient.reviewsUrl: http://localhost:8084/v1/reviews"
        }
)

public class MoviesControllerIntgTest {

        @Autowired
        WebTestClient webTestClient;

        @Test
        void getMoviesById(){
                //given
            String movieId = "1";
            stubFor(get(urlEqualTo("/v1/movieinfos".concat("/").concat(movieId)))
                    .willReturn(aResponse()
                    .withHeader(HTTP.CONTENT_TYPE, "Application/json")
                    .withBodyFile("movieinfo.json")));

            stubFor(get(urlPathEqualTo("/v1/reviews"))
                    .willReturn(aResponse()
                            .withHeader(HTTP.CONTENT_TYPE, "Application/json")
                            .withBodyFile("reviews.json")));

                //when
                //then
                webTestClient
                        .get()
                        .uri("/v1/movies/{id}", movieId)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Movie.class)
                        .consumeWith(movieEntityExchangeResult -> {
                           var movie = movieEntityExchangeResult.getResponseBody();
                           assert movie != null;
                           assert movie.getMovieInfo().getName().equals("Batman Begins");
                           assert movie.getReviewList().size() == 2;
                        });

        }

    @Test
    void getMoviesById_movieInfo_404(){
        //given
        String movieId = "1";
        stubFor(get(urlEqualTo("/v1/movieinfos".concat("/").concat(movieId)))
                .willReturn(aResponse()
                        .withStatus((HttpStatus.SC_NOT_FOUND))));

        //when
        //then
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("There is no movie info available for passed in id: 1");

        WireMock.verify(1,getRequestedFor(urlEqualTo("/v1/movieinfos".concat("/").concat(movieId))));
    }

    @Test
    void getMoviesById_reviews_404(){
        //given
        String movieId = "1";
        stubFor(get(urlEqualTo("/v1/movieinfos".concat("/").concat(movieId)))
                .willReturn(aResponse()
                        .withHeader(HTTP.CONTENT_TYPE, "Application/json")
                        .withBodyFile("movieinfo.json")));


        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                       .withStatus(HttpStatus.SC_NOT_FOUND)));

        //when
        //then
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assert movie != null;
                    assert movie.getMovieInfo().getName().equals("Batman Begins");
                    assert movie.getReviewList().size() == 0;
                });
    }

    @Test
    void getMoviesById_reviews_5xx(){
        //given
        String movieId = "1";
        stubFor(get(urlEqualTo("/v1/movieinfos".concat("/").concat(movieId)))
                .willReturn(aResponse()
                        .withHeader(HTTP.CONTENT_TYPE, "Application/json")
                        .withBodyFile("movieinfo.json")));


        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                        .withBody("Reviews Service Unavailable")));

        //when
        //then
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in ReviewsService Reviews Service Unavailable");

        WireMock.verify(4,getRequestedFor(urlPathMatching("/v1/reviews*")));

    }

    @Test
    void getMoviesById_movieInfo_500(){
        //given
        String movieId = "1";
        stubFor(get(urlEqualTo("/v1/movieinfos".concat("/").concat(movieId)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                        .withBody("MovieInfo Service Unavailable")));

        //when
        //then
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var body = movieEntityExchangeResult.getResponseBody();
                    assert body != null;
                    assert body.equals("Server Exception in MovieInfoService MovieInfo Service Unavailable");
                });

        WireMock.verify(4,getRequestedFor(urlEqualTo("/v1/movieinfos".concat("/").concat(movieId))));
    }
}
