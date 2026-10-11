package com.app.email.template;

/**
 * Picks the HTML file and default subject from the OTP purpose at send time.
 */
public enum EmailTemplateType {

    ONBOARDING("REGISTRATION", "onboarding.html", "Welcome %s — verify your E-Market Place email"),
    FORGOT_PASSWORD("PASSWORD_RESET", "forgot-password.html", "%s, reset your E-Market Place password"),
    REGISTRATION_SUCCESS("REGISTRATION_SUCCESS", "registration-success.html",
            "Congratulations %s — you're in at E-Market Place");

    private final String purpose;
    private final String resourceName;
    private final String subjectPattern;

    EmailTemplateType(String purpose, String resourceName, String subjectPattern) {
        this.purpose = purpose;
        this.resourceName = resourceName;
        this.subjectPattern = subjectPattern;
    }

    public String resourceName() {
        return resourceName;
    }

    public String subjectFor(String username) {
        return subjectPattern.formatted(username == null || username.isBlank() ? "there" : username);
    }

    public static EmailTemplateType fromPurpose(String purpose) {
        if (purpose == null) {
            return ONBOARDING;
        }
        for (EmailTemplateType type : values()) {
            if (type.purpose.equalsIgnoreCase(purpose.trim())) {
                return type;
            }
        }
        return ONBOARDING;
    }
}
