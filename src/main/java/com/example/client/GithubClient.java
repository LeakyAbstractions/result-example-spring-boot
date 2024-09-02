package com.example.client;

import com.example.api.ApiResponse;
import com.example.api.Pet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Github HTTP client.
 */
@FeignClient(value = "github", path = "/result-example-spring-boot/src/test/resources/static")
public interface GithubClient {

	@GetMapping("/all.json")
	ApiResponse<Collection<Pet>> remoteList();

	@GetMapping("/{id}.json")
	ApiResponse<Pet> remoteFindById(@PathVariable Long id);

	@DeleteMapping("/{id}.json")
	ApiResponse<Boolean> remoteDeleteById(@PathVariable Long id);

	@PostMapping
	ApiResponse<Pet> remoteCreate(@RequestBody Pet pet);

	@PutMapping
	ApiResponse<Pet> remoteUpdate(@RequestBody Pet pet);
}
