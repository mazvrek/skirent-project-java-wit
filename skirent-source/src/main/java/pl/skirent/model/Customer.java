package pl.skirent.model;

public class Customer {
    private int id;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private String description;

    public Customer() {}

    public Customer(int id, String firstName, String lastName, String documentNumber, String description) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentNumber = documentNumber;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() { return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""); }
}
