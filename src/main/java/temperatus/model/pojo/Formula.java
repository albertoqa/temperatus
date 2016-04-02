package temperatus.model.pojo;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Formula generated by hbm2java
 */
@Entity
@Table(name = "FORMULA", schema = "PUBLIC", catalog = "DATABASE", uniqueConstraints = @UniqueConstraint(columnNames = "NAME"))
public class Formula implements java.io.Serializable {

    private Integer id;
    private String name;
    private String description;
    private String reference;
    private String operation;

    public Formula() {
    }

    public Formula(String name, String operation) {
        this.name = name;
        this.operation = operation;
    }

    public Formula(String name, String description, String reference, String operation) {
        this.name = name;
        this.description = description;
        this.reference = reference;
        this.operation = operation;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "ID", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "NAME", unique = true, nullable = false, length = 50)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DESCRIPTION", length = 200)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "REFERENCE", length = 200)
    public String getReference() {
        return this.reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Column(name = "OPERATION", nullable = false, length = 200)
    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "Formula{" +
                "name='" + name + '\'' +
                '}';
    }


}
