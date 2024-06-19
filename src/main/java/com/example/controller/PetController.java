package com.example.controller;

import com.example.api.ApiError;
import com.example.api.ApiResponse;
import com.example.api.Pet;
import com.example.api.RepositoryType;
import com.example.config.PetStoreConfig;
import com.example.repository.PetRepository;
import com.leakyabstractions.result.api.Result;
import com.leakyabstractions.result.core.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Interacts with different pet repositories.
 */
@RestController
@Tag(name = "pet")
public class PetController {

	static final Logger log = getLogger(PetController.class);

	final PetStoreConfig config;
	final Collection<PetRepository> repositories;

	PetController(PetStoreConfig config, Collection<PetRepository> repositories) {
		this.config = config;
		this.repositories = repositories;
	}

	@GetMapping("/pet")
	@Operation(summary = "List all pets", description = "Returns all pets in a store")
	ResponseEntity<ApiResponse<Collection<Pet>>> list(@RequestHeader("X-Type") RepositoryType type) {
		log.info("List all pets in {} pet store", type);
		final Result<Collection<Pet>, ApiError> result = locate(type)
			.flatMapSuccess(PetRepository::listPets)
			.ifSuccess(x -> log.info("Listed {} pet(s) in {}", x.size(), type))
			.ifFailure(this::logError);
		return ok(result);
	}

	@PostMapping("/pet")
	@Operation(summary = "Add new pet", description = "Adds a new pet to a store")
	ResponseEntity<ApiResponse<Pet>> create(
		@RequestHeader("X-Type") RepositoryType type,
		@RequestBody @Valid Pet pet) {
		log.info("Create new pet: {} in {} pet store", pet, type);
		final Result<Pet, ApiError> result = locate(type)
			.flatMapSuccess(x -> x.createPet(pet))
			.ifSuccess(x -> log.info("Created pet #{} in {}", x.getId(), type))
			.ifFailure(this::logError);
		return created(result);
	}

	@PutMapping("/pet")
	@Operation(summary = "Update existing pet", description = "Updates an existing pet in a store")
	ResponseEntity<ApiResponse<Pet>> update(
		@RequestHeader("X-Type") RepositoryType type,
		@RequestBody @Valid Pet pet) {
		log.info("Update pet: {} in {} pet store", pet, type);
		final Result<Pet, ApiError> result = locate(type)
			.flatMapSuccess(x -> x.updatePet(pet))
			.ifSuccess(x -> log.info("Updated #{} in {}: {}", x.getId(), type, x))
			.ifFailure(this::logError);
		return ok(result);
	}

	@GetMapping("/pet/{id}")
	@Operation(summary = "Find pet", description = "Returns a single pet by ID")
	ResponseEntity<ApiResponse<Pet>> find(
		@RequestHeader("X-Type") RepositoryType type,
		@PathVariable @Parameter(description = "Pet ID to find") Long id) {
		log.info("Find pet by ID: {} in {} pet store", id, type);
		final Result<Pet, ApiError> result = locate(type)
			.flatMapSuccess(x -> x.findPet(id))
			.ifSuccess(x -> log.info("Found #{} in {}: {}", id, type, x))
			.ifFailure(this::logError);
		return ok(result);
	}

	@DeleteMapping("/pet/{id}")
	@Operation(summary = "Delete pet", description = "Deletes a pet by ID")
	ResponseEntity<ApiResponse<Boolean>> delete(
		@RequestHeader("X-Type") RepositoryType type,
		@PathVariable @Parameter(description = "Pet ID to delete") Long id) {
		log.info("Delete pet by ID: {} from {} pet store", id, type);
		final Result<Boolean, ApiError> result = locate(type)
			.flatMapSuccess(x -> x.deletePet(id))
			.ifSuccessOrElse(x -> log.info("Deleted #{} from {}", id, type), this::logError);
		return ok(result);
	}

	Result<PetRepository, ApiError> locate(RepositoryType type) {
		final Optional<PetRepository> repository = repositories.stream()
			.filter(x -> x.getType() == type)
			.findAny();
		return Results.ofOptional(repository, () -> ApiError.unavailable(type)).ifSuccessOrElse(
			x -> log.info("{} pet store located", type),
			x -> log.warn("Could not locate {} pet store", type));
	}

	private void logError(ApiError error) {
		log.error(error.getMessage());
	}

	private <S> ResponseEntity<ApiResponse<S>> ok(Result<S, ApiError> result) {
		return new ResponseEntity<>(response(result), HttpStatus.OK);
	}

	private <S> ResponseEntity<ApiResponse<S>> created(Result<S, ApiError> result) {
		return new ResponseEntity<>(response(result), HttpStatus.CREATED);
	}

	private <S> ApiResponse<S> response(Result<S, ApiError> result) {
		return new ApiResponse<>(config.getApiVersion(), result);
	}
}
