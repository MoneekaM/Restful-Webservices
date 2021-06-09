package com.appsdeveloperblog.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.service.UserService;
import com.appsdeveloperblog.app.ws.shared.Utils;
import com.appsdeveloperblog.app.ws.shared.dto.UserDTO;
import com.appsdeveloperblog.app.ws.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDTO createUser(UserDTO user) {

		// Checking whether the user is already existing in database by its unique
		// attribute email(in our user entity)
		if (userRepository.findByEmail(user.getEmail()) != null)
			throw new RuntimeException("Record already exists!");

		UserEntity userEntity = new UserEntity();
		BeanUtils.copyProperties(user, userEntity);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		UserEntity storedUserDetails = userRepository.save(userEntity);
		UserDTO returnValue = new UserDTO();
		BeanUtils.copyProperties(storedUserDetails, returnValue);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email); // this user name not found exception is coming with spring
														// frame work along with user details core package where we have
														// UserDeatilsService Interface

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDTO getUser(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);
		UserDTO returnValue = new UserDTO();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDTO getUserByUserId(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		UserDTO returnValue = new UserDTO();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDTO updateUser(String userId, UserDTO user) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		// here Our custom exception"UserServiceException" is invoked instead of spring
		// framework exception "UserNameNotFoundException" . we can use either
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());
		UserEntity updatedUserEntity = userRepository.save(userEntity);
		UserDTO returnValue = new UserDTO();
		BeanUtils.copyProperties(updatedUserEntity, returnValue);
		return returnValue;
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		userRepository.delete(userEntity);
	}

	//Fetching all the users with limit and page
	@Override
	public List<UserDTO> getUsers(int page, int limit) {
		List<UserDTO> returnValue = new ArrayList<>();
		
		if(page>0) page = page-1;
		// for pagination support for application, instead of list of user entities, we
		// create a page user entity and the convert it to list of user entity.
		Pageable pageableRequest = PageRequest.of(page, limit);
		Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = usersPage.getContent();
		for (UserEntity userEntity : users) {
			UserDTO userDto = new UserDTO();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}

		return returnValue;
	}

}
