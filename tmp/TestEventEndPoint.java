import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.CommonClientConfigs;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import java.io.File;
import java.io.ByteArrayInputStream;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.common.config.SaslConfigs;

public class TestEventEndPoint {
  public static final void main(String args[]) {  
    Schema.Parser schemaDefinitionParser = new Schema.Parser();
    Schema schema = null;
    try {
      schema = schemaDefinitionParser.parse(new File("edademo-orders.avsc"));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    GenericDatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(schema);

    Properties props = new Properties();

    props.put("bootstrap.servers", "eepm-eda-egw-event-gw-client-eepm-eda.garage-to-60b41835e65227550a2031aa4f2061fc-0000.ca-tor.containers.appdomain.cloud:443");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

    props.put("group.id", "1");
    props.put("client.id", "58a2b9d0-c60c-4c18-b49d-af1f84e989ec");

    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");

    props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
    props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"placeholder_credential\" password=\"<SASL_password>\";");

    KafkaConsumer consumer = new KafkaConsumer<String, byte[]>(props);
    consumer.subscribe(Collections.singletonList("edademo-orders"));
    try {
      while(true) {
        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1));
        for (ConsumerRecord<String, byte[]> record : records) {
            byte[] value = record.value();
            String key = record.key();
            ByteArrayInputStream bais = new ByteArrayInputStream(value);
            Decoder decoder = DecoderFactory.get().binaryDecoder(bais, null);
            GenericRecord genericRecord;
            genericRecord = reader.read(null, decoder);
            // Do something with your record
            Object somefield = genericRecord.get("field-from-your-schema");
          }
        }
    } catch (Exception e) {
      e.printStackTrace();
      consumer.close();
      System.exit(1);
    }   
  }
}
