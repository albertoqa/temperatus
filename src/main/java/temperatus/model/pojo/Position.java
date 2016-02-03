package temperatus.model.pojo;
// Generated 20-ene-2016 21:22:04 by Hibernate Tools 4.3.1.Final

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Position generated by hbm2java
 */
@Entity
@Table(name = "POSITION", schema = "PUBLIC", catalog = "DATABASE")
public class Position implements java.io.Serializable {

	private Integer id;
	private String place;
	private String picture;

	public Position() {
	}

	public Position(String place, String picture) {
		this.place = place;
		this.picture = picture;
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

	@Column(name = "PLACE", length = 100)
	public String getPlace() {
		return this.place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	@Column(name = "PICTURE", length = 200)
	public String getPicture() {
		return this.picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	@Override
	public String toString() {
		return place;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Position position = (Position) o;

		if (!id.equals(position.id)) return false;
		if (!place.equals(position.place)) return false;
		return !(picture != null ? !picture.equals(position.picture) : position.picture != null);

	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + place.hashCode();
		result = 31 * result + (picture != null ? picture.hashCode() : 0);
		return result;
	}
}
