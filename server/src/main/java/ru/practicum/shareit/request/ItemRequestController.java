package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ResponseEntity<ItemRequest> createItemRequest(@RequestBody ItemRequestDto itemRequestDto,
                                                         @RequestHeader("X-Sharer-User-Id") long userId) {
        return new ResponseEntity<>(service.saveItemRequest(itemRequestDto, userId), HttpStatus.OK);
    }

    @GetMapping
    public Collection<ItemRequestFullDto> findUserItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                               @RequestParam(defaultValue = "0") int from,
                                                               @RequestParam(defaultValue = "20") int size) {
        return service.findUserItemRequests(userId, from, size);
    }

    @GetMapping("/all")
    public Collection<ItemRequestFullDto> findAllItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                              @RequestParam(defaultValue = "0") int from,
                                                              @RequestParam(defaultValue = "20") int size) {
        return service.findAllItemRequests(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemRequestFullDto> findItemById(@PathVariable long id,
                                                           @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.getItemRequest(id, userId).map(itemRequest -> new ResponseEntity<>(itemRequest, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
