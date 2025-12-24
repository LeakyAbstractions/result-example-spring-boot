package com.example.client;

import com.example.api.ApiResponse;
import com.example.api.Pet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Loopback HTTP client.
 */
@FeignClient(value = "loopback", path = "/pet", primary = false)
public interface LoopbackClient {

	@GetMapping
	ApiResponse<Collection<Pet>> remoteList();

	@GetMapping("/{id}")
	ApiResponse<Pet> remoteFindById(@PathVariable Long id);

	@DeleteMapping("/{id}")
	ApiResponse<Boolean> remoteDeleteById(@PathVariable Long id);

	@PostMapping
	ApiResponse<Pet> remoteCreate(@RequestBody Pet pet);

	@PutMapping
	ApiResponse<Pet> remoteUpdate(@RequestBody Pet pet);
}
