package com.appsdeveloperblog.app.ws.exceptions;

import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.appsdeveloperblog.app.ws.ui.model.response.ErrorMessage;

@ControllerAdvice
public class AppExceptionsHandler {
	
	/* 
	 * Customized exception method for handling specific userServiceException 
	 * with specific exception message and date 
	 * (specific messages based on the requirement we can add/remove the fields to display in the message)
	 */
	@ExceptionHandler(value = {UserServiceException.class})
	public ResponseEntity<Object> handleUserServiceExeption(UserServiceException ex, WebRequest req){
		
		ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/* 
	 * Customized exception method for handling other exceptions (generalized messages will be shown)
	 */
	@ExceptionHandler(value = {Exception.class})
	public ResponseEntity<Object> handleOtherExeptions(Exception ex, WebRequest req){
		
		ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	
}
