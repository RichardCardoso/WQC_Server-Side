package com.richard.weger.wqc.faccade;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorFaccade implements ErrorController {
 
    @RequestMapping("/error")
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest) {
         
        ModelAndView errorPage = new ModelAndView("errorPage");
        String errorMsg = "", errorCode = "";
        int httpErrorCode = getErrorCode(httpRequest);
 
        switch (httpErrorCode) {
            case 400: {
                errorCode = "Http Error Code: 400. Bad Request";
                errorMsg = "There is something wrong with your request =(";
                break;
            }
            case 401: {
                errorCode = "Http Error Code: 401. Unauthorized";
                errorMsg = "Whoops. What are you trying to access? Whatever it is, i can't let you do that.";
                break;
            }
            case 404: {
                errorCode = "Http Error Code: 404. Resource not found";
                errorMsg = "Hm. I was not able to find the resource you are looking for =(";
                break;
            }
            case 500: {
                errorCode = "Http Error Code: 500. Internal Server Error";
                errorMsg = "Oh no! Something went bad. I think that I am doing some kind of mistake. X.X";
                break;
            }
        }
        errorPage.addObject("errorCode", errorCode);
        errorPage.addObject("errorMsg", errorMsg);
        return errorPage;
    }
     
    private int getErrorCode(HttpServletRequest httpRequest) {
        return (Integer) httpRequest
          .getAttribute("javax.servlet.error.status_code");
    }

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
