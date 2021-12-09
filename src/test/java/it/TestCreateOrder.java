package it;

import static io.restassured.RestAssured.with;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import ibm.eda.demo.ordermgr.domain.Address;
import ibm.eda.demo.ordermgr.infra.api.dto.OrderDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class TestCreateOrder {
    String basicURL = "/api/v1/orders";
    
    @Test
    @Order(1)
    public void createOrder(){
        Address address = new Address("street","city","country","state","zipcode");
        OrderDTO order = new OrderDTO("C01","P01",10,address);

      Response rep = with()
          .headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON)
          .body(order)
          .when().post(basicURL)
          .then()
             .statusCode(200)
             .contentType(ContentType.JSON)
            .extract()
            .response();
            System.out.println(rep.jsonPath().prettyPrint());
            OrderDTO orderOut = rep.body().as(OrderDTO.class);
            Assertions.assertTrue(orderOut.orderID !=  null);
    }
}
