package com.example.demo.business;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.demo.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService {

	private static ConcurrentHashMap<Long, User> users;

	static {
		users = new ConcurrentHashMap<Long, User>();
		users.put(0L, User.builder().age(15).name("random1").gymId(0L).addressId(0L).userId(0L).build());
		users.put(1L, User.builder().age(15).name("random2").gymId(0L).addressId(1L).userId(1L).build());
		users.put(1L, User.builder().age(15).name("random3").gymId(1L).addressId(2L).userId(2L).build());
	}

	public User getUserById(final Long userId) {
		return users.get(userId);
	}

	public Flux<User> getUsersByGymId(final Long gymId) {
		return Flux.fromStream(users.values().stream().filter(u -> u.getGymId().compareTo(gymId) == 0));
	}

	public Stream<User> getAllUsers() {
		return users.values().stream();
	}

	public Mono<User> getUserFromCache(final Long userId) {
		return Mono.fromCallable(() -> {
			System.out.println("service called with" + userId);

			try {
				TimeUnit.SECONDS.sleep(1L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return users.get(userId);
		}).subscribeOn(Schedulers.boundedElastic());

	}

	public Mono<User> getUserFromAway(final Long userId) {
		return Mono.fromCallable(() -> {
			System.out.println("service called with" + userId);

			try {
				TimeUnit.SECONDS.sleep(3L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return users.get(userId);
		}).subscribeOn(Schedulers.boundedElastic());

	}

	public Mono<User> getUserFromFarAway(final Long userId) {
		return Mono.fromCallable(() -> {
			System.out.println("service called with" + userId);

			try {
				TimeUnit.SECONDS.sleep(5L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return users.get(userId);
		}).subscribeOn(Schedulers.boundedElastic());
	}

}
