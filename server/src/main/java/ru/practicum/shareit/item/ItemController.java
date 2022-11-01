package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    @GetMapping
    public Collection<ItemFullDto> findUserItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        return service.findUserItems(userId);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody ItemDto itemDto,
                                           @RequestHeader("X-Sharer-User-Id") long userId) {
        return new ResponseEntity<>(service.saveItem(itemDto, userId), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable long id,
                                           @RequestBody ItemDto itemDto,
                                           @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.updateItem(id, itemDto, userId).map(updatedItem -> new ResponseEntity<>(updatedItem, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemFullDto> findItemById(@PathVariable long id,
                                                    @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.getItem(id, userId).map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Item> deleteItemById(@PathVariable long id,
                                               @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.deleteItem(id, userId) ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/search")
    public Collection<Item> searchItems(@RequestParam(required = false) String text) {
        return service.searchItems(text);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<CommentDto> addItemComment(@PathVariable long id,
                                                     @RequestBody CommentDto commentDto,
                                                     @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.addItemComment(id, userId, commentDto).map(comment -> new ResponseEntity<>(comment, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
