package com.example.demo.resources;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.business.AddressService;
import com.example.demo.business.GymService;
import com.example.demo.business.UserService;
import com.example.demo.model.Address;
import com.example.demo.model.CompositeGym;
import com.example.demo.model.CompositeUser;
import com.example.demo.model.Gym;
import com.example.demo.model.User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final GymService gymService;
	private final AddressService addressService;

	@GetMapping("/gyms/{gymId}/dashboard")
	public Flux<CompositeGym> getGymDashboard(@PathVariable(name = "gymId") final Long gymId) {

		Gym gymById = gymService.getGymById(gymId);

		CompositeGym compositeGym = new CompositeGym();
		compositeGym.setGym(gymById);

		Flux<User> userFlux = userService.getUsersByGymId(gymId).map(tuple -> {
			CompositeUser compositeUser = new CompositeUser();
			compositeUser.setUser(tuple);
			Address addressById = addressService.getAddressById(tuple.getAddressId());
			compositeUser.setAddress(addressById);
			if (compositeGym.getUsers() == null) {
				compositeGym.setUsers(new ArrayList<CompositeUser>());
			}
			compositeGym.getUsers().add(compositeUser);
			return tuple;
		}).subscribeOn(Schedulers.boundedElastic()).doOnEach(ex -> System.out.println(ex))
				.doOnError(ex -> System.out.println(ex)).onErrorReturn(null);
		userFlux.subscribe();

		return Flux.just(compositeGym);
	}

	@GetMapping(value = "/gyms", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Object> getAllGymsFromDifferentSources() {

		Mono<Gym> fast = gymService.getAllGymsFromFastSource().subscribeOn(Schedulers.boundedElastic())
				.doOnEach(ex -> System.out.println(ex));
		Mono<Gym> slower = gymService.getAllGymsFromSlowerSource().subscribeOn(Schedulers.boundedElastic())
				.doOnEach(ex -> System.out.println(ex));
		Mono<Gym> slowest = gymService.getAllGymsFromSlowestSource().subscribeOn(Schedulers.boundedElastic())
				.doOnEach(ex -> System.out.println(ex));

		// applying a predicate to check all
		Mono<Boolean> all = Flux.merge(fast, slower, slowest).all(g -> Objects.nonNull(g.getDescription()));
		// applying a predicate to check any
		Mono<Boolean> any = Flux.merge(fast, slower, slowest).any(g -> Objects.isNull(g.getDescription()));

		// converts to new object
		Flux<CompositeGym> map = Flux.merge(fast, slower, slowest).map(g -> {
			return CompositeGym.builder().gym(g).build();
		});

		// converts to list
		Mono<Map<Long, String>> collectMap = Flux.merge(fast, slower, slowest)
				.filter(g -> Objects.nonNull(g.getDescription())).collectMap(Gym::getGymId, Gym::getDescription);

		Flux<Gym> merge = Flux.merge(fast, slower, slowest).filter(g -> Objects.nonNull(g.getDescription()));

		return merge.map(sequence -> ServerSentEvent.<String>builder().id(String.valueOf(sequence.getGymId()))
				.event("gym-event").data(new JSONObject(sequence).toString()).build());

	}

	@GetMapping(value = "/sse", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<String>> streamEvents() {
		return Flux.interval(Duration.ofSeconds(1)).filter(seq -> seq % 2 == 0)
				.map(sequence -> ServerSentEvent.<String>builder().id(String.valueOf(sequence)).event("periodic-event")
						.data("SSE - " + LocalTime.now().toString()).build());
	}

	
	@GetMapping(value = "/users")
	public void users() {
		StopWatch watch = new StopWatch();
		watch.start();
		Mono<List<User>> userList = Flux.merge(userService.getUserFromCache(0L), userService.getUserFromAway(1L),
				userService.getUserFromFarAway(2L)).collectList();

		userList.block();
		watch.stop();
		System.out.println("time passed as second:" + watch.getTotalTimeSeconds());

	}

}
