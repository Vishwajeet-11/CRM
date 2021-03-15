package com.agiklo.oracledatabase.service;

import com.agiklo.oracledatabase.entity.Employee;
import com.agiklo.oracledatabase.entity.Post;
import com.agiklo.oracledatabase.entity.dto.PostDTO;
import com.agiklo.oracledatabase.mapper.PostMapper;
import com.agiklo.oracledatabase.repository.EmployeeRepository;
import com.agiklo.oracledatabase.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final EmployeeRepository employeeRepository;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public List<PostDTO> getAllPosts(){
        return postRepository.findAll()
                .stream()
                .map(postMapper::mapPostToDTO)
                .collect(Collectors.toList());
    }

    public Post addNewPost(Post post, Principal principal) {
        Employee employee = employeeRepository.findByEmail(principal.getName()).orElseThrow(() ->
                new IllegalStateException("Employee not found"));
        LocalDateTime actualTime = LocalDateTime.now();
        post.setAuthor(employee);
        post.setCreatedAt(actualTime);
        return postRepository.save(post);
    }

    public void deletePostById(Long id, Principal principal) {
        Post post = postRepository.findById(id).orElseThrow(()->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Post cannot be found, the specified id does not exist"));
        if (post.getAuthor().getEmail().equals(principal.getName())){
            postRepository.deleteById(id);
        }
        else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the author of this post");
        }
    }

    @Transactional(readOnly = true)
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));
        return postMapper.mapPostToDTO(post);
    }

    @Transactional(readOnly = true)
    public Set<PostDTO> findPostsByAuthorFirstnameAndLastname(String firstName, String lastName) {
            return postRepository.findPostByAuthorFirstNameAndAuthorLastName(firstName, lastName)
                    .stream()
                    .map(postMapper::mapPostToDTO)
                    .collect(Collectors.toSet());
    }
}
