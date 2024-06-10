package com.example;

import com.example.api.ApiResponse;
import com.example.api.Pet;
import com.example.client.LoopbackClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;

import java.util.Collection;

import static com.example.api.ApiErrorCode.PET_STORE_UNAVAILABLE;
import static com.example.api.RepositoryType.*;
import static com.leakyabstractions.result.assertj.InstanceOfResultAssertFactories.LIST;
import static com.leakyabstractions.result.assertj.InstanceOfResultAssertFactories.RESULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.RequestEntity.get;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ExampleApplicationTests {

	static final ParameterizedTypeReference<ApiResponse<Pet>> PET = new ParameterizedTypeReference<>() { };
	static final ParameterizedTypeReference<ApiResponse<Collection<Pet>>> PETS = new ParameterizedTypeReference<>() { };

	@Autowired
	TestRestTemplate rest;

	@Test
	void contextLoads() {
	}

	@Test
	void testIndexController() {
		assertThat(rest.getForEntity("/", String.class).getBody())
			.contains("swagger-ui.css");
	}

	@Test
	void testPetControllerLocal() {
		assertThat(rest.exchange(get("/pet").header("X-Type", LOCAL.name()).build(), PETS))
			.extracting(HttpEntity::getBody)
			.extracting(ApiResponse::getResult)
			.asInstanceOf(RESULT)
			.hasSuccessThat(LIST)
			.hasSize(3);
	}

	@Test
	void testPetControllerLoopback() {
		assertThat(rest.exchange(get("/pet/0").header("X-Type", LOOPBACK.name()).build(), PET))
			.extracting(HttpEntity::getBody)
			.extracting(ApiResponse::getResult)
			.asInstanceOf(RESULT)
			.hasSuccessSatisfying(pet -> assertThat(pet).hasFieldOrPropertyWithValue("name", "Rocky"));
	}

	@Test
	void testPetControllerSpecial() {
		assertThat(rest.exchange(get("/pet").header("X-Type", SPECIAL.name()).build(), PETS))
			.extracting(HttpEntity::getBody)
			.extracting(ApiResponse::getResult)
			.asInstanceOf(RESULT)
			.hasFailureSatisfying(error -> assertThat(error).hasFieldOrPropertyWithValue("code", PET_STORE_UNAVAILABLE));
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		LoopbackClient loopbackClient(TestRestTemplate rest) {
			return new LoopbackClient() {
				@Override
				public ApiResponse<Pet> remoteFindById(Long id) {
					return rest
						.exchange(get("/pet/" + id).header("X-Type", LOCAL.name()).build(), PET)
						.getBody();
				}

				@Override
				public ApiResponse<Collection<Pet>> remoteList() {
					throw new RuntimeException("Not implemented");
				}

				@Override
				public ApiResponse<Boolean> remoteDeleteById(Long id) {
					throw new RuntimeException("Not implemented");
				}

				@Override
				public ApiResponse<Pet> remoteCreate(Pet pet) {
					throw new RuntimeException("Not implemented");
				}

				@Override
				public ApiResponse<Pet> remoteUpdate(Pet pet) {
					throw new RuntimeException("Not implemented");
				}
			};
		}
	}
}
