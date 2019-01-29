package com.gy.businessCore.dao.impl;

import com.gy.businessCore.common.EntityAndDTO;
import com.gy.businessCore.dao.BusinessDao;
import com.gy.businessCore.entity.*;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

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

    @Override
    public BusinessEntity getBusinessByClusterAndImage(String cluster, String image) {
        String sql = "FROM BusinessEntity entity WHERE entity.cluster =:cluster AND entity.image =:image";
        List<BusinessEntity> result = em.createQuery(sql, BusinessEntity.class)
                .setParameter("cluster", cluster)
                .setParameter("image",image)
                .getResultList();
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public List<BusinessEntity> getBusinessList() {
        String sql = "FROM BusinessEntity";
        return em.createQuery(sql, BusinessEntity.class)
                .getResultList();
    }

    @Override
    public List<BusinessEntity> getBusinessListByPage(int startIndex, int pageSize) {
        String sql = "select  * FROM tbl_business";
        Query query = em.createNativeQuery(sql);
        //将查询结果集转为Map
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        //设置分页
        query.setFirstResult(startIndex);
        query.setMaxResults(pageSize);
        //获取查询结果集
        List<Map<String, Object>> rungroupInfo = query.getResultList();
        List<BusinessEntity> rungrouppushList = EntityAndDTO.mapConvertToBean(rungroupInfo, BusinessEntity.class);
        return rungrouppushList;
    }

    @Override
    public BusinessEntity getBusinessByUuid(String  uuid) {
        String sql = "FROM BusinessEntity entity WHERE entity.uuid =:uuid";
        List<BusinessEntity> result = em.createQuery(sql, BusinessEntity.class)
                .setParameter("uuid", uuid)
                .getResultList();
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    @Override
    @Transactional
    public boolean insertBusinessResource(BusinessResourceEntity entity) {
        try {
            em.merge(entity);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public List<BusinessResourceEntity> getBusinessResourcesByBusinessId(String businessId) {
        String sql = "FROM BusinessResourceEntity Where businessUuid =:businessId";
        return em.createQuery(sql, BusinessResourceEntity.class)
                .setParameter("businessId",businessId)
                .getResultList();
    }

    @Override
    public List<BusinessResourceEntity> getBusinessResourceByMonitorUuid(String monitorUuid) {
        String sql = "FROM BusinessResourceEntity Where monitorId =:monitorUuid";
        return em.createQuery(sql, BusinessResourceEntity.class)
                .setParameter("monitorUuid",monitorUuid)
                .getResultList();
    }


}
