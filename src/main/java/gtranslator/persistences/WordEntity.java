package gtranslator.persistences;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "WORD", schema = "PUBLIC", uniqueConstraints = {
		@UniqueConstraint(columnNames = "ID"),
		@UniqueConstraint(columnNames = "ENG") }, indexes = { @Index(name = "IDX_WORD_ENG", columnList = "ENG") })
public class WordEntity implements Serializable {

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEng() {
		return eng;
	}

	public void setEng(String eng) {
		this.eng = eng.trim().replaceAll("[ ]+", " ").toLowerCase();
	}

	public int getUseCounter() {
		return useCounter;
	}

	public void setUseCounter(int useCounter) {
		this.useCounter = useCounter;
	}

	private static final long serialVersionUID = -1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;

	@Column(name = "ENG", unique = true, nullable = false)
	private String eng;

	@Column(name = "USE_COUNTER", unique = false, nullable = false)
	private int useCounter = 1;

	@Column(name = "VISIBLE", unique = false, nullable = false)
	private boolean visible = false;
}
