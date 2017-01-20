package org.tobsch;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author  Tobias Schneider - schneider@synyx.de
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public String index() {

        return "Welcome to the bug tls demo page!";
    }
}
