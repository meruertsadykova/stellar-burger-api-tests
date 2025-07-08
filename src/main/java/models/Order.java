package models;

import java.util.List;
import lombok.Data;

@Data
public class Order {

    private List<String> ingredients;

    public Order(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public Order() {
    }

}
