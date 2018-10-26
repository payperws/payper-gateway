package ws.payper.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class PaymentRequiredController {

    @RequestMapping("/payment-required")
    @ResponseStatus(code = HttpStatus.PAYMENT_REQUIRED)
    public String paymentRequired(Model model) {
        String amount = "20";
        String accountAddress = "1016";

        model.addAttribute("amount", amount);
        model.addAttribute("account", accountAddress);
        return "payment-required";
    }

}
