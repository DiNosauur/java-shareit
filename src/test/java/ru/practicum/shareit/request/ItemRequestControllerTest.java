package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService service;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "Нужна 4-местная байдарка");

    private ItemRequest itemRequest = new ItemRequest();

    private ItemRequestFullDto itemRequestFullDto = new ItemRequestFullDto();

    private Item item = new Item();

    @BeforeEach
    void setUp() {
        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setRequestor(1L);
        itemRequest.setCreated(LocalDateTime.of(2022, 10, 1, 1, 1, 1));
        itemRequest.setDescription(itemRequestDto.getDescription());

        item.setId(2L);
        item.setName("Дрель");
        item.setDescription("Инструмент для сверления");
        item.setOwner(3L);

        itemRequestFullDto.setId(itemRequest.getId());
        itemRequestFullDto.setDescription(itemRequest.getDescription());
        itemRequestFullDto.setCreated(itemRequest.getCreated());
        itemRequestFullDto.setItems(List.of(item));
    }

    @Test
    void createItemRequest() throws Exception {
        when(service.saveItemRequest(any(), anyLong()))
                .thenReturn(itemRequest);

        mvc.perform(post("/requests")
                .content(mapper.writeValueAsString(itemRequestDto))
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest.getId()), Long.class))
                .andExpect(jsonPath("$.requestor", is(itemRequest.getRequestor()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequest.getCreated().toString())));
    }

    @Test
    void findUserItemRequests() throws Exception {
        when(service.findUserItemRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestFullDto));

        mvc.perform(get("/requests")
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestFullDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestFullDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestFullDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(item.getId()), Long.class));
    }

    @Test
    void findAllItemRequests() throws Exception {
        when(service.findAllItemRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestFullDto));

        mvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestFullDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestFullDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestFullDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(item.getId()), Long.class));
    }

    @Test
    void findItemByIdIsOk() throws Exception {
        when(service.getItemRequest(anyLong(), anyLong()))
                .thenReturn(Optional.of(itemRequestFullDto));

        mvc.perform(get("/requests/{id}", 1)
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestFullDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestFullDto.getCreated().toString())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(item.getId()), Long.class));
    }

    @Test
    void findItemByIdIsNotFound() throws Exception {
        when(service.getItemRequest(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        mvc.perform(get("/requests/{id}", 1)
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}