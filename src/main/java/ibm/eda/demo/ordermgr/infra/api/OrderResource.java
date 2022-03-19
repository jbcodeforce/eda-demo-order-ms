package ibm.eda.demo.ordermgr.infra.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import ibm.eda.demo.ordermgr.domain.OrderEntity;
import ibm.eda.demo.ordermgr.domain.OrderService;
import ibm.eda.demo.ordermgr.infra.api.dto.ControlDTO;
import ibm.eda.demo.ordermgr.infra.api.dto.OrderDTO;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/api/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OrderResource {
    private static final Logger logger = Logger.getLogger(OrderResource.class.getName());

    @Inject
    public OrderService service;
    
    @GET
    public Multi<OrderDTO> getAllActiveOrders() {
        List<OrderDTO> l = new ArrayList<OrderDTO>();
        for (OrderEntity order : service.getAllOrders()) {
            l.add(OrderDTO.fromEntity(order));
        }
        return Multi.createFrom().items(l.stream());
    }

    @GET
    @Path("/{id}")
    public Uni<OrderDTO> get(@PathParam("id") String id) {
        logger.info("In get order with id: " + id);
        OrderEntity order = service.findById(id);
        if (order == null) {
            throw new WebApplicationException("Order with id of " + id + " does not exist.", 404);
     
        }
        return Uni.createFrom().item(OrderDTO.fromEntity(order));
    }

    @POST
    @Counted(name = "performedNewOrderCreation", description = "How many post new order have been performed.")
    @Timed(name = "checksTimer", description = "A measure of how long it takes to perform the operation.", unit = MetricUnits.MILLISECONDS)
    public Uni<OrderDTO> saveNewOrder(OrderDTO order) {
        logger.info("POST operation " + order.toString());
        OrderEntity entity = OrderDTO.toEntity(order);
        return Uni.createFrom().item(OrderDTO.fromEntity(service.createOrder(entity)));
    }

    @PUT
    public Uni<OrderDTO> updateExistingOrder(OrderDTO order) {
        logger.info("PUT operation " + order.toString());
        OrderEntity entity = OrderDTO.toEntity(order);
        return Uni.createFrom().item(OrderDTO.fromEntity(service.updateOrder(entity)));
    }

    @POST
    @Path("/control")
    public Uni<ControlDTO> startSimulation(ControlDTO control) {
        service.startSimulation(control.backend,control.records);
        control.status = "Started";
        return Uni.createFrom().item(control);
    }
    
}