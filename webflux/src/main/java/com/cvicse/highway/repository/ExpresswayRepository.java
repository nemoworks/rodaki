package com.cvicse.highway.repository;

import com.cvicse.highway.domain.TradeInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpresswayRepository extends ElasticsearchRepository<TradeInfo, String> {


  // Flux<ExpresswayTollInfo> saveAll(Flux<ExpresswayTollInfo> just);
}
