package com.gy.businessCore.dao.impl;

import com.gy.businessCore.dao.BusinessDao;
import com.gy.businessCore.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by gy on 2018/3/31.
 */
@Repository
public class BusinessDaoImpl implements BusinessDao {


    @Autowired
    @PersistenceContext
    EntityManager em;

    @Override
    public TestEntity getJPAInfo() {
        List<TestEntity> result = em.createQuery("FROM TestEntity", TestEntity.class)
                .getResultList();
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    @Override
    @Transactional
    public BusinessEntity insertBusiness(BusinessEntity entity) {
        return em.merge(entity);
    }

    @Override
    public List<String> getCluster() {
        String sql="select cluster from tbl_business group by cluster";
        return em.createNativeQuery(sql).getResultList();
    }

    @Override
    public List<BusinessEntity> getBusinessByCluster(String cluster) {
        String sql = "FROM BusinessEntity entity WHERE entity.cluster =:cluster";
        return em.createQuery(sql, BusinessEntity.class)
                .setParameter("cluster", cluster)
                .getResultList();
    }

}
