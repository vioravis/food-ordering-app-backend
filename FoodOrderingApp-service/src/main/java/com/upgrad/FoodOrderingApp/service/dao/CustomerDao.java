package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * This class contain all Data access related operations for User table
 */

@Repository
public class CustomerDao {


    @PersistenceContext
    private EntityManager entityManager;

    // Create a new user
    public CustomerEntity createCustomer(CustomerEntity customerEntity) {
        entityManager.persist(customerEntity);
        return customerEntity;
    }

    // Persist authentication details
    public CustomerAuthTokenEntity createAuthCustomer(CustomerAuthTokenEntity customerAuthTokenEntity) {
        entityManager.persist(customerAuthTokenEntity);
        return customerAuthTokenEntity;
    }

    // Get user by UUID
    public CustomerEntity getCustomer(final String customerUuid) {
        try {
            return entityManager.createNamedQuery("customerByUuid", CustomerEntity.class).setParameter("uuid", customerUuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    //Get user by id
    public CustomerEntity getCustomerById(final Integer customerId) {
        try {
            return entityManager.createNamedQuery("customerById", CustomerEntity.class).setParameter("id", customerId)
                    .getSingleResult();
        } catch(NoResultException nre) {
            return null;
        }
    }


    // Get user by Username
    public CustomerEntity getCustomerByContactNumber(final String contactNumber) {
        try {
            return entityManager.createNamedQuery("customerByContactNumber", CustomerEntity.class).setParameter("contactNumber", contactNumber).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    // Get user by email
    public CustomerEntity getCustomerByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("customerByEmail", CustomerEntity.class).setParameter("email",email).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }


    // Delete user
    public void deleteCustomer(final CustomerEntity customerEntity) {
        entityManager.remove(customerEntity);
    }

    // Get authentication token by acesss token
    public CustomerAuthTokenEntity getCustomerAuthToken(final String accessToken) {
        try {
            return entityManager.createNamedQuery("customerAuthTokenByAccessToken", CustomerAuthTokenEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        } catch (NoResultException nre) {

            return null;
        }

    }

    // Persist the authentication token
    public CustomerAuthTokenEntity createAuthToken(final CustomerAuthTokenEntity customerAuthTokenEntity) {
        entityManager.persist(customerAuthTokenEntity);
        return customerAuthTokenEntity;
    }

    // Update an existing user
    public void updateCustomer(final CustomerEntity updatedUserEntity){

        entityManager.merge(updatedUserEntity);
    }

}