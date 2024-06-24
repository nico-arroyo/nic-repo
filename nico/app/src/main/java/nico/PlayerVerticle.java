package nico;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerVerticle extends AbstractVerticle {
    private JDBCClient jdbcClient;
    private List<Player> players = new ArrayList<>();

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new PlayerVerticle());
    }

    @Override
    public void start() {
        players.add(new Player("Isco", "Alarcon", Position.CENTER_MIDFIELD, "Betis", new ArrayList<>(Arrays.asList("Malaga", "Real Madrid", "Sevilla")), PreferredLeg.AMBIDEXTROUS, Status.ACTIVE));

        Router router = Router.router(vertx);
        router.get("/player").handler(this::handleFindPlayer);

        final JsonObject config = new JsonObject()
                .put("url", "jdbc:h2:mem:test_mem;")
                .put("driver_class", "org.h2.Driver")
                .put("max_pool_size", 30);

        jdbcClient = JDBCClient.createShared(vertx, config);

        router.route().handler(BodyHandler.create());
        router.post("/player").handler(this::handleAddPlayer);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("Server started on port 8080");
                        initializeDatabase();
                    } else {
                        System.out.println("Failed to start server: " + result.cause());
                    }
                });
    }

    private void initializeDatabase() {
        jdbcClient.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection connection = ar.result();
                String sql = "CREATE TABLE IF NOT EXISTS player ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "name VARCHAR(255), "
                        + "lastName VARCHAR(255), "
                        + "position VARCHAR(255), "
                        + "currentTeam VARCHAR(255), "
                        + "formerTeams VARCHAR(255), "
                        + "preferredLeg VARCHAR(255), "
                        + "status VARCHAR(255))";
                connection.execute(sql, res -> {
                    if (res.succeeded()) {
                        System.out.println("Database initialization succeeded");
                    } else {
                        System.out.println("Database initialization failed: " + res.cause());
                    }
                    connection.close();
                });
            } else {
                System.out.println("Database connection failed: " + ar.cause());
            }
        });
    }

    private void handleAddPlayer(RoutingContext context) {
        JsonObject json = context.getBodyAsJson();

        String name = json.getString("name");
        String lastName = json.getString("lastName");
        Position position = Position.valueOf(json.getString("position"));
        String currentTeam = json.getString("currentTeam");
        String formerTeams = json.getJsonArray("formerTeams").encode();
        PreferredLeg preferredLeg = PreferredLeg.valueOf(json.getString("preferredLeg"));
        Status status = Status.valueOf(json.getString("status"));

        jdbcClient.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection connection = ar.result();
                String sql = "INSERT INTO player (name, lastName, position, currentTeam, formerTeams, preferredLeg, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                JsonArray params = new JsonArray().add(name).add(lastName).add(position).add(currentTeam).add(formerTeams).add(preferredLeg).add(status);
                connection.updateWithParams(sql, params, res -> {
                    if (res.succeeded()) {
                        context.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json")
                                .end(new JsonObject().put("message", "Player added successfully").encodePrettily());
                    } else {
                        context.response()
                                .setStatusCode(500)
                                .putHeader("content-type", "application/json")
                                .end(new JsonObject().put("error", res.cause().getMessage()).encodePrettily());
                    }
                    connection.close();
                });
            } else {
                context.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).encodePrettily());
            }
        });
    }

    private void handleFindPlayer(RoutingContext context) {
        String name = context.request().getParam("name");
        String lastName = context.request().getParam("lastName");
        String position = context.request().getParam("position");
        String currentTeam = context.request().getParam("currentTeam");
        String formerTeam = context.request().getParam("formerTeam");
        String preferredLeg = context.request().getParam("preferredLeg");
        String status = context.request().getParam("status");


        List<JsonObject> filteredPlayers = players.stream()
                .filter(player -> (name == null || player.getName().equalsIgnoreCase(name)) &&
                        (lastName == null || player.getLastName().equalsIgnoreCase(lastName)) &&
                        (position == null || player.getPosition().toString().equalsIgnoreCase(position)) &&
                        (currentTeam == null || player.getCurrentTeam().equalsIgnoreCase(currentTeam)) &&
                        (formerTeam == null || player.getFormerTeams().stream().anyMatch(team -> team.equalsIgnoreCase(formerTeam))) &&
                        (preferredLeg == null || player.getPreferredLeg().toString().equalsIgnoreCase(preferredLeg)) &&
                        (status == null || player.getStatus().toString().equalsIgnoreCase(status)))
                .map(Player::toJson)
                .toList();


        if (filteredPlayers.isEmpty()) {
            context.response()
                    .setStatusCode(200)
                    .end(new JsonObject().encodePrettily());
        } else {
            JsonArray jsonResult = new JsonArray(filteredPlayers);
            context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(jsonResult.encodePrettily());
        }
    }
}
