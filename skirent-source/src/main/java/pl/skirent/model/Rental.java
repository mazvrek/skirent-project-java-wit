package pl.skirent.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Rental {
    private int id;
    private int customerId;
    private List<Integer> skiIds;
    private LocalDateTime from;
    private LocalDateTime to;
    private String status;
    private String remarks;

    public Rental() {
        this.skiIds = new ArrayList<>();
        this.status = "ACTIVE";
    }

    public Rental(int id, int customerId, List<Integer> skiIds, LocalDateTime from, LocalDateTime to, String status, String remarks) {
        this.id = id;
        this.customerId = customerId;
        this.skiIds = skiIds != null ? new ArrayList<>(skiIds) : new ArrayList<>();
        this.from = from;
        this.to = to;
        this.status = status;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public List<Integer> getSkiIds() { return skiIds; }
    public void setSkiIds(List<Integer> skiIds) { this.skiIds = skiIds; }
    public LocalDateTime getFrom() { return from; }
    public void setFrom(LocalDateTime from) { this.from = from; }
    public LocalDateTime getTo() { return to; }
    public void setTo(LocalDateTime to) { this.to = to; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
