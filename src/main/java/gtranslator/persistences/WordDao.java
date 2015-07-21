package gtranslator.persistences;

import gtranslator.App;

import java.util.List;

import org.hibernate.Transaction;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class WordDao {
	public void save(WordEntity entity) {
		try (Session ses = App.getH2Service().openSession()) {
			WordEntity ent = ses.get(WordEntity.class, entity.getId());
			Transaction tr = ses.beginTransaction();
			if (ent != null) {
				ses.update(entity);
			} else {
				ses.persist(entity);
			}
			tr.commit();
		}
	}

	public void save(String eng) {
		save(eng, null);
	}

	public void save(String eng, Boolean isVisible) {
		WordEntity ent = get(eng);
		try (Session ses = App.getH2Service().openSession()) {
			ses.getTransaction().begin();
			if (ent != null) {
				ses.refresh(ent);
				ent.setUseCounter(ent.getUseCounter() + 1);
				if (isVisible != null) {
					ent.setVisible(isVisible);
				}
				ses.update(ent);
			} else {
				ent = new WordEntity();
				ent.setEng(eng);
				ses.persist(ent);
			}
			ses.getTransaction().commit();
		}
	}

	@SuppressWarnings("unchecked")
	public WordEntity get(String eng) {
		try (Session ses = App.getH2Service().openSession()) {
			Criteria criteria = ses.createCriteria(WordEntity.class);
			criteria.add(Restrictions.eq("eng", eng.trim().toLowerCase()));
			List<WordEntity> ents = criteria.list();
			return ents.size() == 0 ? null : ents.get(0);
		}
	}
}
