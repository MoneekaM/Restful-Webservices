package com.userappblog.app.ws.shared;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.userappblog.app.ws.shared.dto.UserDTO;

public class AmazonSES {
	
	final String FROM = "mail4moneeka@gmail.com";
	final String SUBJECT = "To complete your registration!";
	final String HTMLBODY = "<h1>Please verify your email address</h1>"+"<p>Thank you for registering!.To complete registration process and be able to login</p>"+
	"click on the following link: "+ "<a href='http://localhost:8080/verification-service/verification.html?token=$tokenValue'>"+
			"Final step to complete your registration"+"</a><br/><br/>"+"Thank you!";
	final String TEXTBODY="Please verify your email address."+"Thank you for registering!.To complete registration process and be able to login."+
			"open the following URL in your browser window:"+"http://localhost:8080/verification-service/verification.html?token=$tokenValue"+
			"Thank you!";
	
	public void verifyEmail(UserDTO userDTO) {
		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
		
		String htmlBodyWithToken = HTMLBODY.replace("$tokenValue", userDTO.getEmailVerificationToken());
		String textBodyWithToken = TEXTBODY.replace("$tokenValue", userDTO.getEmailVerificationToken());
		
		SendEmailRequest request = new SendEmailRequest()
				.withDestination(
						new Destination().withToAddresses(userDTO.getEmail()))
				.withMessage(new Message()
				.withBody(new Body()
				.withHtml(new Content()
				.withCharset("UTF-8").withData(htmlBodyWithToken))
				.withText(new Content()
				.withCharset("UTF-8").withData(textBodyWithToken)))
				.withSubject(new Content()
				.withCharset("UTF-8").withData(SUBJECT))).withSource(FROM);
		
		client.sendEmail(request);
		System.out.println("Email Sent!");
	}

}
