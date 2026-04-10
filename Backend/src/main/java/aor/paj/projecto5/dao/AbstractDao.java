package aor.paj.projecto5.dao;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

//It tells Wildfly: "Before running any method here, make sure a database transaction is started."
// If something fails, it rolls back automatically to keep your data safe.
@TransactionAttribute(TransactionAttributeType.REQUIRED)

//The T is a placeholder. When you create a UserDao extends AbstractDao<UserEntity>,
// the T becomes UserEntity. This allows you to have type-safe methods without duplicating code.
public abstract class AbstractDao<T extends Serializable> implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	
	private final Class<T> clazz;
	
	@PersistenceContext(unitName = "project5PU")
	protected EntityManager em;


	public AbstractDao(Class<T> clazz)
	{
		this.clazz = clazz;
	}

	//(Read): Fetches a specific row using its Primary Key (ID).
	public T find(Object id) 
	{
		return em.find(clazz, id);
	}

	//(Create): Takes a brand-new Java object and saves it as a new row in the DB.
	public void persist(final T entity) 
	{
		em.persist(entity);
	}
	
	//(Update): Takes an object that might have changed and syncs those changes back to the database.
	public void merge(final T entity) 
	{
		em.merge(entity);
	}

	//(Delete): Deletes the record. The line em.contains(entity) ?
	// ... is a safety check to ensure the object is "attached" to the current database session before trying to kill it.
	public void remove(final T entity) 
	{
		em.remove(em.contains(entity) ? entity : em.merge(entity));
	}
	

	public List<T> findAll() 
	{
		final CriteriaQuery<T> criteriaQuery = em.getCriteriaBuilder().createQuery(clazz);
		criteriaQuery.select(criteriaQuery.from(clazz));
		return em.createQuery(criteriaQuery).getResultList();
	}

	public void deleteAll() 
	{
		final CriteriaDelete<T> criteriaDelete = em.getCriteriaBuilder().createCriteriaDelete(clazz);
		criteriaDelete.from(clazz);
		em.createQuery(criteriaDelete).executeUpdate();
	}
	
	public void flush() {
		em.flush();
	}
}