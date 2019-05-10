package ws.payper.gateway.service;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.model.CryptoCurrency;
import ws.payper.gateway.web.ConfigureLinkController;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ws.payper.gateway.service.ValidationException.Arg;
import static ws.payper.gateway.service.ValidationException.Error;

@Component
public class ValidatorService {

    public void newLink(ConfigureLinkController.LinkConfig link) {
        Optional<Error> url = checkUrl(link.getUrl());
        Optional<Error> title = checkTitle(link.getTitle());
        Optional<Error> description = checkDescription(link.getDescription());
        Optional<Error> pseudonym = checkPseudonym(link.getPseudonym());
        Optional<Error> price = checkPrice(link.getPrice());
        List<Optional<Error>> paymentArgs = checkPaymentArgs(link.getPaymentOptionType(), link.getPaymentOptionArgs(), link.getCurrency());

        List<Optional<Error>> fields = List.of(url, title, description, pseudonym, price);
        List<Optional<Error>> allErrors = new ArrayList<>();
        allErrors.addAll(fields);
        allErrors.addAll(paymentArgs);

        List<Error> errors = allErrors.stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (!errors.isEmpty()) {
            throw new ValidationException("Config link validation errors", errors);
        }
    }

    private Optional<Error> checkUrl(String url) {
        Optional<Error> result = Optional.empty();
        try {
            new URL(url);
            return result;
        } catch (MalformedURLException e) {
            Error error = new Error("linkconfig.url.invalid");
            result = Optional.of(error);
        }

        return result;
    }

    private Optional<Error> checkTitle(String title) {
        int titleMaxLength = 100;

        Optional<Error> result = Optional.empty();
        if (StringUtils.isBlank(title)) {
            Error error = new Error("linkconfig.title.mandatory");
            result = Optional.of(error);
        } else {
            if (title.length() > titleMaxLength) {
                Error error = new Error("linkconfig.title.too-long",
                        Arg.of("value", title), Arg.of("max", String.valueOf(titleMaxLength)));
                result = Optional.of(error);
            }
        }
        return result;
    }

    private Optional<Error> checkDescription(String description) {
        int descriptionMaxLength = 200;

        Optional<Error> result = Optional.empty();
        if (description.length() > descriptionMaxLength) {
            Error error = new Error("linkconfig.description.too-long",
                    Arg.of("value", description), Arg.of("max", String.valueOf(descriptionMaxLength)));
            result = Optional.of(error);
        }
        return result;
    }

    private Optional<Error> checkPseudonym(String pseudonym) {
        return Optional.empty();
    }

    private Optional<Error> checkPrice(BigDecimal price) {
        Optional<Error> result = Optional.empty();
        if (price == null) {
            Error error = new Error("linkconfig.price.mandatory");
            result = Optional.of(error);
        } else if (price.compareTo(BigDecimal.ZERO) < 1) {
            Error error = new Error("linkconfig.price.not-positive",
                    Arg.of("value", price.toString()));
            result = Optional.of(error);
        }
        return result;
    }

    private Map<PaymentOptionType, CryptoCurrency> currencies = Map.of(
            PaymentOptionType.DUMMY_COIN, CryptoCurrency.DUMMY_COIN,
            PaymentOptionType.LIGHTNING_BTC, CryptoCurrency.SATOSHI_BTC,
            PaymentOptionType.HEDERA_HBAR_INVOICE, CryptoCurrency.HBAR
    );

    private List<Optional<Error>> checkPaymentArgs(PaymentOptionType paymentOptionType, Map<String, String> args, CryptoCurrency currency) {
        if (paymentOptionType == null) {
            Error error = new Error("linkconfig.paymentOptionType.missing");
            return List.of(Optional.of(error));
        }
        if (currency == null) {
            Error error = new Error("linkconfig.currency.missing");
            return List.of(Optional.of(error));
        }
        if (!currency.equals(currencies.get(paymentOptionType))) {
            Error error = new Error("linkconfig.currency.invalid",
                    Arg.of("paymentOptionType", paymentOptionType.name()),
                    Arg.of("currency", currency.name()));
            return List.of(Optional.of(error));
        }

        if (PaymentOptionType.LIGHTNING_BTC.equals(paymentOptionType)) {
            Optional<Error> ip = checkIpOrHost(args.get("pubkeyHost"));
            Optional<Error> port = checkPort(args.get("rpcport"));
            Optional<Error> cert = checkCert(args.get("tlsCert"));
            Optional<Error> macaroon = checkMacaroon(args.get("invoiceMacaroon"));
            return List.of(ip, port, cert, macaroon);
        }
        return List.of(Optional.empty());
    }

    private Optional<Error> checkIpOrHost(String pubkeyHost) {
        Optional<Error> result = Optional.empty();
        if (!InetAddresses.isInetAddress(pubkeyHost) && !InternetDomainName.isValid(pubkeyHost)) {
            Error error = new Error("linkconfig.paymentOption.LIGHTNING_BTC.ipOrHost.missing");
            result = Optional.of(error);
        }
        return result;
    }

    private Optional<Error> checkPort(String rpcport) {
        Optional<Error> result = Optional.empty();
        boolean invalid = true;
        if (StringUtils.isNumeric(rpcport)) {
            try {
                int parsedPort = Integer.parseInt(rpcport);
                if (parsedPort >= 0 || parsedPort <= 65535) {
                    invalid = false;
                }
            } catch (NumberFormatException ex) {
                // invalid remains true
            }
        }
        if (invalid) {
            Error error = new Error("linkconfig.paymentOption.LIGHTNING_BTC.rpcport.invalid", Arg.of("value", rpcport));
            result = Optional.of(error);
        }

        return result;
    }

    private Optional<Error> checkCert(String tlsCert) {
        // TODO validate
        return Optional.empty();
    }

    private Optional<Error> checkMacaroon(String invoiceMacaroon) {
        // TODO validate
        return Optional.empty();
    }
}
