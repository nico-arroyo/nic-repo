package nico;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Locale;

public class PlayerVerticle extends AbstractVerticle {
    private List<Player> players = new ArrayList<>();

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new PlayerVerticle());
    }

    @Override
    public void start() {
        // Sample data for testing
        players.add(new Player("Isco", "Alarcon", Position.CENTER_MIDFIELD, new ArrayList<>(Arrays.asList("Málaga", "Real Madrid", "Sevilla", "Betis")), PreferredLeg.AMBIDEXTROUS, Status.ACTIVE));
        Router router = Router.router(vertx);
        router.get("/player/findByStatus").handler(this::handleFindByStatus);
        router.get("/player/:playerName").handler(this::handleFindByName);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("Server started on port 8080");
                    } else {
                        System.out.println("Failed to start server: " + result.cause());
                    }
                });
    }

    private void handleFindByStatus(RoutingContext context) {
        String status = context.request().getParam("status");
        List<Player> filteredPlayers = players.stream()
                .filter(player -> player.getStatus().toString().equalsIgnoreCase(status))
                .toList();

        String jsonResult = filteredPlayers.stream()
                .map(player -> String.format("{\"name\":\"%s\", \"lastName\":\"%s\"}", player.getName(), player.getLastName()))
                .collect(Collectors.joining(",", "[", "]"));

        context.response()
                .putHeader("content-type", "application/json")
                .end(jsonResult);
    }

    private void handleFindByName(RoutingContext context) {
        String name = context.pathParam("playerName");
        List<Player> filteredPlayers = players.stream()
                .filter(player -> player.getName().equalsIgnoreCase(name))
                .toList();

        if (filteredPlayers.isEmpty()) {
            context.response()
                    .setStatusCode(404)
                    .end(String.format("{'result':'Player with name %s not found'}", name));
        } else {
            Player player = filteredPlayers.get(0);
            String jsonResult = String.format("{'result':'Player found: %s %s'}", player.getName(), player.getLastName());
            context.response()
                    .putHeader("content-type", "application/json")
                    .end(jsonResult);
        }
    }
}