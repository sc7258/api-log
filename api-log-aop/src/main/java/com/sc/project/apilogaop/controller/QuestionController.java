package com.sc.project.apilogaop.controller;

import com.sc.project.apilogaop.exception.ResourceNotFoundException;
import com.sc.project.apilogaop.model.Question;
import com.sc.project.apilogaop.repository.QuestionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class QuestionController {

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping("/questions")
    public ResponseEntity<Page<Question>> getQuestions(Pageable pageable) {
        return ResponseEntity.ok(questionRepository.findAll(pageable));
    }

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<Question> getQuestions(@PathVariable Long questionId) {
        return ResponseEntity.ok(questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId))
        );
    }

    @PostMapping("/questions")
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody Question question) {
        return ResponseEntity.ok(questionRepository.save(question));
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long questionId,
                                   @Valid @RequestBody Question questionRequest) {
        return ResponseEntity.ok(
                questionRepository.findById(questionId)
                .map(question -> {
                    question.setTitle(questionRequest.getTitle());
                    question.setDescription(questionRequest.getDescription());
                    return questionRepository.save(question);
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId))
        );
    }


    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        return questionRepository.findById(questionId)
                .map(question -> {
                    questionRepository.delete(question);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
    }
}
