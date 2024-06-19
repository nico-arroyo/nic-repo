package nico;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class Player {
    private String name;
    private String lastName;
    private Position position;
    private ArrayList<String> teams;
    //private Locale nationality;
    private PreferredLeg preferredLeg;
    private Status status;

    public Player(String name, String lastName, Position position, ArrayList<String> teams, PreferredLeg preferredLeg, Status status) {
        this.name = name;
        this.lastName = lastName;
        this.position = position;
        this.teams = teams;
        //this.nationality = nationality;
        this.preferredLeg = preferredLeg;
        this.status = status;
    }

    public String getName() { return name; }
    public String getLastName() { return lastName; }
    public Position getPosition() { return position; }
    public ArrayList<String> getTeams() { return teams; }
    //public Locale getNationality() { return nationality; }
    public PreferredLeg getPreferredLeg() { return preferredLeg; }
    public Status getStatus() { return status; }

    public String toString() {
        return String.format("{name: %s, lastName: %s, position: %s, teams: %s, preferredLeg: %s, status: %s}",
                getName(), getLastName(), getPosition(), getTeams(), getPreferredLeg(), getStatus());
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("name", name)
                .put("lastName", lastName)
                .put("position", position)
                .put("clubs", new JsonArray(teams))
                .put("preferredLeg", preferredLeg)
                .put("status", status);
    }
}
