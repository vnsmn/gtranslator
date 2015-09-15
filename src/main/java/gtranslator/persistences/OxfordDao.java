package gtranslator.persistences;

import gtranslator.App;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class OxfordDao {
    public void save(OxfordEntity entity) {
        try (Session ses = App.getH2Service().openSession()) {
            OxfordEntity ent = ses.get(OxfordEntity.class, entity.getId());
            Transaction tr = ses.beginTransaction();
            if (ent != null) {
                ses.update(entity);
            } else {
                ses.persist(entity);
            }
            tr.commit();
        }
    }

    public void save(String eng, Boolean isMissingSound, Boolean isMissingPhon, String label) {
//        if (!App.getHistoryService().isWord(eng)) {
//            return;
//        }
        OxfordEntity ent = get(eng, label);
        try (Session ses = App.getH2Service().openSession()) {
            ses.getTransaction().begin();
            if (ent != null) {
                ses.refresh(ent);
                ent.setLabel(label);
                ent.setMissingSound(isMissingSound);
                ent.setMissingPhon(isMissingPhon);
                ses.update(ent);
            } else {
                ent = new OxfordEntity();
                ent.setEng(eng);
                ent.setLabel(label);
                ent.setMissingSound(isMissingSound);
                ent.setMissingPhon(isMissingPhon);
                ses.persist(ent);
            }
            ses.getTransaction().commit();
        }
    }

    public void saveSound(String eng, Boolean isMissingSound, String label) {
        save(eng, isMissingSound, null, label);
    }

    public void savePhon(String eng, Boolean isMissing, String label) {
        save(eng, null, isMissing, label);
    }

    public void delete(String eng, String label) {
        OxfordEntity ent = get(eng, label);
        try (Session ses = App.getH2Service().openSession()) {
            if (ent != null) {
                ses.getTransaction().begin();
                ses.refresh(ent);
                ses.delete(ent);
                ses.getTransaction().commit();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public OxfordEntity get(String eng, String label) {
        try (Session ses = App.getH2Service().openSession()) {
            Criteria criteria = ses.createCriteria(OxfordEntity.class);
            criteria.add(Restrictions.eq("eng", eng.trim().toLowerCase()));
            criteria.add(Restrictions.eq("label", label.trim().toLowerCase()));
            List<OxfordEntity> ents = criteria.list();
            return ents.size() == 0 ? null : ents.get(0);
        }
    }

    public List<OxfordEntity> list(Boolean isMissing) {
        try (Session ses = App.getH2Service().openSession()) {
            Criteria criteria = ses.createCriteria(OxfordEntity.class);
            if (isMissing != null) {
                criteria.add(Restrictions.eq("missing", isMissing));
            }
            return criteria.list();
        }
    }
}
