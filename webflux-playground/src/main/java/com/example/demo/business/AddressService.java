package com.example.demo.business;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.demo.model.Address;

@Service
public class AddressService {

	private static ConcurrentHashMap<Long, Address> addresses;

	static {
		addresses = new ConcurrentHashMap<Long, Address>();
		addresses.put(0L, Address.builder().addressId(0L).description("Bogenhausen").build());
		addresses.put(1L, Address.builder().addressId(1L).description("Shwabing").build());
		addresses.put(2L, Address.builder().addressId(2L).description("Germering").build());
	}

	public Address getAddressById(final Long addressId) {
		return addresses.get(addressId);
	}
	
	public Stream<Address> getAllAddresses() {
		return addresses.values().stream();
	}

}
