package com.cvicse.highway.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.cvicse.highway.domain.TradeInfo;
import com.cvicse.highway.repository.ExpresswayRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.Null;

import static com.cvicse.highway.HighwayApplication.geoMap;

import com.cvicse.highway.domain.KafkaConfig;
import com.cvicse.highway.domain.Msg;

@Service
public class ExpresswayServiceImpl implements ExpresswayService {
    private final ExpresswayRepository expresswayRepository;

    private final ApplicationContext context;
    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final KafkaConfig kafkaConfig;

    public ExpresswayServiceImpl(ExpresswayRepository expresswayRepository, ApplicationContext context,
            KafkaTemplate<Integer, String> kafkaTemplate, KafkaConfig kafkaConfig) {
        this.expresswayRepository = expresswayRepository;
        this.context = context;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaConfig = kafkaConfig;
    }

    @Override
    public Flux<String> getLines(Mono<FilePart> filePartMono) {
        return filePartMono.flatMapMany(this::getLines);
    }

    @Override
    public Mono<String> getLines(FilePart filePart) {
        return filePart.content().flatMap(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            String content = new String(bytes, StandardCharsets.UTF_8);
            return Mono.just(content);
        }).map(this::processAndGetLinesAsList).flatMapIterable(Function.identity()).collectList()
                .delayElement(Duration.ofMillis(20)).map(this::handleString)
                // .map(this::downloadFile)
                .map(this::saveFile);
    }

    private String handleString(List<String> strings) {
        String s = "";
        for (String s1 : strings) {
            s += s1;
        }
        return s;

    }

    private String savejson(String s) {
        System.out.println(s);
        JSONObject jsonObject = JSONObject.parseObject(s);

        // System.out.println(jsonObject);
        // System.out.println(jsonObject.get("备用字段7"));
        return "ok";
    }

    private String downloadFile(String s) {
        try {
            File file1 = new File("javaio-appendfile.txt");

            // if file doesnt exists, then create it
            if (!file1.exists()) {
                file1.createNewFile();
            }

            // true = append file
            FileWriter fileWritter = new FileWriter(file1.getName(), true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(s);
            bufferWritter.close();
            // file1.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    private String saveFile(String s) {
        //JSONObject jsonObject = JSONObject.parseObject(s);
        //System.out.print(jsonObject);
        // System.out.print(this.kafkaConfig.topicInput);
        ListenableFuture<SendResult<Integer, String>> future = kafkaTemplate.send(this.kafkaConfig.topicInput, s);
        // JSONArray jsonArray = jsonObject.getJSONArray("tradeInfoList");
        future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                // System.out
                //         .println("Sent message=[" + s + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                // System.out.println("Unable to send message=[" + s + "] due to : " + ex.getMessage());
            }
        });
        // List<TradeInfo> tradeInfos =
        // JSONObject.parseArray(jsonArray.toJSONString(),TradeInfo.class);
        // tradeInfos.forEach(tradeInfo->{
        // // List<Float> loc = geoMap.get(tradeInfo.getgantryId());
        // // if(loc==null){
        // // loc=Arrays.asList(0.0f,0.0f);
        // // }
        // // float[] tmp = {loc.get(0),loc.get(1)};
        // Msg loc = geoMap.get(tradeInfo.getgantryId());
        // if(loc==null){
        // loc=new Msg(0.0f,0.0f);
        // }
        // float[] tmp = {loc.longitude,loc.latitude};
        // tradeInfo.setGeo(tmp);
        // expresswayRepository.save(tradeInfo);
        // });
        return "ok";
    }

    private List<String> processAndGetLinesAsList(String string) {

        Supplier<Stream<String>> streamSupplier = string::lines;

        return streamSupplier.get().filter(s -> !s.isBlank()).collect(Collectors.toList());
    }

    @Override
    public Mono<String> parseJson(Mono<String> jsonstring) {
        // TODO Auto-generated method stub
        return jsonstring.map(this::savejson);
    }
}
