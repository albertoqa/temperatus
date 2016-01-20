package temperatus.model.pojo;
// Generated 20-ene-2016 21:22:04 by Hibernate Tools 4.3.1.Final

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Project generated by hbm2java
 */
@Entity
@Table(name = "PROJECT", schema = "PUBLIC", catalog = "DATABASE")
public class Project implements java.io.Serializable {

	private Integer id;
	private String name;
	private Date dateIni;
	private String observations;

	public Project() {
	}

	public Project(String name, Date dateIni, String observations) {
		this.name = name;
		this.dateIni = dateIni;
		this.observations = observations;
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

	@Column(name = "NAME", length = 100)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "DATE_INI", length = 8)
	public Date getDateIni() {
		return this.dateIni;
	}

	public void setDateIni(Date dateIni) {
		this.dateIni = dateIni;
	}

	@Column(name = "OBSERVATIONS")
	public String getObservations() {
		return this.observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

}
