package com.ken.usermanager.Controllers;

import com.ken.usermanager.DTO.UserDTO;
import com.ken.usermanager.Domains.User;
import com.ken.usermanager.Repositories.UserRepository;
import com.ken.usermanager.Requests.UserRequest;
import com.ken.usermanager.Responses.UserNotFoundResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/v1/users")
@RestController
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }


    @Parameter(in = ParameterIn.HEADER, name = "X-Authorization", required = true, schema = @Schema(type = "string"))
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(@PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable page){
        List<UserDTO> users = userRepository.findAll(page).stream().map(UserDTO::new).toList();
        return ResponseEntity.ok(new PageImpl<UserDTO>(users, page, page.getPageSize()));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundResponse.class))
            })
    })
    @Parameter(in = ParameterIn.HEADER, name = "X-Authorization", required = true, schema = @Schema(type = "string"))
    @GetMapping("/{id}")
    public ResponseEntity<?> getOneUser(@PathVariable Long id){
        if(!userRepository.existsById(id)){
            return ResponseEntity.status(404).body(new UserNotFoundResponse("User not found."));
        }
        return ResponseEntity.ok(new UserDTO(userRepository.getReferenceById(id)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
            }),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundResponse.class))
            })
    })
    @Parameter(in = ParameterIn.HEADER, name = "X-Authorization", required = true, schema = @Schema(type = "string"))
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, UserRequest userDTO){
        if(!userRepository.existsById(id)){
            return ResponseEntity.status(404).body(new UserNotFoundResponse("User not found."));
        }
        User user = userRepository.getReferenceById(id);
        user.setPhone(userDTO.phone());
        user.setEmail(userDTO.email());

        return ResponseEntity.ok(new UserDTO(userRepository.save(user)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundResponse.class))
            })
    })
    @Parameter(in = ParameterIn.HEADER, name = "X-Authorization", required = true, schema = @Schema(type = "string"))
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        if(!userRepository.existsById(id)){
            return ResponseEntity.status(404).body(new UserNotFoundResponse("User not found."));
        }
        userRepository.delete(userRepository.getReferenceById(id));
        return ResponseEntity.noContent().build();
    }

}
