package ibm.eda.demo.ordermgr.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import ibm.eda.demo.ordermgr.infra.events.Address;
import ibm.eda.demo.ordermgr.infra.events.OrderEvent;
import ibm.eda.demo.ordermgr.infra.repo.OrderRepository;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;


@ApplicationScoped
public class OrderService {
	private static final Logger logger = Logger.getLogger(OrderService.class.getName());
	private static final String[] possibleStates = { "Pending", "Assigned", "Closed", "Booked", "Rejected","Cancelled", "OnHold"};
	@ConfigProperty(name="mp.messaging.outgoing.orders.topic")
	public String topicName;
	@ConfigProperty(name="app.pace_ms")
	public int pace_ms;
	@Inject
	public OrderRepository repository;
    @Channel("orders")
	public Emitter<OrderEvent> eventProducer;
	
	public OrderService(){}
	
	public OrderEntity createOrder(OrderEntity order) {
		if (order.creationDate == null) {
			order.creationDate = LocalDate.now().toString();
		}
		order.updateDate= order.creationDate;
		repository.addOrder(order);
		Address deliveryAddress = new Address(order.getDeliveryAddress().getStreet()
				,order.getDeliveryAddress().getCity()
				,order.getDeliveryAddress().getCountry()
				,order.getDeliveryAddress().getState(),
				order.getDeliveryAddress().getZipcode());
		OrderEvent orderPayload =
		 new OrderEvent(order.getOrderID(),
				order.getProductID(),
				order.getCustomerID(),
				order.getQuantity(),
				order.getStatus(),
		        order.creationDate,
				order.updateDate,
				deliveryAddress,
				"OrderCreatedEvent");	
		try {
			
			/* another way
			Message<OrderEvent> record = KafkaRecord.of(order.getCustomerID(),orderPayload);
			eventProducer.send(record);
			*/
			sendOrder(orderPayload);
			logger.info("order created event sent for " + order.getOrderID() + " to topic " + topicName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return order;
	}

	public List<OrderEntity> getAllOrders() {
		return repository.getAll();
	}

    public OrderEntity updateOrder(OrderEntity order) {
		order.updateDate = LocalDate.now().toString();
		repository.updateOrder(order);
		Address deliveryAddress = null;
		if (order.getDeliveryAddress() !=null) {
			deliveryAddress = new Address(order.getDeliveryAddress().getStreet()
			,order.getDeliveryAddress().getCity()
			,order.getDeliveryAddress().getCountry()
			,order.getDeliveryAddress().getState(),
			order.getDeliveryAddress().getZipcode());
		}
		
		OrderEvent orderPayload =
		 new OrderEvent(order.getOrderID(),
				order.getProductID(),
				order.getCustomerID(),
				order.getQuantity(),
				order.getStatus(),
		        order.creationDate,
				order.updateDate,
				deliveryAddress,
				"OrderUpdateEvent");	
			try {
				logger.info("emit order updated event for " + order.getOrderID());
				sendOrder(orderPayload);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return order;
    }

    public OrderEntity findById(String id) {
        return repository.findById(id);
    }

    public void startSimulation(String backend, int records) {
		// backend if for future
		Random r = new Random();
		for (int o = 0; o < records; o++){
			String newState = possibleStates[r.nextInt(possibleStates.length)];
			String eventType = "OrderUpdateEvent";
			if ("Closed".equals(newState)) {
				eventType= "OrderCloseEvent";
			}
			if ("Pending".equals(newState)) {
				eventType= "OrderCreatedEvent";
			}
			Address deliveryAddress = new Address(
				"City_" + r.nextInt(5),
				"USA",
				"CA",Integer.toString(r.nextInt(50)) + " main street","95051");
			OrderEvent orderPayload =  new OrderEvent("Order_" + o,
			"P0" + r.nextInt(5),
			"C0" + r.nextInt(2000),
			r.nextInt(10),
			newState,
			LocalDate.now().toString(),
			LocalDate.now().toString(),
			deliveryAddress,
			eventType);
			logger.info("emit order updated event for " + orderPayload.getOrderID());
			sendOrder(orderPayload);
			try {
				Thread.currentThread().sleep(pace_ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }

	public void sendOrder(OrderEvent orderPayload){
		eventProducer.send(Message.of(orderPayload).addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
			.withKey(orderPayload.getCustomerID()).build())
			.withAck( () -> {
				
				return CompletableFuture.completedFuture(null);
			})
			.withNack( throwable -> {
				return CompletableFuture.completedFuture(null);
			}));
	}
	
}
