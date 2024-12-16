package org.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Restaurant {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        System.out.println("Добро пожаловать в ресторан :)");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<Menu> menu = loadData("src/main/resources/Menu.json", objectMapper, new TypeReference<>() {});
            List<Client> clients = loadData("src/main/resources/Clients.json", objectMapper, new TypeReference<>() {});

            List<Agent> agents = new ArrayList<>();

            processClientsInParallel(clients, menu, agents);

            writeAgents("src/main/resources/agents.json", agents, objectMapper);
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }

    private static <T> List<T> loadData(String filePath, ObjectMapper objectMapper, TypeReference<List<T>> typeReference) throws IOException {
        try {
            return objectMapper.readValue(new File(filePath), typeReference);
        } catch (Exception e) {
            System.out.println("Ошибка загрузки данных из файла: " + filePath);
            throw new IOException("Некорректный формат файла JSON", e);
        }
    }

    private static void processClientsInParallel(List<Client> clients, List<Menu> menu, List<Agent> agents) {
        for (Client client : clients) {
            executorService.submit(() -> processClient(client, menu, agents));
        }

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println("Ошибка при ожидании завершения потоков: " + e.getMessage());
        }
    }

    private static void processClient(Client client, List<Menu> menu, List<Agent> agents) {
        long startTime = System.currentTimeMillis();
        System.out.println("Обработка заказа для клиента " + client.name);

        if (client.order.isEmpty()) {
            System.out.println("У клиента " + client.name + " нет заказов.");
            return;
        }

        double total = 0;
        for (String itemName : client.order) {
            System.out.println("Обрабатывается заказ на: " + itemName);
            Optional<Menu> dish = menu.stream().filter(d -> d.name.equals(itemName)).findFirst();

            if (dish.isPresent()) {
                total += dish.get().price;
                System.out.println("Блюдо найдено: " + itemName + ", цена: " + dish.get().price + " рублей");
                System.out.println("Повар готовит блюдо");
                agents.add(new CookAgent("Повар", "Готовит", "Блюдо " + itemName));
            } else {
                System.out.println("Блюдо не найдено: " + itemName);
                agents.add(new Agent("Повар", "Сообщает", "Блюдо " + itemName + " отсутствует"));
            }
        }
        System.out.println("Итоговая сумма для " + client.name + ": " + total + " рублей");

        long endTime = System.currentTimeMillis();
        System.out.println("Время обработки заказа для клиента " + client.name + ": " + (endTime - startTime) + " мс");
    }

    private static void writeAgents(String filePath, List<Agent> agents, ObjectMapper objectMapper) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), agents);
    }
}
