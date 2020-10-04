package sample;


import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    public static String searchType = "";
    @FXML
    AnchorPane map;
    @FXML
    ComboBox sourceCity;
    @FXML
    ComboBox destinationCity;
    @FXML
    Button goBtn;
    @FXML
    TextArea finalPath;
    @FXML
    RadioButton UCS;
    @FXML
    RadioButton AStar;

    // initialize required variable.
    City[] palestineCities = new City[51]; //store all cities.
    City[] selectedCities = new City[2];   //store user input.
    //UNIFORM COST SEARCH Requirements.
    LinkedList<road>[] adjacencyList;
    ArrayList<City> visited = new ArrayList<>();
    PriorityQueue<City> pq;
    //UI Handling
    boolean shouldDisable = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            goBtn.setDisable(shouldDisable);
            ToggleGroup group=new ToggleGroup();
            UCS.setToggleGroup(group);
            AStar.setToggleGroup(group);
            UCS.setSelected(true);
            this.readFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFile() throws IOException {

        //initialize adjacency list
        adjacencyList = new LinkedList[51];
        for (int j = 0; j < adjacencyList.length; j++) {
            adjacencyList[j] = new LinkedList<road>();
        }

        //Read city file,parse it and fill the cities array
        File cityFile = new File("Cities.txt");
        BufferedReader br = new BufferedReader(new FileReader(cityFile));
        String line;
        int counter = 0;

        while ((line = br.readLine()) != null) {
            String[] info = line.split("~");
            String cityName = info[0];
            double cityYAxis = Double.parseDouble(info[2]);
            double cityXAxis = Double.parseDouble(info[1]);
            String arbicName = info[3];
            String additionalInfo = "";
            if (info.length == 5)
                additionalInfo = info[4];


            City currentCity = new City(counter, cityName, arbicName, additionalInfo, cityXAxis * 1.63, cityYAxis * 1.72);
            palestineCities[counter] = currentCity;
            counter++;
        }

        File adjFile = new File("adj.txt");
        BufferedReader adjBr = new BufferedReader(new FileReader(adjFile));
        String adjLine;

        while ((adjLine = adjBr.readLine()) != null) {
            String[] info = adjLine.split("~");
            City init = find(info[0]);
            City adj = find(info[1]);
            double distance = Double.parseDouble(info[2]);
            road r = new road(adj, distance);
            adjacencyList[init.getId()].add(r);
        }

        this.fillMap();

//        {Dummy Testing uncomment to check}

//        palestineCities[0]=new City(0,"S",0,0,Double.POSITIVE_INFINITY);
//        palestineCities[1]=new City(1,"A",0,0,Double.POSITIVE_INFINITY);
//        palestineCities[2]=new City(2,"B",0,0,Double.POSITIVE_INFINITY);
//        palestineCities[3]=new City(3,"C",0,0,Double.POSITIVE_INFINITY);
//        palestineCities[4]=new City(4,"D",0,0,Double.POSITIVE_INFINITY);
//        palestineCities[5]=new City(5,"E",0,0,Double.POSITIVE_INFINITY);
//        palestineCities[6]=new City(6,"G",0,0,Double.POSITIVE_INFINITY);
//        palestineCities[7]=new City(7,"F",0,0,Double.POSITIVE_INFINITY);
//        palestineCities[8]=new City(8,"H",0,0,Double.POSITIVE_INFINITY);
//
//        adjacencyList[0].add(new road(find("A"),5));
//        adjacencyList[0].add(new road(find("B"),2));
//        adjacencyList[0].add(new road(find("C"),4));
//        adjacencyList[1].add(new road(find("D"),9));
//        adjacencyList[1].add(new road(find("E"),4));
//        adjacencyList[2].add(new road(find("G"),6));
//        adjacencyList[3].add(new road(find("F"),2));
//        adjacencyList[4].add(new road(find("H"),7));
//        adjacencyList[7].add(new road(find("G"),1));
//        adjacencyList[5].add(new road(find("G"),6));
//
//
    }

    public void uniformCostSearch(City source, City goal) {
        searchType = "UCS";
        pq = new PriorityQueue<City>();
        visited.clear();
        source.setCost(0.0);
        pq.add(source);

        while (!pq.isEmpty()) {

            City newMin = pq.remove();
            double minCost = newMin.getCost();
            visited.add(newMin);
            if (newMin.getCityName().equals(goal.getCityName())) {
                printUniformCostSearchPath(newMin, source);
                return;
            }
            for (int i = 0; i < adjacencyList[newMin.getId()].size(); i++) {
                City adj = adjacencyList[newMin.getId()].get(i).c;
                adj.setCost(adjacencyList[newMin.getId()].get(i).distance + minCost);

                if (!visited.contains(adj)) {
                    pq.add(adj);
                    adj.predecessor = newMin;
                }
            }
        }
    }

    public void AStartSearch(City source, City goal) {
        searchType = "A*";
        pq = new PriorityQueue<City>();
        visited.clear();
        source.setfN(0.0 + this.heuristicValueCalculator(source, goal));
        source.setgN(0.0);
        pq.add(source);
        while (!pq.isEmpty()) {
            City newMin = pq.remove();
            visited.add(newMin);

            if (newMin.getCityName().equals(goal.getCityName())) {
                this.printAStartSearchPath(newMin, source);
                return;
            }
            for (int i = 0; i < adjacencyList[newMin.getId()].size(); i++) {
                City adj = adjacencyList[newMin.getId()].get(i).c;
                adj.setgN(0.0);
                double gN = adjacencyList[newMin.getId()].get(i).distance + newMin.getgN();
                double hN = this.heuristicValueCalculator(adj, goal);
                double fN = gN + hN;
                adj.setfN(fN);
                adj.setgN(gN);
                adj.sethN(hN);
                if (!visited.contains(adj)) {
                    pq.add(adj);
                    adj.predecessor = newMin;
                }
            }
        }
    }


    public double heuristicValueCalculator(City c1, City c2) {
        double x1 = c1.getCityXAxis();
        double y1 = c1.getCityYAxis();
        double x2 = c2.getCityXAxis();
        double y2 = c2.getCityYAxis();
        double straightDistance = Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
        return straightDistance / 5;
    }

    public void printAStartSearchPath(City goal, City source) {
        double cost = Math.round(goal.getgN() * 100.0) / 100.0;
        finalPath.appendText("COST ==> " + cost + " KM\n");
        finalPath.appendText("--------------------------\n");

        LinkedList<City> path = new LinkedList<City>();
        path.add(goal);
        while (!goal.getCityName().equals(source.getCityName())) {
            path.add(goal.predecessor);
            goal = goal.predecessor;
        }
        for (int i = 0; i < path.size() - 1; i++) {

            Line partialPath = new Line();
            partialPath.setStartX(path.get(i).getCityXAxis());
            partialPath.setStartY(path.get(i).getCityYAxis());
            partialPath.setEndX(path.get(i + 1).getCityXAxis());
            partialPath.setEndY(path.get(i + 1).getCityYAxis());
            partialPath.setStrokeWidth(7);
            partialPath.setStroke(Color.RED);
            partialPath.setOpacity(0.8);
            partialPath.setId("path");
            Circle c = new Circle(path.get(i).getCityXAxis(), path.get(i).getCityYAxis(), 7);
            c.setFill(Color.AQUA);
            c.setId("pathCircle");
            map.getChildren().addAll(partialPath, c);

        }
        for (int i = path.size() - 1; i >= 0; i--) {
            finalPath.appendText(path.get(i).getCityName() + " - " + path.get(i).getArabicName() + " - " + path.get(i).getAdditionalInfo() + "\n");
            finalPath.appendText("--------------------------\n");
        }

    }

    // UI Handling Method's.
    public void printUniformCostSearchPath(City goal, City source) {
        double cost = Math.round(goal.getCost() * 100.0) / 100.0;
        finalPath.appendText("COST ==> " + cost + " KM\n");
        finalPath.appendText("--------------------------\n");
        LinkedList<City> path = new LinkedList<City>();
        path.add(goal);
        while (!goal.getCityName().equals(source.getCityName())) {
            path.add(goal.predecessor);
            goal = goal.predecessor;
        }
        for (int i = 0; i < path.size() - 1; i++) {

            Line partialPath = new Line();
            partialPath.setStartX(path.get(i).getCityXAxis());
            partialPath.setStartY(path.get(i).getCityYAxis());
            partialPath.setEndX(path.get(i + 1).getCityXAxis());
            partialPath.setEndY(path.get(i + 1).getCityYAxis());
            partialPath.setStrokeWidth(7);
            partialPath.setStroke(Color.RED);
            partialPath.setOpacity(0.8);
            partialPath.setId("path");
            Circle c = new Circle(path.get(i).getCityXAxis(), path.get(i).getCityYAxis(), 7);
            c.setFill(Color.AQUA);
            c.setId("pathCircle");
            map.getChildren().addAll(partialPath, c);

        }
        for (int i = path.size() - 1; i >= 0; i--) {
            finalPath.appendText(path.get(i).getCityName() + " - " + path.get(i).getArabicName() + " - " + path.get(i).getAdditionalInfo() + "\n");
            finalPath.appendText("--------------------------\n");
        }
    }

    public void pathAlgorithmRequested() {
        finalPath.setText("");
        for (int i = 0; i < 51; i++) {
            palestineCities[i].setCost(Double.POSITIVE_INFINITY);
        }
        Set<Node> prevPath = map.lookupAll("#path");
        for (Node p : prevPath) {
            Line path = (Line) p;
            map.getChildren().remove(path);
        }
        Set<Node> prevPathCircle = map.lookupAll("#pathCircle");
        for (Node p : prevPathCircle) {
            Circle path = (Circle) p;
            map.getChildren().remove(path);
        }
        if (UCS.isSelected())
            this.uniformCostSearch(selectedCities[0], selectedCities[1]);
        else if (AStar.isSelected())
            AStartSearch(selectedCities[0],selectedCities[1]);
    }

    public void clear() {
        finalPath.setText("");
        sourceCity.setValue(null);
        destinationCity.setValue(null);
        selectedCities[0] = null;
        selectedCities[1] = null;
        UCS.setSelected(true);
        goBtn.setDisable(true);
        Set<Node> prevPath = map.lookupAll("#path");
        for (Node p : prevPath) {
            Line path = (Line) p;
            map.getChildren().remove(path);
        }
        Set<Node> prevPathCircle = map.lookupAll("#pathCircle");
        for (Node p : prevPathCircle) {
            Circle path = (Circle) p;
            map.getChildren().remove(path);
        }
    }

    public void exit() {
        System.exit(0);
    }

    public void fillMap() {
        for (City palestineCity : palestineCities) {
            Circle dot = new Circle(palestineCity.getCityXAxis(), (palestineCity.getCityYAxis()), 5);
            dot.setFill(Color.GRAY);
            dot.setId(palestineCity.getCityName().replaceAll(" ", ""));
            map.getChildren().add(dot);
            destinationCity.getItems().addAll(palestineCity.getCityName());
            sourceCity.getItems().addAll(palestineCity.getCityName());

        }
        sourceCity.setOnAction(e -> {
            City firstSelection = find(String.valueOf(sourceCity.getValue()));
            this.findSelected(firstSelection, e);
        });

        destinationCity.setOnAction(e -> {
            City firstSelection = find(String.valueOf(destinationCity.getValue()));
            this.findSelected(firstSelection, e);
        });

    }

    public void handleInput() {
        shouldDisable = selectedCities[0] == null || selectedCities[1] == null;
        goBtn.setDisable(shouldDisable);
    }

    public void findSelected(City city, Event e) {

        if (((ComboBox) e.getSource()).getId().equals("sourceCity")) {
            Set<Node> selectedCircle = map.lookupAll("#selectedSource");
            for (Node c : selectedCircle) {
                Circle circle = (Circle) c;
                circle.setFill(Color.GRAY);
                circle.setRadius(5);
                circle.setId(selectedCities[0].getCityName().replaceAll(" ", ""));
            }
            if (city == null)
                return;

            Set<Node> selected = map.lookupAll("#" + city.getCityName().replaceAll(" ", ""));
            for (Node c : selected) {
                Circle circle = (Circle) c;
                circle.setFill(Color.AQUA);
                circle.setRadius(7);
                circle.setId("selectedSource");
            }

            selectedCities[0] = city;
        } else if (((ComboBox) e.getSource()).getId().equals("destinationCity")) {
            Set<Node> selectedCircle = map.lookupAll("#selectedDestination");
            for (Node c : selectedCircle) {
                Circle circle = (Circle) c;
                circle.setFill(Color.GRAY);
                circle.setRadius(5);
                circle.setId(selectedCities[1].getCityName().replaceAll(" ", ""));
            }
            if (city == null)
                return;

            Set<Node> selected = map.lookupAll("#" + city.getCityName().replaceAll(" ", ""));
            for (Node c : selected) {
                Circle circle = (Circle) c;
                circle.setFill(Color.AQUA);
                circle.setRadius(7);
                circle.setId("selectedDestination");
            }
            selectedCities[1] = city;
        }
        handleInput();


    }


    public City find(String cityName) {
        for (int i = 0; i < 51; i++) {
            if (palestineCities[i].getCityName().equals(cityName.trim())) {
                return palestineCities[i];
            }
        }
        return null;
    }


}



