package ws.payper.gateway.service;

import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.model.CryptoCurrency;

public class PaymentOption {

    private PaymentOptionType id;

    private String name;

    private CryptoCurrency currency;

    private String currencyName;

    public PaymentOptionType getId() {
        return id;
    }

    public void setId(PaymentOptionType id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CryptoCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(CryptoCurrency currency) {
        this.currency = currency;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }
}
