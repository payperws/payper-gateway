package ws.payper.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class PaymentRequiredController {

    @RequestMapping("/payment-required")
    @ResponseStatus(code = HttpStatus.PAYMENT_REQUIRED)
    public String paymentRequired(@RequestParam("amount") String amount, @RequestParam("account") String account, Model model) {
        model.addAttribute("amount", amount);
        model.addAttribute("account", account);
        return "payment-required";
    }

}
