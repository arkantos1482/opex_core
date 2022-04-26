package co.nilin.opex.matching.engine.ports.kafka.submitter.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import java.util.function.Supplier

@Configuration
class KafkaTopicConfig {

    @Value("\${spring.app.symbols}")
    private lateinit var symbols: String

    @Autowired
    fun createTopics(applicationContext: GenericApplicationContext) {
        symbols.split(",")
            .map { s -> "orders_$s" }
            .forEach { topic ->
                applicationContext.registerBean("topic_${topic}", NewTopic::class.java, Supplier {
                    TopicBuilder.name(topic)
                        .partitions(10)
                        .replicas(3)
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                        .build()
                })
            }

        symbols.split(",")
            .map { s -> "events_$s" }
            .forEach { topic ->
                applicationContext.registerBean("topic_${topic}", NewTopic::class.java, Supplier {
                    TopicBuilder.name(topic)
                        .partitions(10)
                        .replicas(3)
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                        .build()
                })
            }

        symbols.split(",")
            .map { s -> "trades_$s" }
            .forEach { topic ->
                applicationContext.registerBean("topic_${topic}", NewTopic::class.java, Supplier {
                    TopicBuilder.name(topic)
                        .partitions(10)
                        .replicas(3)
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                        .build()
                })
            }
    }

}