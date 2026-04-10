package aor.paj.projecto5.dto;

import aor.paj.projecto5.utils.UserRoles;

public class LoginResponseDTO {
    private Long id;
    private String firstName;
    private UserRoles userRole;
    private String token;
    private String photoUrl;

    public LoginResponseDTO(Long id, String firstName, UserRoles userRole, String token,String photoUrl) {
        this.id = id;
        this.firstName = firstName;
        this.userRole = userRole;
        this.token = token;
        this.photoUrl = photoUrl;
    }

    public LoginResponseDTO(){}

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public UserRoles getUserRole() {
        return userRole;
    }

    public String getToken() {
        return token;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setUserRole(UserRoles userRole) {
        this.userRole = userRole;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
