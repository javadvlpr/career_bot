package uz.career.career_bot.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String entity, Long id) {
        super(entity + " not found (id=" + id + ")", HttpStatus.NOT_FOUND);
    }
}