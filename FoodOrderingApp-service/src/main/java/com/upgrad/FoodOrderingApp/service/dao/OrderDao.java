package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class OrderDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CouponEntity getCouponByName(String couponName) {
        try {
            return entityManager.createNamedQuery("couponByName", CouponEntity.class).setParameter("couponName", couponName)
                    .getSingleResult();
        } catch(NoResultException nre) {
            return null;
        }
    }

    public List<OrderEntity> getCustomerOrders(Long customerId) {
        try {
            return entityManager.createNamedQuery("ordersByCustomer", OrderEntity.class).setParameter("customer", customerId)
                    .getResultList();
        } catch(NoResultException nre) {
            return null;
        }
    }

    /**
     * Returns orders for a given restaurant
     *
     * @param restaurantEntity Restaurant to get orders for
     *
     * @return List<OrderEntity> object
     */
    public List<OrderEntity> getOrdersByRestaurant(RestaurantEntity restaurantEntity) {
        try {
            return entityManager.createNamedQuery("ordersByRestaurant", OrderEntity.class).setParameter("restaurant", restaurantEntity).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public OrderEntity saveOrder(OrderEntity ordersEntity) {
        entityManager.persist(ordersEntity);
        return ordersEntity;
    }
}