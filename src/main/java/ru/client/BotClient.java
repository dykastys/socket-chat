package ru.client;

import ru.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BotClient extends Client {
    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected String getUserName() {
        return "bot_" + (int) (Math.random() * 100);
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hi to all! A'm a chat-bot. I understand command: date, day, month, year, time, hour, minute, second");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);

            if(!message.contains(":")) {
                return;
            }

            String userName = message.substring(0, message.indexOf(":"));
            String userMessage = message.substring(message.indexOf(":") + 2).toLowerCase();

            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

            String mess = "Info for " + userName + ": ";
            switch (userMessage) {
                case "date":
                    simpleDateFormat.applyPattern("d.MM.YYYY");
                    sendTextMessage(mess + simpleDateFormat.format(date));
                    break;
                case "day":
                    simpleDateFormat.applyPattern("d");
                    sendTextMessage(mess + simpleDateFormat.format(date));
                    break;
                case "month":
                    simpleDateFormat.applyPattern("MMMM");
                    sendTextMessage(mess + simpleDateFormat.format(date));
                    break;
                case "year":
                    simpleDateFormat.applyPattern("YYYY");
                    sendTextMessage(mess + simpleDateFormat.format(date));
                    break;
                case "time":
                    simpleDateFormat.applyPattern("H:mm:ss");
                    sendTextMessage(mess + simpleDateFormat.format(date));
                    break;
                case "hour":
                    simpleDateFormat.applyPattern("H");
                    sendTextMessage(mess + simpleDateFormat.format(date));
                    break;
                case "minute":
                    simpleDateFormat.applyPattern("m");
                    sendTextMessage(mess + simpleDateFormat.format(date));
                    break;
                case "second":
                    simpleDateFormat.applyPattern("s");
                    sendTextMessage(mess + simpleDateFormat.format(date));
                    break;
            }
        }
    }
}
