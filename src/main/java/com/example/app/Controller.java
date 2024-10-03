package com.example.app;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/test")
public class Controller {

    @RequestMapping(path = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Response> loggingTokenAccess(HttpServletRequest servletRequest) {
        System.out.println("DEBUG: CONTROLLER INVOKE");

        return new ResponseEntity<>(new Response(
                "It's work!!! Client cert: " + servletRequest.getHeader("X-Client-Cert")
        ), HttpStatus.OK);
    }

}
