package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.model.Item;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemService service;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private LocalDateTime start = LocalDateTime.of(2022, 10, 1, 12, 0, 1);

    private LocalDateTime end = start.plusDays(7);

    private LocalDateTime commentCreated = end.plusDays(1);

    private ItemDto itemDto = new ItemDto(1L, "Дрель", "Инструмент для сверления", true, 2L);

    private Item item = new Item();

    private ItemFullDto itemFullDto = new ItemFullDto();

    private CommentDto commentDto = new CommentDto(1L, "Не хватает ударной функции", "Dima", commentCreated);

    private Booking booking = new Booking();

    @BeforeEach
    void setUp() {
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setRequestId(itemDto.getRequestId());
        item.setOwner(1L);

        booking.setId(2L);
        booking.setItemId(item.getId());
        booking.setBookerId(3L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingStatus.APPROVED);

        itemFullDto.setId(item.getId());
        itemFullDto.setName(item.getName());
        itemFullDto.setDescription(item.getDescription());
        itemFullDto.setAvailable(item.getAvailable());
        itemFullDto.setRequestId(item.getRequestId());
        itemFullDto.setLastBooking(booking);
        itemFullDto.setComments(List.of(commentDto));
    }

    @Test
    void findUserItems() throws Exception {
        when(service.findUserItems(anyLong()))
                .thenReturn(List.of(itemFullDto));

        mvc.perform(get("/items")
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item.getName())))
                .andExpect(jsonPath("$[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.id", is(itemFullDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.itemId", is(itemFullDto.getLastBooking().getItemId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.bookerId", is(itemFullDto.getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.start", is(itemFullDto.getLastBooking().getStart().toString())))
                .andExpect(jsonPath("$[0].lastBooking.end", is(itemFullDto.getLastBooking().getEnd().toString())))
                .andExpect(jsonPath("$[0].lastBooking.status", is(itemFullDto.getLastBooking().getStatus().toString())))
                .andExpect(jsonPath("$[0].comments", hasSize(1)))
                .andExpect(jsonPath("$[0].comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$[0].comments[0].created", is(commentDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].comments[0].authorName", is(commentDto.getAuthorName())));
    }

    @Test
    void createItem() throws Exception {
        when(service.saveItem(any(), anyLong()))
                .thenReturn(item);

        mvc.perform(post("/items")
                .content(mapper.writeValueAsString(itemDto))
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())))
                .andExpect(jsonPath("$.owner", is(item.getOwner()), Long.class))
                .andExpect(jsonPath("$.requestId", is(item.getRequestId()), Long.class));
    }

    @Test
    void updateItemIsOk() throws Exception {
        when(service.updateItem(anyLong(), any(), anyLong()))
                .thenReturn(Optional.of(item));

        mvc.perform(patch("/items/{id}", 1)
                .content(mapper.writeValueAsString(itemDto))
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())))
                .andExpect(jsonPath("$.owner", is(item.getOwner()), Long.class))
                .andExpect(jsonPath("$.requestId", is(item.getRequestId()), Long.class));
    }

    @Test
    void updateItemIsNotFound() throws Exception {
        when(service.updateItem(anyLong(), any(), anyLong()))
                .thenReturn(Optional.empty());

        mvc.perform(patch("/items/{id}", 1)
                .content(mapper.writeValueAsString(itemDto))
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findItemByIdIsOk() throws Exception {
        when(service.getItem(anyLong(), anyLong()))
                .thenReturn(Optional.of(itemFullDto));

        mvc.perform(get("/items/{id}", 1)
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.id", is(itemFullDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.itemId", is(itemFullDto.getLastBooking().getItemId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(itemFullDto.getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.start", is(itemFullDto.getLastBooking().getStart().toString())))
                .andExpect(jsonPath("$.lastBooking.end", is(itemFullDto.getLastBooking().getEnd().toString())))
                .andExpect(jsonPath("$.lastBooking.status", is(itemFullDto.getLastBooking().getStatus().toString())))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$.comments[0].created", is(commentDto.getCreated().toString())))
                .andExpect(jsonPath("$.comments[0].authorName", is(commentDto.getAuthorName())));
    }

    @Test
    void findItemByIdIsNotFound() throws Exception {
        when(service.getItem(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        mvc.perform(get("/items/{id}", 1)
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteItemByIdIsOk() throws Exception {
        when(service.deleteItem(anyLong(), anyLong()))
                .thenReturn(true);

        mvc.perform(delete("/items/{id}", 1)
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItemByIdIsNotFound() throws Exception {
        when(service.deleteItem(anyLong(), anyLong()))
                .thenReturn(false);

        mvc.perform(delete("/items/{id}", 1)
                .content(mapper.writeValueAsString(itemDto))
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchItems() throws Exception {
        when(service.searchItems(anyString()))
                .thenReturn(List.of(item));

        mvc.perform(get("/items/search")
                .header("X-Sharer-User-Id", "1")
                .param("text", "дрель")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item.getName())))
                .andExpect(jsonPath("$[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].owner", is(item.getOwner()), Long.class));
    }

    @Test
    void addItemCommentIsOk() throws Exception {
        when(service.addItemComment(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(commentDto));

        mvc.perform(post("/items/{id}/comment", 1)
                .content(mapper.writeValueAsString(commentDto))
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated().toString())));
    }

    @Test
    void addItemCommentIsNotFound() throws Exception {
        when(service.addItemComment(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        mvc.perform(post("/items/{id}/comment", 1)
                .content(mapper.writeValueAsString(commentDto))
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}