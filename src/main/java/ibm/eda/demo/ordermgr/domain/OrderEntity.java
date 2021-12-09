package ibm.eda.demo.ordermgr.domain;

public class OrderEntity {
	public static final String PENDING_STATUS = "pending";
    public static final String CANCELLED_STATUS = "cancelled";
    public static final String ASSIGNED_STATUS = "assigned";
    public static final String BOOKED_STATUS = "booked";
    public static final String REJECTED_STATUS = "rejected";
    public static final String COMPLETED_STATUS = "completed";
    
    public String orderID;
    public String productID;
    public String customerID;
    public int quantity;
    public Address deliveryAddress;
    public String status;
	public String creationDate;
	public String updateDate;
    
	public OrderEntity(){}
	
	public OrderEntity(String orderID, String productID, String customerID, int quantity, Address deliveryAddress,
			String creationDate, String status) {
		super();
		this.orderID = orderID;
		this.productID = productID;
		this.customerID = customerID;
		this.quantity = quantity;
		this.deliveryAddress = deliveryAddress;
		this.creationDate = creationDate;
		this.status = status;
	}
	
	

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getOrderID() {
		return orderID;
	}
	public String getProductID() {
		return productID;
	}
	public String getCustomerID() {
		return customerID;
	}
	public int getQuantity() {
		return quantity;
	}
	public Address getDeliveryAddress() {
		return deliveryAddress;
	}
}