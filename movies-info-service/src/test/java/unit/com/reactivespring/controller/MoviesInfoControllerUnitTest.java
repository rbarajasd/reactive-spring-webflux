package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MovieInfosServiceController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    public static final String MOVIEINFOS_URL = "/v1/movieinfos/";

    @Test
    void getAllMovieInfos(){

        var movies = List.of( new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "My mom"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2009, List.of("Christian Bale"), LocalDate.parse("2007-06-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(moviesInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movies));


        webTestClient
                .get()
                .uri(MOVIEINFOS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById(){
        var id = "abc";

        var movie = new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(moviesInfoServiceMock.getMovieInfosById(id)).thenReturn(Mono.just(movie));

        webTestClient
                .get()
                .uri(MOVIEINFOS_URL + "{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");

    }

    @Test
    void addMovieInfo() {
        var movieInfo = new MovieInfo(null
                , ""
                , -2022
                , List.of("")
                , LocalDate.parse("2007-06-18"));

        webTestClient
                .post()
                .uri(MOVIEINFOS_URL)
                .bodyValue(movieInfo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(StringEntityExchangeResult ->{
                    var responseBody = StringEntityExchangeResult.getResponseBody();
                    assertNotNull(responseBody);
                    System.out.println(responseBody);
                    String expectedErrorMessage = "movieInfo.cast can not be empty,movieInfo.name is required,movieInfo.year must be a positive number";
                    assertEquals(expectedErrorMessage, responseBody);
                });
    }

    @Test
    void updateMovieInfo() {
        var movieInfo = new MovieInfo("abc", "Dark Knight Rises Reloaded", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        var updatedMovieInfo = new MovieInfo("abc", "mocked movie", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        var id = "abc";

        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(updatedMovieInfo));

        webTestClient
                .put()
                .uri(MOVIEINFOS_URL + "{id}", id)
                .bodyValue(movieInfo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("$.name").isEqualTo(updatedMovieInfo.getName());
    }

    @Test
    void deleteMovieInfo() {
        var id = "abc";

        when(moviesInfoServiceMock.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(MOVIEINFOS_URL + "{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
