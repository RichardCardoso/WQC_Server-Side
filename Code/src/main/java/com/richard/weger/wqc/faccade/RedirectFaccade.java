package com.richard.weger.wqc.faccade;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RedirectFaccade {
	
	@RequestMapping("/")
	public ModelAndView redirect() {
		return new ModelAndView("redirect:/web/");
	}
}
