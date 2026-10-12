package top.mqxu.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.mqxu.mybatis.dto.UserQuery;
import top.mqxu.mybatis.entity.User;
import top.mqxu.mybatis.mapper.UserMapper;
import top.mqxu.mybatis.service.UserService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final int BATCH_SIZE = 100;

    /* ==================== 增 ==================== */

    /**
     * 练习③：新增 age=200 触发事务回滚
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createUser(User user) {
        applyDefaults(user);
        save(user);
        if (user.getAge() != null && (user.getAge() < 0 || user.getAge() > 150)) {
            throw new RuntimeException("年龄超出合理范围，回滚");
        }
        return true;
    }

    @Override
    public boolean createUserBatch(List<User> users) {
        if (users == null || users.isEmpty()) {
            return false;
        }
        for (User user : users) {
            applyDefaults(user);
        }
        return saveBatch(users, BATCH_SIZE);
    }

    /* ==================== 查 ==================== */

    /**
     * 练习①：getById 查不存在的 id 返回 null
     */
    @Override
    public User getUser(Long id) {
        return getById(id);
    }

    @Override
    public List<User> listByCondition(UserQuery query) {
        return list(buildCondition(query));
    }

    @Override
    public Page<User> pageByCondition(UserQuery query, long current, long size) {
        return page(new Page<>(current, size), buildCondition(query));
    }

    @Override
    public List<User> searchByKeyword(String keyword) {
        return list(new LambdaQueryWrapper<User>()
                .and(StringUtils.hasText(keyword),
                        w -> w.like(User::getUsername, keyword)
                                .or()
                                .like(User::getNickname, keyword))
                .orderByAsc(User::getId));
    }

    @Override
    public List<User> listByAgeRange(Integer minAge, Integer maxAge) {
        return list(new LambdaQueryWrapper<User>()
                .ge(minAge != null, User::getAge, minAge)
                .le(maxAge != null, User::getAge, maxAge)
                .orderByAsc(User::getAge));
    }

    @Override
    public List<User> listTop(Integer limit) {
        int size = limit == null || limit <= 0 ? 3 : Math.min(limit, 50);
        return list(new LambdaQueryWrapper<User>()
                .orderByDesc(User::getPoints)
                .last("LIMIT " + size));
    }

    @Override
    public List<String> listUsernames() {
        return listObjs(new LambdaQueryWrapper<User>()
                .select(User::getUsername)
                .orderByAsc(User::getId), Object::toString);
    }

    @Override
    public List<User> listByTag(String tag) {
        return list(new LambdaQueryWrapper<User>()
                .apply("JSON_CONTAINS(tags, JSON_QUOTE({0}))", tag)
                .orderByAsc(User::getId));
    }

    @Override
    public boolean existsUsername(String username) {
        return exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public long countByGender(Integer gender) {
        return count(new LambdaQueryWrapper<User>()
                .eq(gender != null, User::getGender, gender));
    }

    @Override
    public Map<String, Object> statistics() {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> aggregate = getMap(new QueryWrapper<User>()
                .select("COUNT(*) AS totalCount",
                        "IFNULL(SUM(balance), 0) AS totalBalance",
                        "IFNULL(MAX(age), 0) AS maxAge"));
        if (aggregate != null) {
            result.putAll(aggregate);
        }
        result.put("genderCount", listMaps(new QueryWrapper<User>()
                .select("gender", "COUNT(*) AS cnt")
                .groupBy("gender")
                .orderByAsc("gender")));
        return result;
    }

    /* ==================== 改 ==================== */

    /**
     * 练习⑤：同实体连续两次 updateById 第二次返回 false（乐观锁）
     */
    @Override
    public boolean modifyUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("更新时必须携带主键 id");
        }
        if (getById(user.getId()) == null) {
            throw new IllegalArgumentException("用户不存在：" + user.getId());
        }
        return updateById(user);
    }

    @Override
    public boolean modifyUserStatus(Long id, Integer status) {
        return update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getStatus, status));
    }

    @Override
    public boolean recharge(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于 0");
        }
        User user = getById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在：" + id);
        }
        user.setBalance(user.getBalance().add(amount));
        return updateById(user);
    }

    /* ==================== 删 ==================== */

    /**
     * 练习④：逻辑删除后 selectById 返回 null，库中 deleted=1
     */
    @Override
    public boolean removeUser(Long id) {
        return removeById(id);
    }

    @Override
    public boolean removeUserBatch(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public boolean removeUserByStatus(Integer status) {
        return remove(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, status));
    }

    @Override
    public boolean updateEmailByUsername(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username 不能为空");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email 不能为空");
        }
        return update(new LambdaUpdateWrapper<User>()
                .eq(User::getUsername, username)
                .set(User::getEmail, email));
    }

    /* ==================== 私有工具 ==================== */

    private LambdaQueryWrapper<User> buildCondition(UserQuery query) {
        UserQuery c = query == null ? new UserQuery() : query;
        return new LambdaQueryWrapper<User>()
                .eq(c.getGender() != null, User::getGender, c.getGender())
                .eq(c.getStatus() != null, User::getStatus, c.getStatus())
                .eq(c.getVip() != null, User::getVip, c.getVip())
                .eq(StringUtils.hasText(c.getMemberLevel()),
                        User::getMemberLevel, c.getMemberLevel())
                .ge(c.getMinAge() != null, User::getAge, c.getMinAge())
                .le(c.getMaxAge() != null, User::getAge, c.getMaxAge())
                .and(StringUtils.hasText(c.getKeyword()),
                        w -> w.like(User::getUsername, c.getKeyword())
                                .or()
                                .like(User::getNickname, c.getKeyword()))
                .orderByDesc(User::getId);
    }

    private void applyDefaults(User user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (user.getGender() == null) user.setGender(0);
        if (user.getMemberLevel() == null) user.setMemberLevel("BRONZE");
        if (user.getPoints() == null) user.setPoints(0);
        if (user.getBalance() == null) user.setBalance(BigDecimal.ZERO);
        if (user.getVip() == null) user.setVip(false);
        if (user.getStatus() == null) user.setStatus(1);
    }
}