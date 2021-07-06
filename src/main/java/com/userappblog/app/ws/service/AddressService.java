package com.userappblog.app.ws.service;

import java.util.List;
import com.userappblog.app.ws.shared.dto.AddressDTO;

public interface AddressService {

	List<AddressDTO> getAddresses(String userId);
	AddressDTO getAddress(String addressId);

}
