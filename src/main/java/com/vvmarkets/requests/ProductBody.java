package com.vvmarkets.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//        "sell_price": 22,
//        "product_id": "478bd4d4-1d4b-4003-9934-36c83f1c85a9",
//        "quantity": 2,
//        "discount_percent": 0
public class ProductBody {
    @SerializedName("sell_price")
    @Expose
    private Double SellPrice;

    @SerializedName("product_id")
    @Expose
    private String ProductId;

    @SerializedName("quantity")
    @Expose
    private Double Quantity;

    @SerializedName("discount_percent")
    @Expose
    private int DiscountPercent;

    public Double getSellPrice() {
        return SellPrice;
    }

    public void setSellPrice(Double sellPrice) {
        SellPrice = sellPrice;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public Double getQuantity() {
        return Quantity;
    }

    public void setQuantity(Double quantity) {
        Quantity = quantity;
    }

    public int getDiscountPercent() {
        return DiscountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        DiscountPercent = discountPercent;
    }
}