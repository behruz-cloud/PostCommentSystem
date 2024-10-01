package uz.pdp.bot;

import uz.pdp.bot.entity.BotController;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        BotController botController = new BotController();
        botController.start();


    }
}
