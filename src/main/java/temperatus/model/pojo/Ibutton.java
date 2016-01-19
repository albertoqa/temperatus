package temperatus.model.pojo;
// Generated 08-dic-2015 14:25:06 by Hibernate Tools 4.3.1.Final

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Ibutton generated by hbm2java
 */
@Entity
@Table(name = "IBUTTON", schema = "PUBLIC", catalog = "DATABASE")
public class Ibutton implements java.io.Serializable {

	private Integer id;
	private Position position;
	private String serial;
	private String model;

	public Ibutton() {
	}

	public Ibutton(Position position, String serial, String model) {
		this.position = position;
		this.serial = serial;
		this.model = model;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEFAULTPOS")
	public Position getPosition() {
		return this.position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@Column(name = "SERIAL", length = 200)
	public String getSerial() {
		return this.serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	@Column(name = "MODEL", length = 200)
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

}
