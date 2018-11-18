package app;

import models.Notebook;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import repositories.NotebookRepository;
import repositories.NotebookRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        if(update.hasMessage()){
            Message message = update.getMessage();

            //check if the message has text. it could also  contain for example a location ( message.hasLocation() )
            if(message.hasText()){
                if (message.getText().matches("/start")){
                    StringBuilder builder = new StringBuilder();
                    builder.append("commands:\n")
                            .append("/show - показать все записи\n")
                            .append("/add - добавить запись\n")
                            .append("/edit - изменить запись\n")
                            .append("\n")
                            .append("Введите ФИО, для просмотра полной информации");
                    sendMsg(message.getChatId().toString(), builder.toString());
                }else if (message.getText().matches("/add")){
                    sendMsg(message.getChatId().toString(),"Введите ФИО");
                }else if (message.getText().matches("/edit")){
                    sendMsg(message.getChatId().toString(),"Введите ФИО");
                }else if (message.getText().matches("/show")){
                    sendMsg(message.getChatId().toString(),"Введите ФИО");
                }else {
                    String messageText[] = message.getText().split(" ");
                    Optional<Notebook> candidate = repository.findBySNP(message.getChatId(), messageText[0], messageText[1], messageText[2]);
                    if (candidate.isPresent()){
                        sendMsg(message.getChatId().toString(), candidate.get().toString());
                    }
                }
            }//end if()
        }//end  if()
    }

    private void sendMsg(String chatId, String message){
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(chatId); //who should get the message? the sender from which we got the message...
        sendMessageRequest.setText(message);
        try {
            execute(sendMessageRequest); //at the end, so some magic and send the message ;)
        } catch (TelegramApiException e) {
            //do some error handling
        }//end catch()
    }

    public void sendCustomKeyboard(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Choose what do you prefer");

        // Create ReplyKeyboardMarkup object
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        // Create the keyboard (list of keyboard rows)
        List<KeyboardRow> keyboard = new ArrayList<>();
        // Create a keyboard row
        KeyboardRow row = new KeyboardRow();
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add("Kill yourself");
        // Add the first row to the keyboard
        keyboard.add(row);
        // Create another keyboard row
        row = new KeyboardRow();
        // Set each button for the second line
        row.add("Save yourself");
        // Add the second row to the keyboard
        keyboard.add(row);
        // Set the keyboard to the markup
        keyboardMarkup.setKeyboard(keyboard);
        // Add it to the message
        message.setReplyMarkup(keyboardMarkup);

        try {
            // Send the message
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendImageFromUrl(String url, String chatId) {
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto();
        // Set destination chat id
        sendPhotoRequest.setChatId(chatId);
        // Set the photo url as a simple photo
        sendPhotoRequest.setPhoto(url);
        try {
            // Execute the method
            execute(sendPhotoRequest);
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
