package com.beeva.flink;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.json.JSONParseFlatMap;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer09;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 *
 * Created by rekkeb on 10/4/16.
 */
@SpringBootApplication
public class FlinkApp implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(FlinkApp.class);


    @Override
    public void run(String... args) throws Exception {

        //Obtain the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //Load the initial data. Prepare twitter data
        DataStream<String> streamSource =
                env.addSource(new TwitterSource(System.getProperty("user.dir") + "/src/main/resources/twitter.properties"));

        //Specify transformation on data
        DataStream<String> dataStream = streamSource
                .flatMap(new JSONParseFlatMap<String, String>() {
                    @Override
                    public void flatMap(String s, Collector<String> collector) throws Exception {
                        collector.collect(s);
                    }
                })
                ;

        //Specify Where to put the results
        dataStream.addSink(new FlinkKafkaProducer09<String>("localhost:9092", "flink-topic", new SimpleStringSchema()));

        dataStream.print();

        //Trigger the program execution
        env.execute("Twitter Streaming");

    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(FlinkApp.class).bannerMode(Banner.Mode.OFF).run(args);
    }

}