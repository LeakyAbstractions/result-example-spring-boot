package com.example.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

/**
 * Redirects to Swagger UI.
 */
@Hidden
@Controller
public class IndexController {

	@GetMapping("/")
	public void index(HttpServletResponse response) throws IOException {
		response.sendRedirect("/swagger-ui/index.html");
	}
}
