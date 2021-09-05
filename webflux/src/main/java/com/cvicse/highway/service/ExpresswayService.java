package com.cvicse.highway.service;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.DecoderHttpMessageReader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpresswayService {

    // this is for single file upload.
    Flux<String> getLines(Mono<FilePart> filePartMono);

    // this is for single file upload.
    Mono<String> getLines(FilePart filePart);
    Mono<String> parseJson(Mono<String> jsonstring);
}
