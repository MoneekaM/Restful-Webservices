package com.userappblog.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
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

import com.userappblog.app.ws.exceptions.UserServiceException;
import com.userappblog.app.ws.io.entity.UserEntity;
import com.userappblog.app.ws.io.repositories.UserRepository;
import com.userappblog.app.ws.service.UserService;
import com.userappblog.app.ws.shared.Utils;
import com.userappblog.app.ws.shared.dto.AddressDTO;
import com.userappblog.app.ws.shared.dto.UserDTO;
import com.userappblog.app.ws.ui.model.response.ErrorMessages;

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

		 for(int i=0; i<user.getAddresses().size();i++) {
			 AddressDTO address = user.getAddresses().get(i);
			 address.setUserDetails(user);
			 address.setAddressId(utils.generateAddressId(30));
			 user.getAddresses().set(i, address);
		 }
		//BeanUtils.copyProperties(user, userEntity);
		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity = modelMapper.map(user, UserEntity.class);
		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
		userEntity.setEmailVerificationStatus(false);
		UserEntity storedUserDetails = userRepository.save(userEntity);
		//BeanUtils.copyProperties(storedUserDetails, returnValue);
		UserDTO returnValue  = modelMapper.map(storedUserDetails, UserDTO.class);
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email); // this user name not found exception is coming with spring
														// frame work along with user details core package where we have
														// UserDeatilsService Interface

		//return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>()); -- added email verification status in the login check inorder the email
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), userEntity.getEmailVerificationStatus(), true, true, true, new ArrayList<>());
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

	@Override
	public boolean verifyEmailToken(String token) {
		boolean returnValue = false;
		
		UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
		if(userEntity != null) {
			boolean hasTokenExpired = Utils.hasTokenExpired(token);
			if(!hasTokenExpired) {
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				returnValue = true;
			}
		}
		return returnValue;
	}

}
