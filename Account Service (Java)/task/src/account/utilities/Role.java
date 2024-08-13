package account.utilities;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum Role {
    USER("ROLE_USER", "USER"),
    ACCOUNTANT("ROLE_ACCOUNTANT", "ACCOUNTANT"),
    ADMINISTRATOR("ROLE_ADMINISTRATOR", "ADMINISTRATOR"),
    AUDITOR("ROLE_AUDITOR", "AUDITOR");

    private final String authority;
    private final String role;

    Role(String authority, String role) {
        this.authority = authority;
        this.role = role;
    }


    public static Role findByStringRoleNullable(String stringRole) {
        for (Role current : values()) {
            if (current.getRole().equals(stringRole)) return current;
        }
        return null;
    }

    public static boolean isStringRoleValid(String stringRole) {
        return Objects.nonNull(findByStringRoleNullable(stringRole));
    }

    public static boolean isBusinessRole(Role role) {
        return USER.equals(role) || ACCOUNTANT.equals(role) || AUDITOR.equals(role);
    }

    public static boolean isAdminRole(Role role) {
        return ADMINISTRATOR.equals(role);
    }
}
