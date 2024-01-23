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

    public UserJurisdiction() {
    }

    public UserJurisdiction(@NonNull User user, @NonNull Jurisdiction jurisdiction) {
        this.user = user;
        this.jurisdiction = jurisdiction;
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
}