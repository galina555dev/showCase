package pro.paramoshina.show.showcase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class TaskControllerInMemoryTestIT {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    InMemoryTaskRepository inMemoryTaskRepository;

    @AfterEach
    void tearDown() {
        this.inMemoryTaskRepository.getTasks().clear();
    }


    @Test
    void getAllTasksTest() throws Exception {
        var requestBuilder = get("/api/tasks");
        this.inMemoryTaskRepository.getAllTasks()
                .addAll(List.of(
                        new Task(UUID.fromString("ce8a9ebd-25fa-4435-9111-a0354ee2adc0"),"первая задача",true),
                        new Task(UUID.fromString("40da8ef0-1048-4d84-8fc4-3f86b188e455"),"вторая задача",false)
                ));
        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
        this.mockMvc.perform(requestBuilder)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        this.mockMvc.perform(requestBuilder)
                .andExpect( content().json("""
                        [
                            {
                                "id": "ce8a9ebd-25fa-4435-9111-a0354ee2adc0",
                                "tile": "первая задача",
                                "completed": true
                              },
                            {
                                "id": "40da8ef0-1048-4d84-8fc4-3f86b188e455",
                                "tile": "вторая задача",
                                "completed": false
                                }
                        ]"""

                        )
                );

    }

    @Test
    void createTaskTest() throws Exception {
        //given
            var requestBuilder = post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "title": "интеграционное тестирование"
                        }
                        """
                    );
        //when
        this.mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                        {
                                          "tile": "интеграционное тестирование",
                                          "completed": false
                                                }
                                        """
                        ),
                           jsonPath("$.id").exists()
                        );

        assertEquals(1,this.inMemoryTaskRepository.getTasks().size());
        Task task = this.inMemoryTaskRepository.getTasks().get(0);
        assertNotNull(task.id());
        assertEquals("интеграционное тестирование",task.tile());
        assertFalse(task.completed());

    }

    @Test
    void createTaskTestInvalid() throws Exception {
        //given
        var requestBuilder = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
                .content("""
                        {
                          "title": null
                        }
                        """
                );
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                        {
                                          "errors": ["Task title must be set"]
                                                }
                                        """,true)
                );

        assertTrue(this.inMemoryTaskRepository.getTasks().isEmpty());

    }



}