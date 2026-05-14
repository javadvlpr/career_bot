package uz.career.career_bot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.career.career_bot.bot.handler.CallbackHandler;
import uz.career.career_bot.bot.handler.MessageHandler;
import uz.career.career_bot.config.BotConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class CareerBot extends TelegramLongPollingBot {

    private static final String NOOP = "noop";

    private final BotConfig botConfig;
    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallback(update);
            }
        } catch (TelegramApiException e) {
            log.error("Telegram API error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in bot update: ", e);
        }
    }

    private void handleMessage(Message msg) throws TelegramApiException {
        SendMessage response = null;
        if (msg.hasText()) {
            response = messageHandler.handle(msg);
        } else if (msg.hasContact()) {
            response = messageHandler.handleContact(msg);
        } else if (msg.hasDocument() || msg.hasPhoto()) {
            response = messageHandler.handleFile(msg);
        }
        if (response != null) execute(response);
    }

    private void handleCallback(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String data = update.getCallbackQuery().getData();

        if (NOOP.equals(data)) return;

        SendMessage response = callbackHandler.handle(update.getCallbackQuery());

        if (!shouldKeepOriginalMessage(data)) {
            deleteMessage(chatId, messageId);
        }

        if (response != null) execute(response);
    }

    private boolean shouldKeepOriginalMessage(String data) {
        boolean isSkillToggle = (data.startsWith("uskill_") || data.startsWith("jskill_") || data.startsWith("editskill_"))
                && !data.endsWith("_done");
        boolean isPagination = data.startsWith("joblist_") || data.startsWith("hrcandlist_");
        boolean isCvView = data.startsWith("view_cv_");
        return isSkillToggle || isPagination || isCvView;
    }

    private void deleteMessage(Long chatId, Integer messageId) {
        try {
            DeleteMessage delete = DeleteMessage.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .build();
            execute(delete);
        } catch (TelegramApiException e) {
            log.debug("Could not delete message: {}", e.getMessage());
        }
    }
}