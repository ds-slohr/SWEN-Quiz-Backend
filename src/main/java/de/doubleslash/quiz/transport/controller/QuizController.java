package de.doubleslash.quiz.transport.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.doubleslash.quiz.repository.QuizRepository;
import de.doubleslash.quiz.repository.dao.quiz.Answer;
import de.doubleslash.quiz.repository.dao.quiz.Question;
import de.doubleslash.quiz.transport.dto.Quiz;
import de.doubleslash.quiz.transport.dto.SessionId;
import de.doubleslash.quiz.repository.UserRepository;
import de.doubleslash.quiz.transport.security.SecurityContextService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

  private final QuizHandler quizHandler;

  private final UserRepository repo;

  private final SecurityContextService securityContext;

  @GetMapping
  public List<Quiz> getAllQuiz() {
    var username = securityContext.getLoggedInUser();

    return repo.findByName(username)
        .map(de.doubleslash.quiz.repository.dao.auth.User::getQuizzes)
        .map(list -> list.stream()
            .map(q -> Quiz.builder()
                .name(q.getName())
                .id(q.getId())
                .build())
            .collect(Collectors.toList()))
        .orElse(Lists.newArrayList());
  }

  @PostMapping("/{quizId}")
  public ResponseEntity<SessionId> createNewQuiz(@PathVariable(value = "quizId") Long quizId) {

    var username = securityContext.getLoggedInUser();
    var quiz = repo.findByName(username)
        .flatMap(user -> user.getQuizzes().stream()
            .filter(q -> q.getId().equals(quizId))
            .findFirst());

    if (quiz.isPresent()) {
      var sessionId = quizHandler.newQuiz(quiz.get());
      if (StringUtils.hasText(sessionId)) {
        return new ResponseEntity<>(new SessionId(sessionId), HttpStatus.CREATED);
      }
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
}
