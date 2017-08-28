package de.cwrose.shoppinglist.auth

import com.fasterxml.jackson.annotation.JsonIgnore
import de.cwrose.shoppinglist.User
import de.cwrose.shoppinglist.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class JwtUserDetailsService(@Autowired val userRepository: UserRepository): UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails = userRepository.findByUsername(username).let {
            user -> when (user) {
                null -> throw UsernameNotFoundException("User not found")
                else -> createUserDetails(user)
            }
        }
}

internal fun createUserDetails(user: User) = JwtUser(user.id, user.username!!, user.password!!)

class JwtUser(@JsonIgnore val id: String, private val username: String, private val password: String): UserDetails {

    override fun getUsername(): String = username

    @JsonIgnore
    override fun getPassword(): String = password

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf()

    override fun isEnabled() = true

    override fun isCredentialsNonExpired() = true

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true
}