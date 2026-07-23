package com.library.librarysystem.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.librarysystem.entity.SysRole;
import com.library.librarysystem.entity.SysUser;
import com.library.librarysystem.entity.SysUserRole;
import com.library.librarysystem.mapper.SysRoleMapper;
import com.library.librarysystem.mapper.SysUserMapper;
import com.library.librarysystem.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Load roles
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));

        List<String> roleCodes = userRoles.stream()
                .map(ur -> {
                    SysRole role = roleMapper.selectById(ur.getRoleId());
                    return role != null ? role.getCode() : null;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

        return new UserDetailsImpl(user, roleCodes);
    }
}
