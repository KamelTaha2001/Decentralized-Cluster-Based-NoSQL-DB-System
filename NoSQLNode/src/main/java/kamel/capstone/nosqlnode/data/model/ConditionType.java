package kamel.capstone.nosqlnode.data.model;

public enum ConditionType {
    EQUAL,
    NOT_EQUAL,
    GREATER,
    GREATER_OR_EQUAL,
    SMALLER,
    SMALLER_OR_EQUAL;

    public String getConditionSymbol() {
        switch (this) {
            case EQUAL -> {
                return "=";
            }
            case NOT_EQUAL -> {
                return "!=";
            }
            case GREATER -> {
                return ">";
            }
            case SMALLER -> {
                return "<";
            }
            case GREATER_OR_EQUAL -> {
                return ">=";
            }
            case SMALLER_OR_EQUAL -> {
                return "<=";
            }
            default -> {
                return "=";
            }
        }
    }
}
