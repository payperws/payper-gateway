package ws.payper.gateway.dummy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class DummyWalletController {

    @Autowired
    private DummyCoinPaymentNetwork network;

    @RequestMapping(value = "/dummy-wallet", method = RequestMethod.GET)
    public String paymentPage(@RequestParam String amount, @RequestParam String invoice, @RequestParam String note, Model model) {
        model.addAllAttributes(Map.of(
                "amount", amount,
                "invoice", invoice,
                "note", note
        ));
        return "dummy-wallet-payment-page";
    }

    @RequestMapping(value = "/dummy-wallet/authorize", method = RequestMethod.POST)
    @ResponseBody
    public Object authorizePayment(@RequestBody DummyWalletAuthorization authorization) {
        network.payInvoice(authorization.getInvoice());

        return new Object() {
            public boolean getAuthorized() {
                return true;
            }
        };
    }

    @RequestMapping(value = "/dummy-wallet/success", method = RequestMethod.GET)
    public String success() {
        return "dummy-wallet-success-page";
    }
}
