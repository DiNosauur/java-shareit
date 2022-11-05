package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository repository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private User validateUser(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new NotFoundException(String.format("Пользователь (id = %s) не найден", userId));
        }
        return user.get();
    }

    @Transactional
    @Override
    public ItemRequest saveItemRequest(ItemRequestDto itemRequestDto, long userId) {
        log.info("Добавление запроса ({}) пользователем (id={})", itemRequestDto.toString(), userId);
        validateUser(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, userId);
        return repository.save(itemRequest);
    }

    @Override
    public Collection<ItemRequestFullDto> findAllItemRequests(long userId, int from, int size) {
        log.info("Поиск всех запросов не от пользователя (id={})", userId);
        validateUser(userId);
        int page = from / size;
        return repository.findByRequestorNotOrderByCreatedDesc(userId, PageRequest.of(page, size))
                .stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestFullDto(itemRequest,
                        itemRepository.findByRequestId(itemRequest.getId())
                                .stream()
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemRequestFullDto> findUserItemRequests(long userId, int from, int size) {
        log.info("Поиск всех запросов пользователя (id={})", userId);
        validateUser(userId);
        int page = from / size;
        return repository.findByRequestorOrderByCreatedDesc(userId, PageRequest.of(page, size))
                .stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestFullDto(itemRequest,
                        itemRepository.findByRequestId(itemRequest.getId())
                                .stream()
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ItemRequestFullDto> getItemRequest(long id, long userId) {
        log.info("Получение информации о запросе (id={}) пользователем (id={})", id, userId);
        validateUser(userId);
        Optional<ItemRequest> itemRequest = repository.findById(id);
        if (itemRequest.isPresent()) {
            return Optional.of(ItemRequestMapper.toItemRequestFullDto(itemRequest.get(),
                    itemRepository.findByRequestId(id)
                            .stream()
                            .collect(Collectors.toList())));
        } else {
            return Optional.empty();
        }
    }
}
