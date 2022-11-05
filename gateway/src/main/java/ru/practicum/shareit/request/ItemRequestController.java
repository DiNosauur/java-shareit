package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                                    @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestClient.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findUserItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam(defaultValue = "0") int from,
                                                       @RequestParam(defaultValue = "20") int size) {
        validatePage(from, size);
        return itemRequestClient.getItemRequests(userId, from, size);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @RequestParam(defaultValue = "0") int from,
                                                      @RequestParam(defaultValue = "20") int size) {
        validatePage(from, size);
        return itemRequestClient.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findItemById(@PathVariable long id,
                                               @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestClient.getItemRequest(userId, id);
    }

    private int validatePage(int from, int size) {
        if (size <= 0) {
            throw new ValidationException(String.format("Параметр size (%s) задан некорректно", size));
        }
        if (from < 0) {
            throw new ValidationException(String.format("Параметр from (%s) задан некорректно", from));
        }
        int page = from / size;
        return page;
    }

}
