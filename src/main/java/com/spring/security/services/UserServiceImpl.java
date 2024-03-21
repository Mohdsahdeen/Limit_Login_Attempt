package com.spring.security.services;

import java.util.Date;
import java.util.UUID;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.spring.security.entity.User;
import com.spring.security.repository.UserRepo;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;




@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	@Autowired
	private JavaMailSender mailSender;
	
	
	
	
	
	@Override
	public User saveUser(User user, String url) {
		
	String password = 	passwordEncoder.encode(user.getPassword());
	user.setPassword(password);	
	user.setRole("ROLE_ADMIN");
	
	user.setEnable(false);
	user.setVerificationCode(UUID.randomUUID().toString());
	
	user.setAccountNonLocked(true);
	user.setFailedAttempt(0);
	user.setLockTime(null);
	
	User newUser  = userRepo.save(user);
	
	if(newUser != null) {
		sendEmail(newUser, url);
	}
	
		return newUser;
	}
	
	

	@Override
	public void removeSessionMessage() {
       HttpSession session	 = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest().getSession();
		
       session.removeAttribute("msg");
	}



	@Override
	public void sendEmail(User user, String url) {
		
	String from = "sahdeenmohd2415675@gmail.com";
	String to = user.getEmail();
	String subject = "Account Verification";
	String content = "Dear [[name]],<br>" + "Please click the link below to verify your rgistration:<br> "
	                  +"<h3><a href=\"[[URL]]\" target=\"_self\"> VERIFY </a></h3>" + "Thank you,<br>"+"Sahdeen";
	
	try {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		
		
		
		
		helper.setFrom(from,"Sahdeen");
		helper.setTo(to);
		helper.setSubject(subject);
		
		content = content.replace("[[name]]", user.getName());
		
		String siteUrl = url + "/verify?code=" + user.getVerificationCode();
		
		content = content.replace("[[URL]]", siteUrl);
		
		helper.setText(content,true);
		
		mailSender.send(message);
		
	} catch(Exception e) {
		e.printStackTrace();
	}
	
	}



	@Override
	public boolean verifyAccount(String verificationCode) {
		
		User user = userRepo.findByVerificationCode(verificationCode);
		
		if(user == null) {
			return false;
		}else {
			user.setEnable(true);
			user.setVerificationCode(null);
			
			userRepo.save(user);
			return true;
		}
		
		
	}



	@Override
	public void increaseFailedAttempt(User user) {
	  int attempt = 	user.getFailedAttempt()+1;
	  userRepo.updateFailedAttempt(attempt, user.getEmail());
	  
		
	}

  

	@Override
	public void resetAttempt(String email) {
		userRepo.updateFailedAttempt(0, email);
		
		
	}



	@Override
	public void lock(User user) {
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepo.save(user);
		
		
	}

	// for 1 hour lock
//private static final long lock_duration_time = 1*60*60*1000;
	
	
	// 0.5 second = 30000 millisecond
 private static final long lock_duration_time = 30000;	
 
 public static final long ATTEMPT_TIME = 3;

	@Override
	public boolean unlokAccountTimeExpired(User user) {
	
	long lockTimeInMills = 	user.getLockTime().getTime();	
	long currentTimeMillis = System.currentTimeMillis();
	
	if(lockTimeInMills+lock_duration_time< currentTimeMillis) {
		user.setAccountNonLocked(true);
		user.setLockTime(null);
		user.setFailedAttempt(0);
		
		userRepo.save(user);
		
		return true;
	}
	
		return false;
	}
	
	

}