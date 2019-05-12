package ws.payper.gateway.dummy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DummyWalletController {

    @Autowired
    private DummyCoinPaymentNetwork network;

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
}
