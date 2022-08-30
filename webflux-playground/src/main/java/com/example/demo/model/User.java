package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder 
@AllArgsConstructor
@NoArgsConstructor
public class User {

	private Long userId;
	private Long addressId;
	private Long gymId;
	private String name;
	private int age;
}
