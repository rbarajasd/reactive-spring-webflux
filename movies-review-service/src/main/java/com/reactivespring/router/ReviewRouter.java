package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class ReviewRouter {

    private final ReviewHandler handler;

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler){

        return route()
                .nest(path("/v1/reviews"), builder -> {
                    builder
                            .POST("", reviewHandler::addReview)
                            .GET("", reviewHandler::getReviews)
                            .PUT("/{id}", reviewHandler::updateReview)
                            .DELETE("/{id}", reviewHandler::deleteById)
                            .GET("/stream", reviewHandler::getReviewsStream);
                })
                .GET("/v1/helloworld", serverRequest -> ServerResponse.ok().bodyValue("helloworld"))
                .build();
    }
}
