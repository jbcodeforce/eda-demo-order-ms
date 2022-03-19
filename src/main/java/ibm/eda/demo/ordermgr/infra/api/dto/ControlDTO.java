package ibm.eda.demo.ordermgr.infra.api.dto;

public class ControlDTO {
    public String backend;
    public int records;
    public String status;

    public ControlDTO(){
    }

    @Override
    public String toString() {
        return "Backend: " + backend + " with " + records + " records";
    }
}
