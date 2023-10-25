package reactiveland.show.season2.episode3;

public record Advice(String advice) {
    public boolean isLongEnough() {
        return advice.length() > 3;
    }
}
