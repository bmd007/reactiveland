package io.github.bmd007.reactiveland.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.regex.Pattern;

import static org.apache.kafka.common.config.TopicConfig.*;

@Configuration
@Profile("!test")
public class TopicCreator {

    private final PartitionDef eventTopicDefinition;
    private final PartitionDef changeLogTopicDefinition;
    private final String applicationName;

    public TopicCreator(
            @Value("${spring.application.name}") String applicationName,
            @Value("${kafka.topic.config.event}") String eventTopicDefinition,
            @Value("${kafka.topic.config.changelog}") String changeLogTopicDefinition) {
        this.applicationName = applicationName;
        this.eventTopicDefinition = PartitionDef.parse(eventTopicDefinition);
        this.changeLogTopicDefinition = PartitionDef.parse(changeLogTopicDefinition);

    }

    @Bean
    public NewTopic customerEventsTopic() {
        return new NewTopic(Topics.CUSTOMER_EVENTS_TOPIC, eventTopicDefinition.numPartitions, eventTopicDefinition.replicationFactor)
                .configs(Map.of(RETENTION_MS_CONFIG, "-1", RETENTION_BYTES_CONFIG, "-1"));
    }

    @Bean
    public NewTopic reservationEventsTopic() {
        return new NewTopic(Topics.RESERVATION_EVENTS_TOPIC, eventTopicDefinition.numPartitions, eventTopicDefinition.replicationFactor)
                .configs(Map.of(RETENTION_MS_CONFIG, "-1", RETENTION_BYTES_CONFIG, "-1"));
    }

    public static String stateStoreTopicName(String storeName, String applicationName) {
        return String.format("%s-%s-changelog", applicationName, storeName);
    }

    @Bean
    public NewTopic reservationAggregateChangeLogTopic() {
        return new NewTopic(stateStoreTopicName(StateStores.RESERVATION_STATUS_IN_MEMORY_STATE_STORE, applicationName),
                changeLogTopicDefinition.numPartitions, changeLogTopicDefinition.replicationFactor)
                .configs(Map.of(CLEANUP_POLICY_CONFIG, CLEANUP_POLICY_COMPACT));
    }

    private record PartitionDef(int numPartitions, short replicationFactor) {

        private static final Pattern PATTERN = Pattern.compile("(\\d+):(\\d+)");

        public static PartitionDef parse(String value) {
            var matcher = PATTERN.matcher(value);
            if (matcher.matches()) {
                var numParts = Integer.parseInt(matcher.group(1));
                var repFactor = Short.parseShort(matcher.group(2));
                return new PartitionDef(numParts, repFactor);
            } else {
                throw new IllegalArgumentException("Invalid topic partition definition: " + value);
            }
        }
    }
}
