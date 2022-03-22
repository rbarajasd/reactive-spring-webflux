package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.mongodb.embedded.version= latest"})
@DirtiesContext
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var movies = List.of( new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "My mom"), LocalDate.parse("2005-06-15")),
                                            new MovieInfo(null, "The Dark Knight", 2009, List.of("Christian Bale"), LocalDate.parse("2007-06-18")),
                                            new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movies).blockLast();
    }

    @Test
    void findAll(){
        //given

        //when
        var movies = movieInfoRepository.findAll();

        //then
        StepVerifier.create(movies)
                .expectNextCount(3L)
                .verifyComplete();
    }

    @Test
    void findById(){
        //given

        //when
        var movies = movieInfoRepository.findById("abc");

        //then
        StepVerifier.create(movies)
                .assertNext(movieInfo -> assertEquals(movieInfo.getName(), "Dark Knight Rises"))
                .verifyComplete();
    }

    @Test
    void findByYear(){
        //given

        //when
        var movies = movieInfoRepository.findByYear(2005);

        //then
        StepVerifier.create(movies)
                .expectNextCount(1L)
                .verifyComplete();
    }

    @Test
    void findByName(){
        //given
        String name = "The Dark Knight";
        //when
        var movies = movieInfoRepository.findByName(name);

        //then
        StepVerifier.create(movies)
                .assertNext(movieInfo -> assertEquals(movieInfo.getName(), name))
                .verifyComplete();
    }

    @Test
    void save(){
        //given
        var movie = new MovieInfo(null, "Batman Begins2", 2005, List.of("Christian Bale", "My mom"), LocalDate.parse("2005-06-15"));
        //when
        var movieInfoMono = movieInfoRepository.save(movie);

        //then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movie.getMovieInfoId());
                    assertEquals(movieInfo.getName(), "Batman Begins2");
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo(){
        //given
        var movies = movieInfoRepository.findById("abc").block();
        movies.setYear(2021);
        //when

        var updatedMono = movieInfoRepository.save(movies);

        //then
        StepVerifier.create(updatedMono)
                .assertNext(movieInfo -> assertEquals(movieInfo.getYear(), 2021))
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo(){
        //given
        movieInfoRepository.deleteById("abc").block();

        //when
        var movies = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(movies)
                .expectNextCount(2)
                .verifyComplete();
    }
//    @AfterAll
//    static void tearDown() {
//        movieInfoRepository.deleteAll().block();
//    }
}