package bot;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.*;
import java.util.*;
public class CleanerBot extends TelegramLongPollingBot {
    BotService botService = new BotService();
    DbService dbService=new DbService();
    Set<Long> chatIdSet=new HashSet<>();
    ArrayList<String> arabian = new ArrayList<>(67);
    BufferedReader reader;

    {
        try {
            reader = new BufferedReader(new FileReader("src/main/resources/arabian.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                arabian.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return "Cleaner_groups_bot";
    }

    @Override
    public String getBotToken() {
        return "1677029522:AAEzoaD4yVg0-zJabMTLpaHmODdBrUXFfRI";
    }

    @Override
    public void onUpdateReceived(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        if (chatIdSet.isEmpty()) {
            chatIdSet = dbService.returnData();
        }
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            Integer userId = message.getFrom().getId();

            String check="";
            GetChatMember chatMemberStatus = botService.getChatMemberStatus(chatId, userId);
            try {
                check=execute(chatMemberStatus).getStatus();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            boolean checkAdmin = check.equals("creator") || check.equals("administrator");

//      Join and Left Hider -->
            if (message.getLeftChatMember() != null) {
                if (message.getLeftChatMember().getId() == 1677029522) {
                    dbService.deleteUser(chatId);
                    return;
//       remove user
                } else {
                    deleteMessage.setChatId(String.valueOf(message.getChatId()));
                    deleteMessage.setMessageId(message.getMessageId());
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
//            Add memmber
            if (message.getNewChatMembers().size() > 0) {
                if (message.getNewChatMembers().get(0).getId() == 1677029522) {
                    dbService.newUser(chatId);
                    return;
                } else {
                    if (message.getNewChatMembers().get(0).getIsBot() && !checkAdmin) {
                        User user = message.getNewChatMembers().get(0);
                        KickChatMember kickChatMember=new KickChatMember();
                        kickChatMember.setChatId(String.valueOf(chatId));
                        kickChatMember.setUserId(user.getId());
                        try {
                            execute(kickChatMember);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    deleteMessage.setChatId(String.valueOf(message.getChatId()));
                    deleteMessage.setMessageId(message.getMessageId());
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }

//      Join and Left Hider <--
//      Stickerni uchirish -->
            if (message.hasSticker() && message.getCaptionEntities()!=null) {
                if (!checkAdmin) {
                    try {
                        execute(botService.deleteSticker(message));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (message.hasText()) {
                String text = message.getText();
                if (!chatIdSet.contains(chatId)) {
                    dbService.newUser(chatId);
                }
//      Salomlashish -->
                if (text.equals("/start")) {
                    try {
                        execute(botService.isStart(message));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
//      Salomlashish <--

                } else {
                    if (message.getChatId() < 0) {
//      Linkni va Arab yozuvni uchirish -->
                        if (!checkAdmin) {
                            System.out.println("text -> " + text);
                            String type = "";
                            if (message.hasEntities()) {
                                List<MessageEntity> entity = message.getEntities();
                                for (int i = 0; i < entity.size(); i++) {
                                    type += entity.get(i).getType();
                                }
                            }
                            for (int i = 0; i < arabian.size(); i++) {
                                if (text.contains(arabian.get(i))) {
                                    type = "url";
                                }
                            }
                            if (type.contains("url") || type.contains("text_link")) {
                                try {
                                    execute(botService.deleteLink(message));
                                    execute(botService.sendWarning(message));
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
//      Linkni va Arab yozuvni uchirish <--
                        }
                    }
                }
            }
        }
    }
}

