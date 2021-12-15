package ibm.eda.demo.ordermgr.domain;

public class OrderEntity {
	public static final String PENDING_STATUS = "pending";
    public static final String CANCELLED_STATUS = "cancelled";
    public static final String ASSIGNED_STATUS = "assigned";
    public static final String BOOKED_STATUS = "booked";
    public static final String REJECTED_STATUS = "rejected";
    public static final String COMPLETED_STATUS = "completed";
    
    public String transactionID;
    public String upc;
    public String customerID;
    public int quantity;
    public int unit_price;
	public String trans_dt;
	public String store_num;
    
	public OrderEntity(){}
	
}
