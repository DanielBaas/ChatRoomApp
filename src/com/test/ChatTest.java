package com.test;

import com.client.ChatClient;

import java.util.Scanner;

public class ChatTest {

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);

        System.out.println("Nombre de usuario?");
        String userName = keyboard.nextLine().trim();

        System.out.println("Sala?");
        String roomName = keyboard.nextLine().trim();

        ChatClient client = new ChatClient(userName, roomName);

        //Desde la clase controlador de la vista de salas, yo debo crear un nuevo objeto vista y pasárselo a mi ChatClient
    }

}
