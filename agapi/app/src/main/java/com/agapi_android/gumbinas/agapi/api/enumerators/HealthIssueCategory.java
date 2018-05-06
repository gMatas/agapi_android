package com.agapi_android.gumbinas.agapi.api.enumerators;

public enum HealthIssueCategory {

    HEART("Širdis", "Širdies problemos"),
    NEUROLOGICAL("Neurologinė liga", "Neurologinės problemos"),
    PSYCHIATRIC("Psichologinė liga", "Psichologinės problemos"),
    VISUAL("Rega", "Regos problemos"),
    HEARING("Klausa", "Klausos problemos"),
    OTHER("Kita", "Kitos problemos");

    private String _name;
    private String _description;

    HealthIssueCategory(String name, String description) {
        _name = name;
        _description = description;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }
}
