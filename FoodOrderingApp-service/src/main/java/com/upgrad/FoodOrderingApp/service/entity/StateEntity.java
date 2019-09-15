package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "state")
@NamedQueries(
        {
                @NamedQuery(name = "customerByUuid", query = "select u from CustomerEntity u where u.uuid = :uuid"),
                @NamedQuery(name = "customerByEmail", query = "select u from CustomerEntity u where u.email =:email"),
                @NamedQuery(name = "customerByContactNumber", query = "select u from CustomerEntity u where u.contactNumber =:contactNumber")
        }
)

public class StateEntity implements Serializable{
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "UUID")
    @Size(max = 200)
    private String uuid;

    @Column(name = "state_name")
    private String stateName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}
