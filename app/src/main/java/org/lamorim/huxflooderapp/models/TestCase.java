package org.lamorim.huxflooderapp.models;

/**
 * Created by lucas on 03/02/2017.
 */

public class TestCase {
    private String input;
    private String output;
    private int id;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public static String deserializeInput(String input) {
        return input.replace("`", "\n");
    }
}
