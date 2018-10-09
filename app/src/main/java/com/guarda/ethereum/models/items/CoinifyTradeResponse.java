package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class CoinifyTradeResponse {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("inAmount")
    @Expose
    private String inAmount;
    @SerializedName("outAmountExpected")
    @Expose
    private String outAmountExpected;
    @SerializedName("transferIn")
    @Expose
    private TransferIn transferIn;


    public class TransferIn {
        @SerializedName("details")
        @Expose
        private Details details;
        @SerializedName("sendAmount")
        @Expose
        private Float sendAmount;

        public Details getDetails() {
            return details;
        }

        public void setDetails(Details details) {
            this.details = details;
        }

        public Float getSendAmount() {
            return sendAmount;
        }
    }

    public class Details {
        //For Card payment
        @SerializedName("redirectUrl")
        @Expose
        private String redirectUrl;
        //For Bank transfer
        @SerializedName("referenceText")
        @Expose
        private String referenceText;
        @SerializedName("bank")
        @Expose
        private Bank bank;
        @SerializedName("holder")
        @Expose
        private Holder holder;
        @SerializedName("account")
        @Expose
        private Account account;
        @SerializedName("returnUrl")
        @Expose
        private String returnUrl;

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public String getReferenceText() {
            return referenceText;
        }

        public Bank getBank() {
            return bank;
        }

        public Holder getHolder() {
            return holder;
        }

        public Account getAccount() {
            return account;
        }

        public String getReturnUrl() {
            return returnUrl;
        }
    }

    public class Bank {
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("address")
        @Expose
        private Address address;

        public String getName() {
            return name;
        }

        public Address getAddress() {
            return address;
        }
    }

    public class Holder {
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("address")
        @Expose
        private Address address;

        public String getName() {
            return name;
        }

        public Address getAddress() {
            return address;
        }
    }

    public class Address {
        @SerializedName("street")
        @Expose
        private String street;
        @SerializedName("zipcode")
        @Expose
        private String zipcode;
        @SerializedName("city")
        @Expose
        private String city;
        @SerializedName("state")
        @Expose
        private String state;
        @SerializedName("country")
        @Expose
        private String country;

        public String getState() {
            return state;
        }

        public String getCity() {
            return city;
        }

        public String getStreet() {
            return street;
        }

        public String getCountry() {
            return country;
        }

        public String getZipcode() {
            return zipcode;
        }
    }

    public class Account {
        @SerializedName("bic")
        @Expose
        private String bic;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("number")
        @Expose
        private String number;
        @SerializedName("currency")
        @Expose
        private String currency;

        public String getBic() {
            return bic;
        }

        public String getType() {
            return type;
        }

        public String getNumber() {
            return number;
        }

        public String getCurrency() {
            return currency;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInAmount() {
        return inAmount;
    }

    public void setInAmount(String inAmount) {
        this.inAmount = inAmount;
    }

    public String getOutAmountExpected() {
        return outAmountExpected;
    }

    public void setOutAmountExpected(String outAmountExpected) {
        this.outAmountExpected = outAmountExpected;
    }

    public TransferIn getTransferIn() {
        return transferIn;
    }

    public void setTransferIn(TransferIn transferIn) {
        this.transferIn = transferIn;
    }
}
