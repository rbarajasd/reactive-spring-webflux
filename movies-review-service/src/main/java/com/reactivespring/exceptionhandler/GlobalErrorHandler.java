package com.reactivespring.exceptionhandler;

import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable ex) {

        log.error("Exception message is: {}", ex.getMessage(), ex);
        DataBufferFactory dataBufferFactory = serverWebExchange.getResponse().bufferFactory();
        var errorMessage = dataBufferFactory.wrap(ex.getMessage().getBytes());

        if(ex instanceof ReviewDataException){
            serverWebExchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        }else if(ex instanceof ReviewNotFoundException){
            serverWebExchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
        }else{
            serverWebExchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return serverWebExchange.getResponse().writeWith(Mono.just(errorMessage));
    }
}
