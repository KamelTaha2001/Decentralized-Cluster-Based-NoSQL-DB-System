package kamel.capstone.nosqlnode.data.model;

import java.util.Objects;

public class Condition {
    private String column;
    private ConditionType conditionType;
    private String value;
    private static final Condition emptyCondition = new Condition();

    public Condition(String column, ConditionType conditionType, String value) {
        this.column = column;
        this.conditionType = conditionType;
        this.value = value;
    }

    public Condition() {
        column = "_id";
        conditionType = ConditionType.NOT_EQUAL;
        value = "-1";
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isEmptyCondition() { return this.equals(emptyCondition); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Condition condition)) return false;

        return Objects.equals(column, condition.column) && conditionType == condition.conditionType && Objects.equals(value, condition.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, conditionType, value);
    }
}
