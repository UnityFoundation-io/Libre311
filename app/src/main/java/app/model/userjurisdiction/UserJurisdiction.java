package app.model.userjurisdiction;


import app.model.jurisdiction.Jurisdiction;
import app.model.user.User;
import io.micronaut.core.annotation.NonNull;

import javax.persistence.*;

@Entity
@Table(name = "user_jurisdiction")
public class UserJurisdiction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User user;

    @NonNull
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Jurisdiction jurisdiction;

    private boolean isUserAdmin = false;

    public UserJurisdiction() {
    }

    public UserJurisdiction(@NonNull User user, @NonNull Jurisdiction jurisdiction, boolean isUserAdmin) {
        this.user = user;
        this.jurisdiction = jurisdiction;
        this.isUserAdmin = isUserAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NonNull
    public User getUser() {
        return user;
    }

    public void setUser(@NonNull User user) {
        this.user = user;
    }

    @NonNull
    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(@NonNull Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public boolean isUserAdmin() {
        return isUserAdmin;
    }

    public void setUserAdmin(boolean userAdmin) {
        isUserAdmin = userAdmin;
    }
}