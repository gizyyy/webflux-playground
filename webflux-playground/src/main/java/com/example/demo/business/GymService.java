package com.example.demo.business;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.demo.model.Gym;

import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

@Service
public class GymService {

	private static ConcurrentHashMap<Long, Gym> gyms;

	static {
		gyms = new ConcurrentHashMap<Long, Gym>();
		gyms.put(0L, Gym.builder().description("MC FIT Bogenhausen").gymId(0L).build());
		gyms.put(1L, Gym.builder().description("MC FIT Schwabing").gymId(1L).build());
	}

	public Gym getGymById(final Long gymId) {
		return gyms.get(gymId);
	}

	public Stream<Gym> getAllGyms() {
		return gyms.values().stream();
	}

	@SneakyThrows
	public Mono<Gym> getAllGymsFromFastSource() {
		Mono<Gym> gym = Mono.fromCallable(() -> {
			TimeUnit.SECONDS.sleep(1L);
			return Gym.builder().description("MC FIT Bogenhausen").gymId(1l).build();
		});

		return gym;

	}

	@SneakyThrows
	public Mono<Gym> getAllGymsFromSlowerSource() {
		Mono<Gym> gym = Mono.fromCallable(() -> {
			TimeUnit.SECONDS.sleep(3L);
			return Gym.builder().description("MC FIT Unterhaching").gymId(2l).build();
		});
		return gym;
	}

	@SneakyThrows
	public Mono<Gym> getAllGymsFromSlowestSource() {
		Mono<Gym> gym = Mono.fromCallable(() -> {
			TimeUnit.SECONDS.sleep(5L);
			return Gym.builder().description("MC FIT Haar").gymId(3l).build();
		});
		return gym;
	}

}
