package com.example.giftyfy;

public class ReceivedGift {
    private String productId;
    private String senderName;
    private String date;

    public ReceivedGift() {}

    public ReceivedGift(String productId, String senderName, String date) {
        this.productId = productId;
        this.senderName = senderName;
        this.date = date;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}