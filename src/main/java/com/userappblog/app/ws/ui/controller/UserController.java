package com.userappblog.app.ws.ui.controller;

import java.util.ArrayList;
import java.util.List;

import javax.management.OperationsException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.userappblog.app.ws.exceptions.UserServiceException;
import com.userappblog.app.ws.service.UserService;
import com.userappblog.app.ws.shared.dto.UserDTO;
import com.userappblog.app.ws.ui.model.request.UserDetailsRequestModel;
import com.userappblog.app.ws.ui.model.response.ErrorMessages;
import com.userappblog.app.ws.ui.model.response.OperationStatusModel;
import com.userappblog.app.ws.ui.model.response.RequestOperationName;
import com.userappblog.app.ws.ui.model.response.RequestOperationStatus;
import com.userappblog.app.ws.ui.model.response.UserRest;

@RestController
@RequestMapping("users") // http://localhost:8080/users
public class UserController {

	@Autowired
	UserService userService;

	@GetMapping(path = "/{userId}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest getUser(@PathVariable String userId) {

		UserRest returnValue = new UserRest();
		UserDTO fetchedUserDto = userService.getUserByUserId(userId);
		BeanUtils.copyProperties(fetchedUserDto, returnValue);
		return returnValue;
	}

	// consumes means that the request input type can be either XML/JSON for the
	// userDetailsRequestModel in the @RequestBody and
	// produces means the response from webservice can be XML/JSON - we need to add
	// dependency "jackson-dataformat-xml"(by default json is accepted but for
	// xml we need to add this dependency and mention Application/XML in the
	// mediatype )
	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {
		// customzied exception class "UserServiceException" is called. we call
		// generalized/other common exceptions based on the error
		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		// generalized exceptions can also called like below
		// if(userDetails.getFirstName().isEmpty()) throw new NullPointerException("The
		// object is null");

		/*
		 * UserDTO userDto = new UserDTO(); BeanUtils.copyProperties(userDetails,
		 * userDto);
		 */
		ModelMapper modelMapper = new ModelMapper();
		UserDTO userDto = modelMapper.map(userDetails, UserDTO.class);
		UserDTO createdUser = userService.createUser(userDto);
		UserRest returnValue = modelMapper.map(createdUser, UserRest.class);
		/* BeanUtils.copyProperties(createdUser, returnValue); */
		return returnValue;
	}

	@PutMapping(path = "/{userId}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest updateUser(@PathVariable String userId, @RequestBody UserDetailsRequestModel userDetails) {
		UserRest returnValue = new UserRest();

		UserDTO userDto = new UserDTO();
		BeanUtils.copyProperties(userDetails, userDto);
		UserDTO updatedUser = userService.updateUser(userId, userDto);
		BeanUtils.copyProperties(updatedUser, returnValue);

		return returnValue;
	}

	@DeleteMapping(path = "/{userId}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String userId) {

		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.DELETE.name());
		userService.deleteUser(userId);
		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		return returnValue;
	}

	@GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "2") int limit) {

		List<UserRest> returnValue = new ArrayList<>();
		List<UserDTO> userList = userService.getUsers(page,limit);
		for(UserDTO userDto : userList) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}
		return returnValue;
	}
}
