package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.mongodb.embedded.version= latest"})
@AutoConfigureWebTestClient
class MovieInfosServiceControllerIntgTest {

    public static final String MOVIEINFOS_URL = "/v1/movieinfos/";
    @Autowired
    private WebTestClient webClientTest;

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {

        var movies = List.of( new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "My mom"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2009, List.of("Christian Bale"), LocalDate.parse("2007-06-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movies).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        var movieInfo = new MovieInfo(null, "The Dark Knight", 2009, List.of("Christian Bale"), LocalDate.parse("2007-06-18"));

        webClientTest
                .post()
                .uri(MOVIEINFOS_URL)
                .bodyValue(movieInfo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult ->{
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo);
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo_validation() {
        var movieInfo = new MovieInfo(null
                , ""
                , null
                , List.of("Christian Bale")
                , LocalDate.parse("2007-06-18"));

        webClientTest
                .post()
                .uri(MOVIEINFOS_URL)
                .bodyValue(movieInfo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult ->{
                    var responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(responseBody);
                    System.out.println(responseBody.toString());
                });
    }

    @Test
    void getAllMovieInfos(){
        webClientTest
                .get()
                .uri(MOVIEINFOS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoByYear(){
        var uri = UriComponentsBuilder
                .fromUriString(MOVIEINFOS_URL)
                .queryParam("year", 2005)
                .buildAndExpand()
                .toUri();

        webClientTest
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getMovieInfosById(){
        var id = "abc";
        webClientTest
                .get()
                .uri(MOVIEINFOS_URL + "{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
//                .expectBody(MovieInfo.class)
//                .consumeWith(movieInfoEntityExchangeResult -> {
//                    var response = movieInfoEntityExchangeResult.getResponseBody();
//                    assertEquals(response.getName(), "Dark Knight Rises");
//                });
    }

    @Test
    void getMovieInfosById_NotFound(){
        var id = "def";
        webClientTest
                .get()
                .uri(MOVIEINFOS_URL + "{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void updateMovieInfo() {
        var movieInfo = new MovieInfo("abc", "Dark Knight Rises Reloaded", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        var id = "abc";

        webClientTest
                .put()
                .uri(MOVIEINFOS_URL + "{id}", id)
                .bodyValue(movieInfo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises Reloaded");
    }

    @Test
    void updateMovieInfo_notFound() {
        var movieInfo = new MovieInfo("abc", "Dark Knight Rises Reloaded", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        var id = "def";

        webClientTest
                .put()
                .uri(MOVIEINFOS_URL + "{id}", id)
                .bodyValue(movieInfo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfo() {
        var id = "abc";

        webClientTest
                .delete()
                .uri(MOVIEINFOS_URL + "{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void stream(){
        //given
        var movieInfo = new MovieInfo(null, "The Dark Knight", 2009, List.of("Christian Bale"), LocalDate.parse("2007-06-18"));

        webClientTest
                .post()
                .uri(MOVIEINFOS_URL)
                .bodyValue(movieInfo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult ->{
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo);
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                });

        var movieStreamFlux = webClientTest
                .get()
                .uri(MOVIEINFOS_URL.concat("/stream"))
                .exchange()
                .expectStatus().isOk()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        //when
        //then

        StepVerifier.create(movieStreamFlux)
                .assertNext(movieInfo1 -> {
                    assert movieInfo1.getMovieInfoId() != null;
                })
                .thenCancel()
                .verify();
    }
}