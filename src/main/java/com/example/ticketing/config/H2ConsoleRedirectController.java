package com.example.ticketing.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * H2's servlet is registered as {@code /h2-console/*}; a bare {@code /h2-console} request
 * is handled by DispatcherServlet and yields {@link org.springframework.web.servlet.resource.NoResourceFoundException}.
 * Redirect to the trailing slash so the H2 console servlet serves the UI.
 */
@Controller
public class H2ConsoleRedirectController {

    @GetMapping("/h2-console")
    public RedirectView redirectToSlash() {
        return new RedirectView("/h2-console/");
    }
}
