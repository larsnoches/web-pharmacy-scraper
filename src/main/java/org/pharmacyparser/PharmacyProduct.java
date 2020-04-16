package org.pharmacyparser;

public class PharmacyProduct {
    private String name = "";
    private String price = "";
    private String pharmacyLabel = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPharmacyLabel() {
        return pharmacyLabel;
    }

    public void setPharmacyLabel(String pharmacyLabel) {
        this.pharmacyLabel = pharmacyLabel;
    }
}
