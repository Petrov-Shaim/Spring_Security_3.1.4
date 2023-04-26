package com.example.spring_security.service;


import com.example.spring_security.dao.RoleRepository;
import com.example.spring_security.dao.UserRepository;
import com.example.spring_security.entity.Role;
import com.example.spring_security.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;


    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    @Transactional
    public boolean saveUser(User user) {
        User userFromDB = userRepository.findByUsername(user.getUsername());

        if (userFromDB != null) {
            throw new IllegalArgumentException("A user with this username already exists");
        }
        user.setPassword(user.getPassword());
        userRepository.save(user);
        return true;
    }


    public List<User> getAllUsers() {


        return userRepository.findAll();
    }


    @Transactional
    public User update(Integer id, User user) {
        Optional<User> userToBeUpdatedOptional = userRepository.findById(id);
        if (!userToBeUpdatedOptional.isPresent()) {
            throw new UsernameNotFoundException("A user with this ID does not exist.");
        }
        User userToBeUpdated = userToBeUpdatedOptional.get();
        userToBeUpdated.setName(user.getName());
        userToBeUpdated.setSurname(user.getSurname());
        userToBeUpdated.setPassword(user.getPassword());
        userToBeUpdated.setUsername(user.getUsername());
        userToBeUpdated.setRoles(user.getRoles());
        return userToBeUpdated;
    }


    @Transactional(readOnly = true)

    public User getUserById(Integer id) {
        Optional<User> userByIdOptional = userRepository.findById(id);
        if (!userByIdOptional.isPresent()) {
            throw new UsernameNotFoundException("A user with this ID does not exist.");
        }
        User userById = userByIdOptional.get();
        return userById;
    }


    @Transactional
    public boolean deleteUser(Integer userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("User '%s' not found", username));
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }

    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleByName(String name) {//todo do I need to remake that?
        return roleRepository.findRoleByName(name);
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).collect(Collectors.toList());
    }
}
