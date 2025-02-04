package pro.paramoshina.show.showcase;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/tasks")
public class TaskControllerInMemory {

    private final TaskRepository taskRepository;

    private final MessageSource messageSource;

    public TaskControllerInMemory(@Qualifier("inMemoryTaskRepository") TaskRepository taskRepository, MessageSource messageSource) {
        this.taskRepository = taskRepository;
        this.messageSource = messageSource;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks() {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(this.taskRepository.getAllTasks());

    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody NewTaskPayload task
    , UriComponentsBuilder ucBuilder, Locale locale) {
        if (task.title() == null || task.title().isBlank()) {
            final var messError = this.messageSource
                    .getMessage("tasks.error.title_is_null",new Object[0], locale);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorsMessage(List.of(messError)));
        } else {
            var taskNew = new Task(task.title());
            this.taskRepository.save(taskNew);
            return ResponseEntity.created(ucBuilder
                            .path("/api/tasks/{taskId}")
                            .build(Map.of("taskId", taskNew.id())))
                    .contentType(MediaType.APPLICATION_JSON).body(taskNew);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<Task> getTask(@PathVariable("id") UUID id) {
        return ResponseEntity.of(this.taskRepository.findById(id));
    }


}
