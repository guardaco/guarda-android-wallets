package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class CoinifyBankAcc {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("account")
    @Expose
    private Account account;
    @SerializedName("bank")
    @Expose
    private Bank bank;
    @SerializedName("holder")
    @Expose
    private Holder holder;

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

    public Account getAccount() {
        return account;
    }

    public Bank getBank() {
        return bank;
    }

    public Holder getHolder() {
        return holder;
    }
}
