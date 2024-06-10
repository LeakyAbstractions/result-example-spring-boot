package com.example.client;

import com.example.api.Pet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Swagger HTTP client.
 */
@FeignClient(value = "swagger", path = "/v2/pet")
public interface SwaggerClient {

	@GetMapping("/findByStatus?status=available,pending,sold")
	List<Pet> remoteList();

	@GetMapping("/{id}")
	Pet remoteFindById(@PathVariable Long id);

	@DeleteMapping("/{id}")
	ResponseEntity<Object> remoteDeleteById(@PathVariable Long id);

	@PostMapping
	Pet remoteCreate(@RequestBody Pet pet);

	@PutMapping
	Pet remoteUpdate(@RequestBody Pet pet);
}
