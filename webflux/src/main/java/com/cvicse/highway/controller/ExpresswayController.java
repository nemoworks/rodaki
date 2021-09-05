package com.cvicse.highway.controller;

import com.alibaba.fastjson.JSONObject;
import com.cvicse.highway.repository.ExpresswayRepository;
import com.cvicse.highway.service.ExpresswayService;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class ExpresswayController {

    private final ExpresswayRepository expresswayRepository;

    private final ExpresswayService expresswayService;

    private final ReactiveGridFsTemplate gridFsTemplate;

    public ExpresswayController(ExpresswayRepository expresswayRepository, ExpresswayService expresswayService,
            ReactiveGridFsTemplate gridFsTemplate) {
        this.expresswayRepository = expresswayRepository;
        this.expresswayService = expresswayService;
        this.gridFsTemplate = gridFsTemplate;
    }

    @PostMapping(value = "/upload-filePart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Mono<String> upload(@RequestPart("file") FilePart filePart) {

            return expresswayService.getLines(filePart);


    }

    @PostMapping(value = "/upload-json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Mono<String> uploadjson(@RequestBody Mono<String> jsonstring) {
        return expresswayService.parseJson(jsonstring);
    }

    @PostMapping("")
    public Mono<String> upload(@RequestPart("file") Mono<FilePart> fileParts) {
        return fileParts.flatMap(part -> this.gridFsTemplate.store(part.content(), part.filename()))
                .map(objectId -> "ok");
    }
}
