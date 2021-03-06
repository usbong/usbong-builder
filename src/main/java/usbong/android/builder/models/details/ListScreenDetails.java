package usbong.android.builder.models.details;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by Rocky Camacho on 8/13/2014.
 */
public class ListScreenDetails {

    public static enum ListType {
        ANY_ANSWER("ANY_ANSWER"),
        SINGLE_ANSWER("SINGLE_ANSWER"),
        MULTIPLE_ANSWERS("MULTIPLE_ANSWERS");

        private final String name;

        private ListType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static ListType from(String name) {
            for (ListType listType : ListType.values()) {
                if (listType.getName().equals(name)) {
                    return listType;
                }
            }
            return null;
        }
    }

    @Expose
    private String text;
    @Expose
    private List<String> items;
    @Expose
    private String type;
    @Expose
    private boolean hasAnswer;
    @Expose
    private int answer;
    @Expose
    private int numberOfChecksNeeded = 1;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHasAnswer() {
        return hasAnswer;
    }

    public void setHasAnswer(boolean hasAnswer) {
        this.hasAnswer = hasAnswer;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public int getNumberOfChecksNeeded() {
        return numberOfChecksNeeded;
    }

    public void setNumberOfChecksNeeded(int numberOfChecksNeeded) {
        this.numberOfChecksNeeded = numberOfChecksNeeded;
    }
}
