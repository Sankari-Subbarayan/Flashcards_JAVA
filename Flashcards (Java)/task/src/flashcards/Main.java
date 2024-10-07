package flashcards;

import java.io.*;
import java.util.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

class Main {
    private static Map<String, String> flashcards = new HashMap<>(); // Store cards and definitions
    private static Map<String, Integer> mistakes = new HashMap<>();  // Store mistakes for each card
    private static StringBuilder log = new StringBuilder();          // Store log data
    private static Scanner scanner = new Scanner(System.in);
    private static String exportFile; // Declare exportFile at class level

    // Method for outputting messages and logging them
    public static void outputMsg(String msg) {
        System.out.println(msg);  // Print to console
        log.append(msg).append("\n");  // Log the message
    }

    // Method for getting user input and logging it
    public static String getUserInput() {
        String input = scanner.nextLine();  // Get input from user
        log.append("> ").append(input).append("\n");  // Log the input
        return input;
    }

    // Method to export log to a file
    public static void exportLog(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(log.toString());
            outputMsg("The log has been saved.");
        } catch (IOException e) {
            outputMsg("An error occurred while saving the log.");
        }
    }

    // Method to add a new card
    public static void addCard() {
        outputMsg("The card:");
        String card = getUserInput();
        if (flashcards.containsKey(card)) {
            outputMsg("The card \"" + card + "\" already exists.");
            return;
        }

        outputMsg("The definition of the card:");
        String definition = getUserInput();
        if (flashcards.containsValue(definition)) {
            outputMsg("The definition \"" + definition + "\" already exists.");
            return;
        }

        flashcards.put(card, definition);
        mistakes.put(card, 0);  // Initialize mistakes for this card
        outputMsg("The pair (\"" + card + "\":\"" + definition + "\") has been added.");
    }

    // Method to ask questions based on cards
    public static void askCards() {
        outputMsg("How many times to ask?");
        int times = Integer.parseInt(getUserInput());

        Object[] keys = flashcards.keySet().toArray();
        for (int i = 0; i < times; i++) {
            String card = (String) keys[i % keys.length];
            outputMsg("Print the definition of \"" + card + "\":");
            String answer = getUserInput();

            if (flashcards.get(card).equals(answer)) {
                outputMsg("Correct answer.");
            } else {
                mistakes.put(card, mistakes.get(card) + 1);
                String correctDefinition = flashcards.get(card);
                String altTerm = findTermByDefinition(answer);
                if (altTerm != null) {
                    outputMsg("Wrong. The right answer is \"" + correctDefinition + "\", but your definition is correct for \"" + altTerm + "\".");
                } else {
                    outputMsg("Wrong. The right answer is \"" + correctDefinition + "\".");
                }
            }
        }
    }

    // Method to find a term by definition
    public static String findTermByDefinition(String definition) {
        for (Map.Entry<String, String> entry : flashcards.entrySet()) {
            if (entry.getValue().equals(definition)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Method to find hardest card(s)
    public static void hardestCard() {
        int maxMistakes = Collections.max(mistakes.values());
        if (maxMistakes == 0) {
            outputMsg("There are no cards with errors.");
            return;
        }

        List<String> hardestCards = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : mistakes.entrySet()) {
            if (entry.getValue() == maxMistakes) {
                hardestCards.add(entry.getKey());
            }
        }

        String cardMessage = hardestCards.size() == 1 ?
                "The hardest card is \"" + hardestCards.get(0) + "\". You have " + maxMistakes + " errors answering it." :
                "The hardest cards are " + hardestCards.stream().map(card -> "\"" + card + "\"").collect(Collectors.joining(", ")) + ". You have " + maxMistakes + " errors answering them.";

        outputMsg(cardMessage);
    }

    // Method to reset stats
    public static void resetStats() {
        for (String card : mistakes.keySet()) {
            mistakes.put(card, 0);
        }
        outputMsg("Card statistics have been reset.");
    }

    // Method to import cards from a file
    public static void importCards(String filename) {
        try (Scanner fileScanner = new Scanner(new File(filename))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String card = parts[0].trim();
                    String definition = parts[1].trim();
                    flashcards.put(card, definition);
                    mistakes.put(card, 0);  // Initialize mistakes for this card
                }
            }
            outputMsg(flashcards.size() + " cards have been loaded.");
        } catch (FileNotFoundException e) {
            outputMsg("File not found.");
        }
    }

    // Method to export cards to a file
    public static void exportCards(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (Map.Entry<String, String> entry : flashcards.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }
            outputMsg(flashcards.size() + " cards have been saved.");
        } catch (IOException e) {
            outputMsg("An error occurred while saving the cards.");
        }
    }

    // Method to handle user commands
    public static void handleUserAction(String action) {
        switch (action) {
            case "add":
                addCard();
                break;
            case "ask":
                askCards();
                break;
            case "log":
                outputMsg("File name:");
                String filename = getUserInput();
                exportLog(filename);
                break;
            case "hardest card":
                hardestCard();
                break;
            case "reset stats":
                resetStats();
                break;
            case "exit":
                outputMsg("Bye bye!");
                // If exportFile is set, export the cards here
                if (exportFile != null) {
                    exportCards(exportFile);
                }
                System.exit(0);
                break;
            default:
                outputMsg("Unknown command: " + action);
        }
    }

    // Main method
    public static void main(String[] args) {
        String importFile = null;

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if ("-import".equals(args[i]) && i + 1 < args.length) {
                importFile = args[++i];
            } else if ("-export".equals(args[i]) && i + 1 < args.length) {
                exportFile = args[++i]; // Use the class-level exportFile
            }
        }

        // Import cards if specified
        if (importFile != null) {
            importCards(importFile);
        }

        while (true) {
            outputMsg("Input the action (add, ask, log, hardest card, reset stats, exit):");
            String action = getUserInput();
            handleUserAction(action);
        }
    }
}
