package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

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

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @MockBean
    private BookingService service;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private LocalDateTime start = LocalDateTime.of(2022, 10, 1, 12, 0, 1);

    private LocalDateTime end = start.plusDays(7);

    interface Creator<T> {
        T create();
    }

    Creator<User> creatorUser = () -> {
        User user = new User();
        user.setId(1L);
        user.setName("Dima");
        user.setEmail("dimano@mail.ru");
        return user;
    };

    Creator<Item> creatorItem = () -> {
        Item item = new Item();
        item.setId(2L);
        item.setName("Дрель");
        item.setDescription("Инструмент для сверления");
        item.setOwner(3L);
        return item;
    };

    private User user = creatorUser.create();

    private Item item = creatorItem.create();

    private BookingFullDto bookingFullDto = new BookingFullDto(
            4L, start, end, BookingStatus.WAITING, user, item);

    private BookingDto bookingDto = new BookingDto(1L, start, end);

    @Test
    void createBooking() throws Exception {
        when(service.saveBooking(any(), anyLong()))
                .thenReturn(bookingFullDto);

        mvc.perform(post("/bookings")
                .content(mapper.writeValueAsString(bookingDto))
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(bookingFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingFullDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingFullDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingFullDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(bookingFullDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingFullDto.getBooker().getName())))
                .andExpect(jsonPath("$.item.id", is(bookingFullDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingFullDto.getItem().getName())));
    }

    @Test
    void updateBookingOk() throws Exception {
        when(service.updateBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(Optional.of(bookingFullDto));

        mvc.perform(patch("/bookings/{id}", 4)
                .param("approved", "true")
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingFullDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingFullDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingFullDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(bookingFullDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingFullDto.getBooker().getName())))
                .andExpect(jsonPath("$.item.id", is(bookingFullDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingFullDto.getItem().getName())));
    }

    @Test
    void updateBookingNotFound() throws Exception {
        when(service.updateBooking(1L, 1L, true))
                .thenReturn(Optional.empty());

        mvc.perform(patch("/bookings/{id}", 1)
                .param("approved", "true")
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void findBookingByIdOk() throws Exception {
        when(service.getBooking(anyLong(), anyLong()))
                .thenReturn(Optional.of(bookingFullDto));

        mvc.perform(get("/bookings/{id}", 4)
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingFullDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingFullDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingFullDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(bookingFullDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingFullDto.getBooker().getName())))
                .andExpect(jsonPath("$.item.id", is(bookingFullDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingFullDto.getItem().getName())));
    }

    @Test
    void findBookingByIdNotFound() throws Exception {
        when(service.getBooking(1L, 1L))
                .thenReturn(Optional.empty());

        mvc.perform(get("/bookings/{id}", 1)
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findUserBookings() throws Exception {
        when(service.findUserBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingFullDto));

        mvc.perform(get("/bookings")
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingFullDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingFullDto.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingFullDto.getEnd().toString())))
                .andExpect(jsonPath("$[0].status", is(bookingFullDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingFullDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(bookingFullDto.getBooker().getName())))
                .andExpect(jsonPath("$[0].item.id", is(bookingFullDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingFullDto.getItem().getName())));
    }

    @Test
    void findOwnerBookings() throws Exception {
        when(service.findOwnerBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingFullDto));

        mvc.perform(get("/bookings/owner")
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingFullDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingFullDto.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingFullDto.getEnd().toString())))
                .andExpect(jsonPath("$[0].status", is(bookingFullDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingFullDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(bookingFullDto.getBooker().getName())))
                .andExpect(jsonPath("$[0].item.id", is(bookingFullDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingFullDto.getItem().getName())));
    }

}