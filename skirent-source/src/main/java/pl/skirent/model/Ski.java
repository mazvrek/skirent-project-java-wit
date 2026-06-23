package pl.skirent.model;

public class Ski {
    private int id;
    private int skiTypeId;
    private String brand;
    private String model;
    private String bindings;
    private double length;

    public Ski() {}

    public Ski(int id, int skiTypeId, String brand, String model, String bindings, double length) {
        this.id = id;
        this.skiTypeId = skiTypeId;
        this.brand = brand;
        this.model = model;
        this.bindings = bindings;
        this.length = length;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSkiTypeId() { return skiTypeId; }
    public void setSkiTypeId(int skiTypeId) { this.skiTypeId = skiTypeId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getBindings() { return bindings; }
    public void setBindings(String bindings) { this.bindings = bindings; }
    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }

    @Override
    public String toString() { return (brand != null ? brand : "") + " " + (model != null ? model : ""); }
}
