package com.laurentiuspilca.liveproject.controllers;

import com.laurentiuspilca.liveproject.controllers.dto.HealthAdvice;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/advice")
public class HealthAdviceController {

  private static Logger logger = Logger.getLogger(HealthAdviceController.class.getName());

  @PostMapping
  public void provideHealthAdviceCallback(Authentication authentication, @RequestBody List<HealthAdvice> healthAdvice) {
    logger.info("Authentication: " + authentication);
    healthAdvice.forEach(h -> logger.info("Advice for: "+ h.getUsername()+
            " Advice text: "+h.getAdvice()));
  }
}
