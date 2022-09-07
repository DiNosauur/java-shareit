package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConflictException extends RuntimeException {
    public ConflictException(String s) {
        super(s);
        log.error(s);
    }
}
