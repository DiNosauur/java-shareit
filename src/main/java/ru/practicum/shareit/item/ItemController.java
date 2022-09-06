package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;
    private final UserService userService;

    @GetMapping
    public Collection<Item> findItemItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        return service.findUserItems(userId);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody ItemDto itemDto,
                                           @RequestHeader("X-Sharer-User-Id") long userId) {
        if (itemDto.getAvailable() == null) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        if (!userService.getUser(userId).isPresent()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        User user = userService.getUser(userId).get();
        Item item = ItemMapper.toItem(itemDto, user, null);
        if (!service.checkItem(item)) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(service.saveItem(item), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable long id,
                                           @RequestBody ItemDto itemDto,
                                           @RequestHeader("X-Sharer-User-Id") long userId) {
        if (!userService.getUser(userId).isPresent()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        User user = userService.getUser(userId).get();
        itemDto.setId(id);
        Item item = ItemMapper.toItem(itemDto, user, null);
        if (!service.getItem(id).isPresent() ||
                !service.getItem(id).get().getOwner().equals(user)) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        if (!service.checkItem(item)) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
        return service.updateItem(item).map(updatedItem -> new ResponseEntity<>(updatedItem, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> findItemById(@PathVariable long id) {
        return service.getItem(id).map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Item> deleteItemById(@PathVariable long id,
                                               @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.deleteItem(id, userId) ? new ResponseEntity<>(null, HttpStatus.OK)
                : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/search")
    public Collection<Item> searchItems(@RequestParam(required = false) String text) {
        return service.searchItems(text);
    }

}
