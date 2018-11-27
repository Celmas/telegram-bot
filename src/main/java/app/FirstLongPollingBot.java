package app;

import models.Notebook;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;
import repositories.NotebookRepository;
import repositories.NotebookRepositoryImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FirstLongPollingBot extends TelegramLongPollingBot {
    private NotebookRepository repository;

    public FirstLongPollingBot(DefaultBotOptions options) {
        super(options);
        DriverManagerDataSource datasource = new DriverManagerDataSource();
        datasource.setDriverClassName("org.postgresql.Driver");
        datasource.setUsername("postgres");
        datasource.setPassword("qwerty007");
        datasource.setUrl("jdbc:postgresql://localhost:5432/telegram_bot_db");
        this.repository = new NotebookRepositoryImpl(datasource);
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText()) {
                if ("/start".equalsIgnoreCase(message.getText()) || "/help".equalsIgnoreCase(message.getText())) {
                    String commands = "Команды:\n" +
                            "/show - показать все записи\n" +
                            "/add - добавить запись\n" +
                            "/edit - изменить запись\n" +
                            "/del - удаление записи\n" +
                            "\n" +
                            "Введите ФИО, для просмотра полной информации";
                    sendMsg(message.getChatId().toString(), commands);
                }else if(message.getText().toLowerCase().startsWith("/add")){
                    if ("/add".equalsIgnoreCase(message.getText())) {
                        sendMsg(message.getChatId().toString(), "Введите /add <Фамилия> <Имя> <Отчество> <Номер телефона> <Описание>\nБез '<' и '>'");
                    }else {
                        String messageText[] = message.getText().split(" ");
                        if (messageText.length > 5){
                            String surname = messageText[1];
                            String name = messageText[2];
                            String patronymic = messageText[3];
                            String phone = messageText [4];
                            StringBuilder description = new StringBuilder();
                            for (int i = 5; i < messageText.length; i++){
                                description.append(messageText[i]).append(" ");
                            }
                            repository.save(Notebook.builder()
                                    .chatId(message.getChatId())
                                    .surname(surname)
                                    .name(name)
                                    .patronymic(patronymic)
                                    .phone(phone)
                                    .description(description.toString())
                                    .build());
                            sendMsg(message.getChatId().toString(), "Успешно добавлен");
                        }else {
                            sendMsg(message.getChatId().toString(), "Напишите /add для получения информации по добавлению записи");
                        }
                    }
                }else if(message.getText().toLowerCase().startsWith("/edit")){
                    if ("/edit".equalsIgnoreCase(message.getText())) {
                        sendMsg(message.getChatId().toString(), "Напишите /edit <номер записи> <Фамилия> <Имя> <Отчество> <Номер телефона> <Описание>\nБез '<' и '>'");
                    } else {
                        String messageText[] = message.getText().split(" ");
                        if (messageText.length > 6){
                            List<Notebook> notebooks = repository.findByChatId(message.getChatId());
                            Notebook notebook = notebooks.get(Integer.parseInt(messageText[1])-1);
                            notebook.setSurname(messageText[2]);
                            notebook.setName(messageText[3]);
                            notebook.setPatronymic(messageText[4]);
                            notebook.setPhone(messageText[5]);
                            StringBuilder description = new StringBuilder();
                            for (int i = 6; i < messageText.length; i++){
                                description.append(messageText[i]).append(" ");
                            }
                            notebook.setDescription(description.toString());
                            repository.update(notebook);
                            sendMsg(message.getChatId().toString(), "Запись " + messageText[1] + "обновлена");
                        } else {
                            sendMsg(message.getChatId().toString(), "Напишите /edit для получения информации по изменению записи");
                        }
                    }
                } else if ("/show".equalsIgnoreCase(message.getText())) {
                    List<Notebook> notebooks = repository.findByChatId(message.getChatId());
                    StringBuilder stringBuilder = new StringBuilder();
                    if (notebooks.size() == 0) {
                        sendMsg(message.getChatId().toString(), "Пусто :(\nСначала добвьте запись с помощью /add");
                    } else{
                        int i = 1;
                        for (Notebook notebook : notebooks) {
                            stringBuilder.append(i++)
                                    .append(" ")
                                    .append(notebook.getSurname())
                                    .append(" ")
                                    .append(notebook.getName())
                                    .append(" ")
                                    .append(notebook.getPatronymic())
                                    .append("\n");
                        }
                        sendMsg(message.getChatId().toString(), stringBuilder.toString());
                    }
                } else if ("/del".equalsIgnoreCase(message.getText())) {
                    sendCustomKeyboard(message.getChatId().toString(), repository.findByChatId(message.getChatId()));
                } else {
                    String messageText[] = message.getText().split(" ");
                    if (messageText.length == 3){
                        Optional<Notebook> candidate = repository.findBySNP(message.getChatId(), messageText[0], messageText[1], messageText[2]);
                        if (candidate.isPresent()){
                            Notebook notebook = candidate.get();
                            sendMsg(message.getChatId().toString(), notebook.toString());
                        }else sendMsg(message.getChatId().toString(), "Такого пользователя нет");
                    }else {
                        sendMsg(message.getChatId().toString(), "Напишите /help для получения информации по работе с ботом");
                    }
                }
            }
        }else if (update.hasCallbackQuery()){
            String callData = update.getCallbackQuery().getData();
            if(callData.toLowerCase().startsWith("del_")){
                String[] callDataArray = callData.split("_");
                repository.delete(Long.parseLong(callDataArray[1]));
            }
        }
    }

    private void sendMsg(String chatId, String message) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(chatId); //who should get the message? the sender from which we got the message...
        sendMessageRequest.setText(message);
        try {
            execute(sendMessageRequest);
        } catch (TelegramApiException e) {
            //do some error handling
        }//end catch()
    }

    private void sendCustomKeyboard(String chatId, List<Notebook> notebooks) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите какую запись удалить");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        notebooks.forEach(notebook -> {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText(notebook.getSurname() + " " + notebook.getName() + " " + notebook.getPatronymic()).setCallbackData("del_" + notebook.getId()));
            rowsInline.add(rowInline);

        });
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return "YouAreAwesome";
    }

    public String getBotToken() {
        return "661693148:AAFPnVd-XR_mdcW8w7tYCx748bh9yDOL2mg";
    }
}
