package gtranslator.persistences;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "OXFORD", schema = "PUBLIC", uniqueConstraints = {
        @UniqueConstraint(columnNames = "ID"),
        @UniqueConstraint(columnNames = {"ENG", "LABEL"})},
        indexes = {@Index(name = "IDX_OXFORD_ENG_LABEL", columnList = "ENG,LABEL")})
public class OxfordEntity implements Serializable {
    private static final long serialVersionUID = -1L;

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

    public boolean isMissingSound() {
        return missingSound;
    }

    public void setMissingSound(Boolean missing) {
        if (missing != null) {
            this.missingSound = missing;
        }
    }

    public boolean isMissingPhon() {
        return missingPhon;
    }

    public void setMissingPhon(Boolean missing) {
        if (missing != null) {
            this.missingPhon = missing;
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label.trim().toLowerCase();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Integer id;

    @Column(name = "ENG", unique = false, nullable = false)
    private String eng;

    @Column(name = "MISSING_SOUND", unique = false, nullable = false)
    private boolean missingSound = false;

    @Column(name = "MISSING_PHON", unique = false, nullable = false)
    private boolean missingPhon = false;

    @Column(name = "LABEL", unique = false, nullable = false)
    private String label = "";
}
