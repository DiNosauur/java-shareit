package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import javax.validation.Valid;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> findUserItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.getItems(userId);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemRequestDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@PathVariable long id,
                                             @RequestBody ItemRequestDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.updItem(userId, id, itemDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findItemById(@PathVariable long id,
                                               @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.getItem(userId, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItemById(@PathVariable long id,
                                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.delItem(userId, id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(required = false) String text,
                                              @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.searchItems(userId, text);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> addItemComment(@PathVariable long id,
                                                 @Valid @RequestBody CommentRequestDto commentDto,
                                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.addItemComment(userId, id, commentDto);
    }
}

