package george.test.exchange.core.processing.util;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

@Singleton
public class JPAUtilsBean {

    @PersistenceContext
    private EntityManager em;
    
    @Transactional
    public <T> void deleteAll(Class<T> clazz) {
        CriteriaBuilder cBuilder = em.getCriteriaBuilder();
        CriteriaDelete<T> cq = cBuilder.createCriteriaDelete(clazz);
        Root<T> root = cq.from(clazz);
        em.createQuery(cq).executeUpdate();        
    }
    
}
