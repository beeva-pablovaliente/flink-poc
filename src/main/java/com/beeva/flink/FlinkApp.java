package com.beeva.flink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.json.JSONParseFlatMap;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Properties;

/**
 *
 * Created by rekkeb on 10/4/16.
 */
@SpringBootApplication
public class FlinkApp implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(FlinkApp.class);


    @Override
    public void run(String... args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //Prepare twitter data
        DataStream<String> streamSource =
                env.addSource(new TwitterSource(System.getProperty("user.dir") + "/src/main/resources/twitter.properties"));

        DataStream<String> dataStream = streamSource
                .flatMap(new JSONParseFlatMap<String, String>() {
                    @Override
                    public void flatMap(String s, Collector<String> collector) throws Exception {
                        collector.collect(s);
                    }
                })
                ;

        dataStream.print();

        env.execute("Twitter Streaming");

    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(FlinkApp.class).bannerMode(Banner.Mode.OFF).run(args);
    }

}