package app.model.jurisdiction;

import jakarta.persistence.*;

@Entity
@Table(name = "remote_hosts")
public class RemoteHost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToOne
  @JoinColumn(name = "jurisdiction_id")
  private Jurisdiction jurisdiction;

  public RemoteHost() {
  }

  public RemoteHost(String name) {
    this.name = name;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Jurisdiction getJurisdiction() {
    return jurisdiction;
  }

  public void setJurisdiction(Jurisdiction jurisdiction) {
    this.jurisdiction = jurisdiction;
  }
}
