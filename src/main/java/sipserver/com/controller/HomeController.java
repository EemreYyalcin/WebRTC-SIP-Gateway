package sipserver.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	// public ModelAndView getHomePage() {
	// return new ModelAndView("index");
	// }
	@RequestMapping("/")
	public String getValue() {
		return "index";
	}

}
