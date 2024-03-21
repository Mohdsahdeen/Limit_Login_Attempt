package com.spring.security.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.spring.security.entity.User;
import com.spring.security.repository.UserRepo;
import com.spring.security.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
    
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepo userRepo;
	
	@ModelAttribute
	public void commonUser(Principal p, Model m) {
		
		if(p != null) {
			String email = p.getName();
		    User user = userRepo.findByEmail(email);
		    m.addAttribute("user",user);
		}
		
	}
	
	
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	@GetMapping("/register")
	public String register() {
		return "register";
	}
	
	@GetMapping("/signin")
	public String login() {
		return "login";
	}
	
//	@GetMapping("/user/profile")
//	public String profile(Principal p, Model m) {
//		
//		String email = p.getName();
//	    User user = userRepo.findByEmail(email);
//	    m.addAttribute("user",user);
//		
//		return "profile";
//	}
//	
//	@GetMapping("/user/home")
//	public String home() {
//		return "home";
//	}
	
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute User user, HttpSession session, HttpServletRequest request) {
	
		String url = request.getRequestURI().toString();
		
		url = url.replace(request.getServletPath(), "");
		User u   = 	userService.saveUser(user,url);
	if(u != null) {
		session.setAttribute("msg", "Register successfully");
	}else {
		session.setAttribute("msg", "Something wrong in server ");
	}
		return "redirect:/register";
	}
	
	@GetMapping("/verify")
	public String verifyAccount(@Param("code") String code, Model m) {
	     boolean f = 	userService.verifyAccount(code);
	     if(f) {
	    	 m.addAttribute("msg","Sucessfully your account is verified");
	     }else {
	    	 m.addAttribute("msg","may be your verification code is incorrect or already verified");
	     }
		
		return "message";
	}
	
}
