package bot;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMembersCount;
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class CleanerBot extends TelegramLongPollingBot {
    BotService botService = new BotService();
    DbService dbService = new DbService();
    Set<Long> chatIdSet = new HashSet<>();
    HashMap<Long, Integer> adminMap = new HashMap<>();
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

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {


        DeleteMessage deleteMessage = new DeleteMessage();
        if (chatIdSet.isEmpty()) {
            chatIdSet = dbService.returnData();
        }
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Integer messageId = message.getMessageId();
            Long chatId = message.getChatId();
            Integer userId = message.getFrom().getId();
            String userName = message.getFrom().getUserName();

            String check = "";
            GetChatMember chatMemberStatus = botService.getChatMemberStatus(chatId, userId);
            try {
                check = execute(chatMemberStatus).getStatus();
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
                    if (chatId < 0) {
                        GetChatMembersCount count = new GetChatMembersCount(String.valueOf(chatId));
                        String member;
                        try {
                            member = execute(count).toString();
                        } catch (Exception e) {
                            member = null;
                        }
                        Long membersCount = 0l;
                        if (member != null)
                            membersCount = Long.parseLong(member);
                        dbService.newUser(chatId, userName, membersCount);
                    } else {
                        dbService.newUser(chatId, userName, 1l);
                    }
                    chatIdSet = dbService.returnData();
                    return;
                } else {
                    if (message.getNewChatMembers().get(0).getIsBot() && !checkAdmin) {
                        User user = message.getNewChatMembers().get(0);
                        KickChatMember kickChatMember = new KickChatMember();
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
            if (message.hasSticker() && message.getCaptionEntities() != null) {
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


                SendMessage sendMessage = new SendMessage();

//                ADMIN PANEL LOGIN
                if (text.equals("exit")) {
                    adminMap.clear();
                }

                if (text.equals("login")) {
                    adminMap.put(chatId, 1);
                    sendMessage.setText("parolni kiriting");
                    sendMessage.setChatId(String.valueOf(chatId));
                    execute(sendMessage);
                    return;
                }
                // PASSWORD
                if (text.equals("970599")) {
                    if (adminMap.containsKey(chatId))
                        adminMap.put(chatId, 2);
                    deleteMessage.setMessageId(messageId);
                    deleteMessage.setChatId(String.valueOf(chatId));
                    execute(deleteMessage);

                    sendMessage.setText("xush kelibsiz\nrestore\nsendAll");
                    sendMessage.setChatId(String.valueOf(chatId));
                    execute(sendMessage);
                    return;
                }

                //SEND ALL
                if (text.equals("sendAll")) {
                    adminMap.put(chatId, 3);
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("postni jo'nating");
                    execute(sendMessage);
                }
                //IS OK
                if (text.equals("ok")) {
                    if (adminMap.get(chatId) == 3) {
                        ForwardMessage copyMessage = new ForwardMessage();
                        copyMessage.setFromChatId(String.valueOf(chatId));
                        copyMessage.setMessageId(messageId - 1);

                        ArrayList<Long> set = new ArrayList<>(dbService.returnData());

                        for (int i = 0; i < set.size(); i++) {
                            if ((i + 2) % 30 == 0) {
                                LocalTime time = LocalTime.now();
                                while (Math.abs(Duration.between(time, LocalTime.now()).toMillis()) < 1200) {
                                }
                            }
                            copyMessage.setChatId(String.valueOf(set.get(i)));
                            try {
                                execute(copyMessage);
                            } catch (TelegramApiException e) {
                                if (e.toString().contains("bot was blocked by the user")) {
                                    dbService.deleteUser(set.get(i));
                                }
                            }

                        }
                        sendMessage.setText("Yuborildi");
                        sendMessage.setChatId(String.valueOf(chatId));
                        execute(sendMessage);
                        return;
                    }
                }


                if (!chatIdSet.contains(chatId)) {
                    if (chatId < 0) {
                        GetChatMembersCount count = new GetChatMembersCount(String.valueOf(chatId));
                        String member;
                        try {
                            member = execute(count).toString();
                        } catch (Exception e) {
                            member = null;
                        }
                        Long membersCount = 0l;
                        if (member != null)
                            membersCount = Long.parseLong(member);
                        dbService.newUser(chatId, userName, membersCount);
                    } else {
                        dbService.newUser(chatId, userName, 1l);
                    }
                    chatIdSet = dbService.returnData();
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
                            String type = "";
                            if (message.hasEntities()) {
                                List<MessageEntity> entity = message.getEntities();
                                for (int i = 0; i < entity.size(); i++) {
                                    type += entity.get(i).getType();
                                }
                            }

                            //CHECK ARABIAN TEXT
                            for (int i = 0; i < arabian.size(); i++) {
                                if (text.contains(arabian.get(i))) {
                                    type = "url";
                                }
                            }

                            //CHECK CAPTION URL
                            if (message.getCaptionEntities()!=null){
                                List<MessageEntity> captionEntities = message.getCaptionEntities();
                                for (MessageEntity captionEntity : captionEntities) {
                                    if (captionEntity.getType().equals("mention") || captionEntity.getType().equals("url")) {
                                        type="url";
                                    }
                                }
                            }

                            //CHECK URL AND LINK
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

