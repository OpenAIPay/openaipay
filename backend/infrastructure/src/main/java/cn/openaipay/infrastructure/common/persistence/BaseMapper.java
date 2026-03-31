package cn.openaipay.infrastructure.common.persistence;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 通用持久化Mapper基类
 *
 * 业务场景：替代Spring Data JPA的通用仓储能力，统一提供save/saveAll/findById/count等基础能力，
 * 保障各业务域迁移到MyBatis-Plus后调用风格与原仓储实现保持一致。
 *
 * @param <T> 持久化DO类型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface BaseMapper<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {

    /**
     * 保存DO
     *
     * 业务场景：仓储实现通过该方法统一处理新增与更新，避免在各聚合仓储中重复编写insert/update分支。
     *
     * @param entity 待保存DO
     * @return 保存后的DO
     */
    default T save(T entity) {
        if (entity == null) {
            return null;
        }
        Object idValue = extractId(entity);
        if (idValue == null) {
          /**
           * 处理业务数据。
           */
            insert(entity);
            return entity;
        }
        int updatedRows = updateById(entity);
        if (updatedRows <= 0) {
          /**
           * 处理业务数据。
           */
            insert(entity);
        }
        return entity;
    }

    /**
     * 批量保存DO
     *
     * 业务场景：RBAC角色关联、银行卡批量初始化等场景需要一次性持久化多条记录。
     *
     * @param entities DO集合
     * @return 保存后的DO列表
     */
    default List<T> saveAll(Collection<T> entities) {
        List<T> saved = new ArrayList<>();
        if (entities == null || entities.isEmpty()) {
            return saved;
        }
        for (T entity : entities) {
            saved.add(save(entity));
        }
        return saved;
    }

    /**
     * 主键查询
     *
     * 业务场景：仓储以Optional形式返回DO，保持调用方语义不变。
     *
     * @param id 主键
     * @return 查询结果
     */
    default Optional<T> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(selectById(id));
    }

    /**
     * 查询全部
     *
     * 业务场景：后台管理员列表、角色列表等全量读取场景。
     *
     * @return 全量DO列表
     */
    default List<T> findAll() {
        return selectList(null);
    }

    /**
     * 统计总数
     *
     * 业务场景：优惠券发放总量等统计场景。
     *
     * @return 记录总数
     */
    default long count() {
        Long total = selectCount(null);
        return total == null ? 0L : total;
    }

    private Object extractId(T entity) {
        Class<?> entityClass = entity.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo != null && tableInfo.getKeyProperty() != null && !tableInfo.getKeyProperty().isBlank()) {
            Object tableIdValue = readFieldValue(entityClass, entity, tableInfo.getKeyProperty());
            if (tableIdValue != null) {
                return tableIdValue;
            }
        }
        return readFieldValue(entityClass, entity, "id");
    }

    private Object readFieldValue(Class<?> entityClass, T entity, String fieldName) {
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(entity);
            } catch (NoSuchFieldException ignore) {
                current = current.getSuperclass();
            } catch (IllegalAccessException e) {
                /**
                 * 处理业务数据。
                 */
                throw new IllegalStateException("unable to read field '" + fieldName + "' from entity", e);
            }
        }
        return null;
    }
}
